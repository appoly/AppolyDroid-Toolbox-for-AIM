package uk.co.appoly.droid.util

import com.duck.flexilogger.FlexiLog
import com.duck.flexilogger.LoggerWithLevel
import com.duck.flexilogger.LoggingLevel
import uk.co.appoly.droid.DateHelperLogger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateHelper {
	private var logger: LoggerWithLevel = DateHelperLogger.withLevel(LoggingLevel.NONE)
	const val SERVER_PATTERN_FULL = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
	const val SERVER_PATTERN_SHORT = "yyyy-MM-dd HH:mm:ss"
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
		this.logger = logger.withLevel(loggingLevel)
	}

	/**
	 * parse to LocalDateTime from "2023-12-01T10:38:29.000000Z"
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
				logger.d(this, "parseLocalDateTime: failed to parse \"$dateTime\" from SERVER_PATTERN_FULL, trying with SERVER_PATTERN_SHORT", e)
				null
			} ?: run {
				//attempt to pars from pattern SERVER_PATTERN_SHORT
				try {
					val it: LocalDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(SERVER_PATTERN_SHORT))
					it
				} catch (e: Exception) {
					logger.e(this@DateHelper, "parseLocalDateTime: failed to parse \"$dateTime\" with SERVER_PATTERN_SHORT", e)
					null
				}
			}
		}
	}

	fun formatLocalDateTime(dateTime: LocalDateTime?): String? {
		return if (dateTime == null) {
			null
		} else {
			DateTimeFormatter.ofPattern(SERVER_PATTERN_FULL).format(dateTime)
		}
	}

	fun parseLocalDate(dateTime: String?): LocalDate? {
		return if (dateTime.isNullOrBlank()) {
			null
		} else {
			//attempt to pars from pattern SERVER_PATTERN_DATE
			try {
				val it: LocalDate = LocalDate.parse(dateTime, DateTimeFormatter.ofPattern(SERVER_PATTERN_DATE))
				it
			} catch (e: Exception) {
				logger.e(this@DateHelper, "parseLocalDate: failed to parse \"$dateTime\" with SERVER_PATTERN_DATE", e)
				null
			} ?: run {
				//attempt to parse as date-time
				parseLocalDateTime(dateTime)?.toLocalDate()
			}
		}
	}

	fun formatLocalDate(date: LocalDate?): String? {
		return if (date == null) {
			null
		} else {
			DateTimeFormatter.ofPattern(SERVER_PATTERN_DATE).format(date)
		}
	}

	fun LocalDateTime?.toJsonString(): String? {
		return formatLocalDateTime(this)
	}

	fun String?.parseJsonDateTime(): LocalDateTime? {
		return parseLocalDateTime(this)
	}

	fun LocalDate?.toJsonString(): String? {
		return formatLocalDate(this)
	}

	fun String?.parseJsonDate(): LocalDate? {
		return parseLocalDate(this)
	}

	fun LocalDateTime.toFileString(): String {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS").format(this)
	}

	fun nowAsUTC(): ZonedDateTime {
		return ZonedDateTime.now().toUTC()
	}
}