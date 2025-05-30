package uk.co.appoly.droid.util

import androidx.room.TypeConverter
import uk.co.appoly.droid.util.DateHelper.parseJsonDate
import uk.co.appoly.droid.util.DateHelper.parseJsonDateTime
import uk.co.appoly.droid.util.DateHelper.toJsonString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Room TypeConverters for Java 8 Time API date and time classes.
 *
 * This class provides bidirectional conversions between Java 8 time types
 * ([LocalDateTime], [LocalDate], [ZonedDateTime]) and [String] for Room database storage.
 *
 * All string representations use the standard formats defined in [DateHelper]:
 * - [LocalDateTime]: ISO-8601 format "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
 * - [LocalDate]: Simple date format "yyyy-MM-dd"
 * - [ZonedDateTime]: Converted to UTC, then stored as [LocalDateTime]
 *
 * Usage:
 * ```kotlin
 * @Database(entities = [YourEntity::class], version = 1)
 * @TypeConverters(DBDateConverters::class)
 * abstract class YourDatabase : RoomDatabase() {
 *     // ...
 * }
 * ```
 */
class DBDateConverters {
	/**
	 * Converts a [LocalDateTime] to its string representation for database storage.
	 *
	 * Uses the [DateHelper.SERVER_PATTERN_FULL] format.
	 *
	 * @param date The [LocalDateTime] to convert
	 * @return The string representation of the date-time, or null if the input is null
	 */
	@TypeConverter
	fun localDateTimeToJson(date: LocalDateTime?): String? = date.toJsonString()

	/**
	 * Converts a string representation to [LocalDateTime] when reading from the database.
	 *
	 * Attempts to parse using both [DateHelper.SERVER_PATTERN_FULL] and
	 * [DateHelper.SERVER_PATTERN_SHORT] formats.
	 *
	 * @param json The string representation to convert
	 * @return The parsed [LocalDateTime], or null if the input is null or invalid
	 */
	@TypeConverter
	fun jsonToLocalDateTime(json: String?): LocalDateTime? = json.parseJsonDateTime()

	/**
	 * Converts a [LocalDate] to its string representation for database storage.
	 *
	 * Uses the [DateHelper.SERVER_PATTERN_DATE] format.
	 *
	 * @param date The [LocalDate] to convert
	 * @return The string representation of the date, or null if the input is null
	 */
	@TypeConverter
	fun localDateToJson(date: LocalDate?): String? = date.toJsonString()

	/**
	 * Converts a string representation to [LocalDate] when reading from the database.
	 *
	 * @param json The string representation to convert
	 * @return The parsed [LocalDate], or null if the input is null or invalid
	 */
	@TypeConverter
	fun jsonToLocalDate(json: String?): LocalDate? = json.parseJsonDate()

	/**
	 * Converts a [ZonedDateTime] to its string representation for database storage.
	 *
	 * The date-time is first normalized to UTC timezone, then converted to a
	 * [LocalDateTime] which is formatted as a string.
	 *
	 * @param date The [ZonedDateTime] to convert
	 * @return The string representation of the date-time in UTC, or null if the input is null
	 */
	@TypeConverter
	fun zonedDateTimeToJson(date: ZonedDateTime?): String? =
		date?.toUTC()
			?.toLocalDateTime()
			?.toJsonString()

	/**
	 * Converts a string representation to [ZonedDateTime] when reading from the database.
	 *
	 * The string is first parsed as a [LocalDateTime], then converted to a
	 * [ZonedDateTime] in UTC timezone, and finally adjusted to the device's timezone.
	 *
	 * @param json The string representation to convert
	 * @return The parsed [ZonedDateTime] in the device's timezone, or null if the input is null or invalid
	 */
	@TypeConverter
	fun jsonToZonedDateTime(json: String?): ZonedDateTime? =
		json.parseJsonDateTime()
			?.atZone(ZoneOffset.UTC)
			?.toDeviceZone()
}
