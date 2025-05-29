import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.regex.Pattern

/**
 * Gradle task to update version numbers in README.md files based on the toolboxVersion in libs.versions.toml
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

        // Extract the toolbox version from libs.versions.toml
        val tomlContent = tomlFile.readText()
        val versionRegex = Pattern.compile("toolboxVersion\\s*=\\s*\"([^\"]+)\"")
        val matcher = versionRegex.matcher(tomlContent)

        if (!matcher.find()) {
            logger.error("Could not find toolboxVersion in libs.versions.toml")
            return
        }

        val currentVersion = matcher.group(1)
        logger.lifecycle("Found toolboxVersion: $currentVersion")

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

            // Pattern 1: Check implementation statements in code blocks
            val implementationPattern = Pattern.compile("(implementation\\([\"']com\\.github\\.appoly\\.AppolyDroid-Toolbox:[^:]+:)([^\"')]+)([\"')])")
            val matcher1 = implementationPattern.matcher(content)
            val sb1 = StringBuffer()

            while (matcher1.find()) {
                val foundVersion = matcher1.group(2)
                if (foundVersion != currentVersion) {
                    if (checkOnly.get()) {
                        logger.error("Version mismatch in ${file.name}: found $foundVersion, expected $currentVersion")
                        hasInconsistencies = true
                    } else {
                        matcher1.appendReplacement(sb1, "${matcher1.group(1)}$currentVersion${matcher1.group(3)}")
                        fileUpdates++
                        totalUpdates++
                    }
                } else if (!checkOnly.get()) {
                    matcher1.appendReplacement(sb1, matcher1.group())
                }
            }

            if (!checkOnly.get()) {
                matcher1.appendTail(sb1)
                content = sb1.toString()
            }

            // Pattern 2: Check TOML version example in main README
            if (file.name.equals("README.md", ignoreCase = true)) {
                val tomlVersionPattern = Pattern.compile("(appolydroidToolbox\\s*=\\s*\")([^\"]+)(\".+?\\#\\s*Replace with the latest version)")
                val matcher2 = tomlVersionPattern.matcher(content)
                val sb2 = StringBuffer()

                while (matcher2.find()) {
                    val foundVersion = matcher2.group(2)
                    if (foundVersion != currentVersion) {
                        if (checkOnly.get()) {
                            logger.error("TOML example version mismatch in ${file.name}: found $foundVersion, expected $currentVersion")
                            hasInconsistencies = true
                        } else {
                            matcher2.appendReplacement(sb2, "${matcher2.group(1)}$currentVersion${matcher2.group(3)}")
                            fileUpdates++
                            totalUpdates++
                            logger.lifecycle("Updated version in TOML example in ${file.name}")
                        }
                    } else if (!checkOnly.get()) {
                        matcher2.appendReplacement(sb2, matcher2.group())
                    }
                }

                if (!checkOnly.get()) {
                    matcher2.appendTail(sb2)
                    content = sb2.toString()
                }
            }

            if (fileUpdates > 0 && !checkOnly.get()) {
                file.writeText(content)
                logger.lifecycle("Updated ${file.name} with $fileUpdates version changes")
            }
        }

        if (checkOnly.get()) {
            if (hasInconsistencies) {
                throw RuntimeException("README version references are inconsistent with toolboxVersion in libs.versions.toml. Run 'updateReadmeVersions' task to fix.")
            } else {
                logger.lifecycle("All README version references are consistent with toolboxVersion ($currentVersion)")
            }
        } else {
            logger.lifecycle("Total version references updated: $totalUpdates")
        }
    }
}
