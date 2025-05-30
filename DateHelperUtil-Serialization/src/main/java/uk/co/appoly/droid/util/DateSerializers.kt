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

/**
 * Serializer for nullable [LocalDate] instances using kotlinx.serialization.
 *
 * This serializer handles nullable [LocalDate] values and uses the standard date format
 * defined in [DateHelper] (yyyy-MM-dd).
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Event(
 *     val id: Int,
 *     @Serializable(with = NullableLocalDateSerializer::class)
 *     val date: LocalDate?
 * )
 * ```
 */
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

/**
 * Serializer for non-nullable [LocalDate] instances using kotlinx.serialization.
 *
 * This serializer handles non-nullable [LocalDate] values and uses the standard date format
 * defined in [DateHelper] (yyyy-MM-dd).
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Event(
 *     val id: Int,
 *     @Serializable(with = LocalDateSerializer::class)
 *     val date: LocalDate
 * )
 * ```
 */
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

/**
 * Serializer for nullable [LocalDateTime] instances using kotlinx.serialization.
 *
 * This serializer handles nullable [LocalDateTime] values and uses the standard ISO-8601 format
 * defined in [DateHelper] (yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z').
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Event(
 *     val id: Int,
 *     @Serializable(with = NullableDateTimeSerializer::class)
 *     val startTime: LocalDateTime?
 * )
 * ```
 */
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

/**
 * Serializer for non-nullable [LocalDateTime] instances using kotlinx.serialization.
 *
 * This serializer handles non-nullable [LocalDateTime] values and uses the standard ISO-8601 format
 * defined in [DateHelper] (yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z').
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Event(
 *     val id: Int,
 *     @Serializable(with = DateTimeSerializer::class)
 *     val startTime: LocalDateTime
 * )
 * ```
 */
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
 * Serializer for non-nullable [ZonedDateTime] instances using kotlinx.serialization.
 *
 * This serializer handles [ZonedDateTime] values by:
 * 1. Converting to UTC timezone before serialization
 * 2. Serializing as an ISO-8601 formatted string
 * 3. When deserializing, parsing the string and converting to the device's timezone
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Event(
 *     val id: Int,
 *     @Serializable(with = ZonedDateTimeSerializer::class)
 *     val startTime: ZonedDateTime
 * )
 * ```
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
 * Serializer for nullable [ZonedDateTime] instances using kotlinx.serialization.
 *
 * This serializer handles nullable [ZonedDateTime] values by:
 * 1. Converting to UTC timezone before serialization (if not null)
 * 2. Serializing as an ISO-8601 formatted string
 * 3. When deserializing, parsing the string and converting to the device's timezone
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Event(
 *     val id: Int,
 *     @Serializable(with = NullableZonedDateTimeSerializer::class)
 *     val reminderTime: ZonedDateTime?
 * )
 * ```
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
