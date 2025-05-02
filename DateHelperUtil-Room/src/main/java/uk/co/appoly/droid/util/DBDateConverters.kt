package uk.co.appoly.droid.util

import androidx.room.TypeConverter
import uk.co.appoly.droid.util.DateHelper.parseJsonDate
import uk.co.appoly.droid.util.DateHelper.parseJsonDateTime
import uk.co.appoly.droid.util.DateHelper.toJsonString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DBDateConverters {
	@TypeConverter
	fun localDateTimeToJson(date: LocalDateTime?): String? = date.toJsonString()

	@TypeConverter
	fun jsonToLocalDateTime(json: String?): LocalDateTime? = json.parseJsonDateTime()

	@TypeConverter
	fun localDateToJson(date: LocalDate?): String? = date.toJsonString()

	@TypeConverter
	fun jsonToLocalDate(json: String?): LocalDate? = json.parseJsonDate()

	@TypeConverter
	fun zonedDateTimeToJson(date: ZonedDateTime?): String? =
		date?.toUTC()
			?.toLocalDateTime()
			?.toJsonString()

	@TypeConverter
	fun jsonToZonedDateTime(json: String?): ZonedDateTime? =
		json.parseJsonDateTime()
			?.atZone(ZoneOffset.UTC)
			?.toDeviceZone()
}