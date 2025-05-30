import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.regex.Pattern

/**
 * Function type for processing regex matches.
 *
 * @return Triple containing:
 * - The updated content string
 * - Whether inconsistencies were found
 * - The count of updates made
 */
private typealias MatchProcessor = (String, File, String, String, Boolean) -> Triple<String, Boolean, Int>

/**
 * Gradle task to update version numbers in README.md files based on the toolboxVersion and other versions in libs.versions.toml
 *
 * In this project this task is connected to run with the gradle sync task.
 */
abstract class UpdateReadmeVersions : DefaultTask() {

    /**
     * If true, only checks if versions are up to date without modifying files.
     * Fails the build if versions are inconsistent.
     */
    @get:Internal
    abstract val checkOnly: Property<Boolean>

    init {
        checkOnly.convention(false)
    }

    @TaskAction
    fun updateVersions() {
        logger.lifecycle("Starting UpdateReadmeVersions task")
        val rootDir = project.rootDir
        val tomlFile = File(rootDir, "gradle/libs.versions.toml")

        if (!tomlFile.exists()) {
            logger.error("libs.versions.toml not found at ${tomlFile.absolutePath}")
            return
        }

        // Extract versions from libs.versions.toml
        val tomlContent = tomlFile.readText()
        val versionsMap = mutableMapOf<String, String?>()

        versionsMap["toolbox"] = extractVersion(tomlContent, "toolboxVersion")
        versionsMap["room"] = extractVersion(tomlContent, "roomVersion")
        versionsMap["kotlinx-serialization"] = extractVersion(tomlContent, "kotlinxSerialization")

        // Check if all required versions were found
        val missingVersions = versionsMap.filterValues { it == null }.keys
        if (missingVersions.isNotEmpty()) {
            missingVersions.forEach { key ->
                logger.error("Could not find ${key.replace("kotlinx-serialization", "kotlinxSerialization")}Version in libs.versions.toml")
            }
            return
        }

        val toolboxVersion = versionsMap["toolbox"]!!
        val roomVersion = versionsMap["room"]!!
        val kotlinxSerializationVersion = versionsMap["kotlinx-serialization"]!!

        logger.lifecycle("Found versions - toolbox: $toolboxVersion, room: $roomVersion, kotlinx-serialization: $kotlinxSerializationVersion")

        // Find all README.md files in the project
        val readmeFiles = rootDir.walk()
            .filter { it.name.equals("README.md", ignoreCase = true) }
            .toList()

        logger.lifecycle("Found ${readmeFiles.size} README.md files to check" + if (checkOnly.get()) "" else " and update")

        // Variables to track inconsistencies
        var totalUpdates = 0
        var hasInconsistencies = false

        readmeFiles.forEach { file ->
            var content = file.readText()
            var fileUpdates = 0

            // Pattern 1: Check toolbox implementation statements in code blocks
            val toolboxResult = updateVersions(
                content = content,
                pattern = Pattern.compile("(implementation\\([\"']com\\.github\\.appoly\\.AppolyDroid-Toolbox:[^:]+:)([^\"')]+)([\"')])")
                    .toMatchProcessor(1, 3) { it == toolboxVersion },
                file = file,
                versionName = "toolbox",
                expectedVersion = toolboxVersion,
                checkOnly = checkOnly.get()
            )

            if (toolboxResult.second) hasInconsistencies = true
            content = toolboxResult.first
            fileUpdates += toolboxResult.third
            totalUpdates += toolboxResult.third

            // Pattern 2: Check Room dependency versions in code blocks
            val roomResult = updateVersions(
                content = content,
                pattern = Pattern.compile("(implementation\\([\"']androidx\\.room:[^:]+:)([^\"')]+)([\"')])")
                    .toMatchProcessor(1, 3) { it == roomVersion },
                file = file,
                versionName = "room",
                expectedVersion = roomVersion,
                checkOnly = checkOnly.get()
            )

            if (roomResult.second) hasInconsistencies = true
            content = roomResult.first
            fileUpdates += roomResult.third
            totalUpdates += roomResult.third

            // Pattern 3: Check kapt Room compiler versions in code blocks
            val kaptRoomResult = updateVersions(
                content = content,
                pattern = Pattern.compile("(kapt\\([\"']androidx\\.room:[^:]+:)([^\"')]+)([\"')])")
                    .toMatchProcessor(1, 3) { it == roomVersion },
                file = file,
                versionName = "room",
                expectedVersion = roomVersion,
                checkOnly = checkOnly.get()
            )

            if (kaptRoomResult.second) hasInconsistencies = true
            content = kaptRoomResult.first
            fileUpdates += kaptRoomResult.third
            totalUpdates += kaptRoomResult.third

            // Pattern 4: Check kotlinx-serialization dependency versions in code blocks
            val kotlinxSerializationResult = updateVersions(
                content = content,
                pattern = Pattern.compile("(implementation\\([\"']org\\.jetbrains\\.kotlinx:kotlinx-serialization-[^:]+:)([^\"')]+)([\"')])")
                    .toMatchProcessor(1, 3) { it == kotlinxSerializationVersion },
                file = file,
                versionName = "kotlinx-serialization",
                expectedVersion = kotlinxSerializationVersion,
                checkOnly = checkOnly.get()
            )

            if (kotlinxSerializationResult.second) hasInconsistencies = true
            content = kotlinxSerializationResult.first
            fileUpdates += kotlinxSerializationResult.third
            totalUpdates += kotlinxSerializationResult.third

            // Pattern 5: Check TOML version example in main README
            if (file.name.equals("README.md", ignoreCase = true) && file.parentFile == rootDir) {
                try {
                    val tomlResult = updateVersions(
                        content = content,
                        pattern = Pattern.compile("(appolydroidToolbox\\s*=\\s*\")([^\"]+)(\".+?\\#\\s*Replace with the latest version)")
                            .toMatchProcessor(1, 3) { it == toolboxVersion },
                        file = file,
                        versionName = "toolbox TOML example",
                        expectedVersion = toolboxVersion,
                        checkOnly = checkOnly.get()
                    )

                    if (tomlResult.second) hasInconsistencies = true
                    content = tomlResult.first
                    fileUpdates += tomlResult.third
                    totalUpdates += tomlResult.third
                } catch (e: Exception) {
                    logger.warn("Error processing TOML version example in main README: ${e.message}")
                }
            }

            if (fileUpdates > 0 && !checkOnly.get()) {
                try {
                    file.writeText(content)
                    logger.lifecycle("Updated ${file.name} with $fileUpdates version changes")
                } catch (e: Exception) {
                    logger.error("Failed to update ${file.name}: ${e.message}")
                }
            }
        }

        if (checkOnly.get()) {
            if (hasInconsistencies) {
                throw RuntimeException("README version references are inconsistent with versions in libs.versions.toml. Run 'updateReadmeVersions' task to fix.")
            } else {
                logger.lifecycle("All README version references are consistent with versions in libs.versions.toml")
            }
        } else {
            logger.lifecycle("Total version references updated: $totalUpdates")
        }
    }

