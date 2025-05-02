package uk.co.appoly.droid.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import uk.co.appoly.droid.util.DateHelper.parseJsonDate
import uk.co.appoly.droid.util.DateHelper.parseJsonDateTime
import uk.co.appoly.droid.util.DateHelper.toJsonString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDate::class)
object NullableLocalDateSerializer : KSerializer<LocalDate?> {
	override fun serialize(encoder: Encoder, value: LocalDate?) {
		value.toJsonString()?.let {
			encoder.encodeString(it)
		}
	}

	override fun deserialize(decoder: Decoder): LocalDate? =
		decoder.decodeString().parseJsonDate()
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDate::class)
object LocalDateSerializer : KSerializer<LocalDate> {
	override fun serialize(encoder: Encoder, value: LocalDate) {
		value.toJsonString()?.let {
			encoder.encodeString(it)
		}
	}

	override fun deserialize(decoder: Decoder): LocalDate =
		decoder.decodeString().parseJsonDate()!!
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDateTime::class)
object NullableDateTimeSerializer : KSerializer<LocalDateTime?> {
	override fun serialize(encoder: Encoder, value: LocalDateTime?) {
		value.toJsonString()?.let {
			encoder.encodeString(it)
		}
	}

	override fun deserialize(decoder: Decoder): LocalDateTime? =
		decoder.decodeString().parseJsonDateTime()
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDateTime::class)
object DateTimeSerializer : KSerializer<LocalDateTime> {
	override fun serialize(encoder: Encoder, value: LocalDateTime) {
		value.toJsonString()?.let {
			encoder.encodeString(it)
		}
	}

	override fun deserialize(decoder: Decoder): LocalDateTime =
		decoder.decodeString().parseJsonDateTime()!!
}

/**
 * ZonedDateTime using UTC timezone
 * serialize from ZonedDateTime as UTC
 * deserialize to ZonedDateTime as UTC then convert to system default timezone
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDateTime::class)
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
	override fun serialize(encoder: Encoder, value: ZonedDateTime) {
		value.toUTC().toLocalDateTime().toJsonString()?.let {
			encoder.encodeString(it)
		}
	}

	override fun deserialize(decoder: Decoder): ZonedDateTime =
		decoder.decodeString().parseJsonDateTime()!!.atZone(ZoneOffset.UTC).toDeviceZone()
}

/**
 * ZonedDateTime using UTC timezone
 * serialize from ZonedDateTime as UTC
 * deserialize to ZonedDateTime as UTC then convert to system default timezone
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDateTime::class)
object NullableZonedDateTimeSerializer : KSerializer<ZonedDateTime?> {
	override fun serialize(encoder: Encoder, value: ZonedDateTime?) {
		value?.toUTC()?.toLocalDateTime().toJsonString()?.let {
			encoder.encodeString(it)
		}
	}

	override fun deserialize(decoder: Decoder): ZonedDateTime? =
		decoder.decodeString().parseJsonDateTime()?.atZone(ZoneOffset.UTC)?.toDeviceZone()
}