package uk.co.appoly.droid.util

import com.duck.flexilogger.FlexiLog
import com.duck.flexilogger.LoggingLevel
import uk.co.appoly.droid.DateHelperLog
import uk.co.appoly.droid.util.DateHelper.SERVER_PATTERN_DATE
import uk.co.appoly.droid.util.DateHelper.SERVER_PATTERN_FULL
import uk.co.appoly.droid.util.DateHelper.SERVER_PATTERN_SHORT
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility object for standardized date and time operations.
 *
 * Provides methods for parsing and formatting dates in consistent formats,
 * with built-in error handling and logging. Uses Java 8 Time API for
 * robust date and time operations.
 *
 * The helper uses three standard date formats:
 * - [SERVER_PATTERN_FULL]: ISO-8601 extended format with microseconds, e.g., "2023-12-01T10:38:29.000000Z"
 * - [SERVER_PATTERN_SHORT]: Simple date-time format without timezone, e.g., "2023-12-01 10:38:29"
 * - [SERVER_PATTERN_DATE]: Date-only format, e.g., "2023-12-01"
 */
object DateHelper {
	/**
	 * ISO-8601 extended format with microseconds, e.g., "2023-12-01T10:38:29.000000Z"
	 */
	const val SERVER_PATTERN_FULL = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"

	/**
	 * Simple date-time format without timezone, e.g., "2023-12-01 10:38:29"
	 */
	const val SERVER_PATTERN_SHORT = "yyyy-MM-dd HH:mm:ss"

	/**
	 * Date-only format, e.g., "2023-12-01"
	 */
	const val SERVER_PATTERN_DATE = "yyyy-MM-dd"

	/**
	 * Set the logger for this class
	 * @param logger [FlexiLog] the logger to use
	 * @param loggingLevel [LoggingLevel] the logging level to use
	 */
	fun setLogger(
		logger: FlexiLog,
		loggingLevel: LoggingLevel = LoggingLevel.NONE
	) {
		DateHelperLog.updateLogger(logger, loggingLevel)
	}

	/**
	 * Parses a string to [LocalDateTime] using standard formats.
	 *
	 * This method attempts to parse the provided string first using [SERVER_PATTERN_FULL],
	 * and if that fails, falls back to [SERVER_PATTERN_SHORT]. Parsing failures are logged
	 * with appropriate error messages.
	 *
	 * @param dateTime String representation of date-time to parse (e.g., "2023-12-01T10:38:29.000000Z")
	 * @return Parsed [LocalDateTime] or null if the input is null, blank, or cannot be parsed
	 */
	fun parseLocalDateTime(dateTime: String?): LocalDateTime? {
		return if (dateTime.isNullOrBlank()) {
			null
		} else {
			//attempt to pars from pattern SERVER_PATTERN_FULL
			try {
				val it: LocalDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(SERVER_PATTERN_FULL))
				it
			} catch (e: Exception) {
				DateHelperLog.d(this, "parseLocalDateTime: failed to parse \"$dateTime\" from SERVER_PATTERN_FULL, trying with SERVER_PATTERN_SHORT", e)
				null
			} ?: run {
				//attempt to pars from pattern SERVER_PATTERN_SHORT
				try {
					val it: LocalDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(SERVER_PATTERN_SHORT))
					it
				} catch (e: Exception) {
					DateHelperLog.e(this@DateHelper, "parseLocalDateTime: failed to parse \"$dateTime\" with SERVER_PATTERN_SHORT", e)
					null
				}
			}
		}
	}

	/**
	 * Formats a [LocalDateTime] to string using [SERVER_PATTERN_FULL] format.
	 *
	 * @param dateTime The [LocalDateTime] to format
	 * @return Formatted date-time string or null if the input is null
	 */
	fun formatLocalDateTime(dateTime: LocalDateTime?): String? {
		return if (dateTime == null) {
			null
		} else {
			DateTimeFormatter.ofPattern(SERVER_PATTERN_FULL).format(dateTime)
		}
	}

	/**
	 * Parses a string to [LocalDate] using standard date format.
	 *
	 * This method first attempts to parse using [SERVER_PATTERN_DATE], and if that fails,
	 * tries to parse it as a date-time and extracts the date part. Parsing failures are
	 * logged with appropriate error messages.
	 *
	 * @param dateTime String representation of date to parse (e.g., "2023-12-01")
	 * @return Parsed [LocalDate] or null if the input is null, blank, or cannot be parsed
	 */
	fun parseLocalDate(dateTime: String?): LocalDate? {
		return if (dateTime.isNullOrBlank()) {
			null
		} else {
			//attempt to pars from pattern SERVER_PATTERN_DATE
			try {
				val it: LocalDate = LocalDate.parse(dateTime, DateTimeFormatter.ofPattern(SERVER_PATTERN_DATE))
				it
			} catch (e: Exception) {
				DateHelperLog.e(this@DateHelper, "parseLocalDate: failed to parse \"$dateTime\" with SERVER_PATTERN_DATE", e)
				null
			} ?: run {
				//attempt to parse as date-time
				parseLocalDateTime(dateTime)?.toLocalDate()
			}
		}
	}

	/**
	 * Formats a [LocalDate] to string using [SERVER_PATTERN_DATE] format.
	 *
	 * @param date The [LocalDate] to format
	 * @return Formatted date string or null if the input is null
	 */
	fun formatLocalDate(date: LocalDate?): String? {
		return if (date == null) {
			null
		} else {
			DateTimeFormatter.ofPattern(SERVER_PATTERN_DATE).format(date)
		}
	}

	/**
	 * Extension function to format a [LocalDateTime] as a JSON string.
	 *
	 * @return Formatted date-time string using [SERVER_PATTERN_FULL] or null if the receiver is null
	 */
	fun LocalDateTime?.toJsonString(): String? {
		return formatLocalDateTime(this)
	}

	/**
	 * Extension function to parse a JSON date-time string to [LocalDateTime].
	 *
	 * @return Parsed [LocalDateTime] or null if the string is null, blank, or cannot be parsed
	 */
	fun String?.parseJsonDateTime(): LocalDateTime? {
		return parseLocalDateTime(this)
	}

	/**
	 * Extension function to format a [LocalDate] as a JSON string.
	 *
	 * @return Formatted date string using [SERVER_PATTERN_DATE] or null if the receiver is null
	 */
	fun LocalDate?.toJsonString(): String? {
		return formatLocalDate(this)
	}

	/**
	 * Extension function to parse a JSON date string to [LocalDate].
	 *
	 * @return Parsed [LocalDate] or null if the string is null, blank, or cannot be parsed
	 */
	fun String?.parseJsonDate(): LocalDate? {
		return parseLocalDate(this)
	}

	/**
	 * Formats a [LocalDateTime] for use in filenames using a safe format.
	 *
	 * This format uses hyphens instead of colons for time values, making it
	 * safe for use in filenames across different operating systems.
	 *
	 * @return A string in the format "yyyy-MM-dd_HH-mm-ss.SSS"
	 */
	fun LocalDateTime.toFileString(): String {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS").format(this)
	}

	/**
	 * Gets the current date and time in UTC timezone.
	 *
	 * @return Current [ZonedDateTime] in UTC timezone
	 */
	fun nowAsUTC(): ZonedDateTime {
		return ZonedDateTime.now().toUTC()
	}
}