    /**
     * Extracts a version value from the TOML content.
     *
     * @param tomlContent The content of the libs.versions.toml file
     * @param versionKey The key to extract (without the "Version" suffix)
     * @return The extracted version or null if not found
     */
    private fun extractVersion(tomlContent: String, versionKey: String): String? {
        val versionRegex = Pattern.compile("$versionKey\\s*=\\s*\"([^\"]+)\"")
        val matcher = versionRegex.matcher(tomlContent)
        return if (matcher.find()) matcher.group(1) else null
    }

    /**
     * Extension function to create a version match processor from a regex pattern.
     *
     * @param prefixGroup The capturing group index for the prefix
     * @param suffixGroup The capturing group index for the suffix
     * @param isCorrectVersion A function that checks if a version is correct
     * @return A MatchProcessor that processes matches of the pattern
     */
    private fun Pattern.toMatchProcessor(
        prefixGroup: Int,
        suffixGroup: Int,
        isCorrectVersion: (String) -> Boolean
    ): MatchProcessor {
        return { content, file, versionName, expectedVersion, checkOnly ->
            val matcher = this.matcher(content)
            val sb = StringBuffer()
            var updatedCount = 0
            var hasInconsistencies = false

            while (matcher.find()) {
                val foundVersion = matcher.group(2)
                if (!isCorrectVersion(foundVersion)) {
                    if (checkOnly) {
                        logger.error("$versionName version mismatch in ${file.name}: found $foundVersion, expected $expectedVersion")
                        hasInconsistencies = true
                    } else {
                        val replacement = matcher.group(prefixGroup) + expectedVersion + matcher.group(suffixGroup)
                        matcher.appendReplacement(sb, replacement)
                        updatedCount++
                    }
                } else if (!checkOnly) {
                    matcher.appendReplacement(sb, matcher.group())
                }
            }

            if (!checkOnly) {
                matcher.appendTail(sb)
                Triple(sb.toString(), hasInconsistencies, updatedCount)
            } else {
                Triple(content, hasInconsistencies, 0)
            }
        }
    }

    /**
     * Updates versions in content based on a pattern.
     *
     * @param content The content to update
     * @param pattern The regex pattern match processor
     * @param file The file being processed (for logging)
     * @param versionName The name of the version being checked
     * @param expectedVersion The expected version value
     * @param checkOnly Whether to only check or also update versions
     * @return Triple containing updated content, whether inconsistencies were found, and the update count
     */
    private fun updateVersions(
        content: String,
        pattern: MatchProcessor,
        file: File,
        versionName: String,
        expectedVersion: String,
        checkOnly: Boolean
    ): Triple<String, Boolean, Int> {
        return pattern(content, file, versionName, expectedVersion, checkOnly)
    }
}
