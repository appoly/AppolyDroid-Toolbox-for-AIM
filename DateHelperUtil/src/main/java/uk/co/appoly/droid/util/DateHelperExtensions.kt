package uk.co.appoly.droid.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Extension functions for date and time operations.
 *
 * This file contains extensions to Java 8 Time API classes for common operations
 * like time zone conversions, future/past checks, and conversions between
 * date-time types and milliseconds.
 */

/**
 * Adjusts the [ZonedDateTime] to UTC timezone.
 *
 * This extension function takes a [ZonedDateTime] in any timezone and returns
 * a new [ZonedDateTime] representing the same instant in UTC.
 *
 * @return A new [ZonedDateTime] representing the same instant in UTC timezone
 */
fun ZonedDateTime.toUTC(): ZonedDateTime {
	return this.withZoneSameInstant(ZoneOffset.UTC)
}

/**
 * Checks if this [LocalDateTime] is in the future.
 *
 * Uses Kotlin contracts to allow smart casting after null checking.
 *
 * @return True if the date-time is not null and is after the current date-time
 */
@OptIn(ExperimentalContracts::class)
fun LocalDateTime?.isFuture(): Boolean {
	contract { returns(true) implies (this@isFuture != null) }
	return this?.isAfter(LocalDateTime.now()) == true
}

/**
 * Checks if this [ZonedDateTime] is in the future.
 *
 * Uses Kotlin contracts to allow smart casting after null checking.
 *
 * @return True if the date-time is not null and is after the current date-time
 */
@OptIn(ExperimentalContracts::class)
fun ZonedDateTime?.isFuture(): Boolean {
	contract { returns(true) implies (this@isFuture != null) }
	return this?.isAfter(ZonedDateTime.now()) == true
}

/**
 * Checks if this [LocalDateTime] is in the past.
 *
 * Uses Kotlin contracts to allow smart casting after null checking.
 *
 * @return True if the date-time is not null and is before the current date-time
 */
@OptIn(ExperimentalContracts::class)
fun LocalDateTime?.isPassed(): Boolean {
	contract { returns(true) implies (this@isPassed != null) }
	return this?.isBefore(LocalDateTime.now()) == true
}

/**
 * Checks if this [ZonedDateTime] is in the past.
 *
 * Uses Kotlin contracts to allow smart casting after null checking.
 *
 * @return True if the date-time is not null and is before the current date-time
 */
@OptIn(ExperimentalContracts::class)
fun ZonedDateTime?.isPassed(): Boolean {
	contract { returns(true) implies (this@isPassed != null) }
	return this?.isBefore(ZonedDateTime.now()) == true
}

/**
 * Converts a [LocalDateTime] from device timezone to UTC.
 *
 * This extension function takes a local device time and converts it to the
 * equivalent time in UTC.
 *
 * @return A new [LocalDateTime] representing the same instant in UTC
 */
fun LocalDateTime.deviceToUTC(): LocalDateTime {
	return this.atZone(ZoneOffset.systemDefault()).toUTC().toLocalDateTime()
}

/**
 * Adjusts the [ZonedDateTime] to the system's default timezone.
 *
 * This extension function takes a [ZonedDateTime] in any timezone and returns
 * a new [ZonedDateTime] representing the same instant in the device's timezone.
 *
 * @return A new [ZonedDateTime] representing the same instant in the device's timezone
 */
fun ZonedDateTime.toDeviceZone(): ZonedDateTime {
	return this.withZoneSameInstant(ZoneOffset.systemDefault())
}

/**
 * Truncates a [Duration] to a specified unit.
 *
 * This function rounds down the duration to a multiple of the specified unit.
 *
 * @param unit The duration unit to truncate to
 * @return A new [Duration] truncated to the specified unit
 */
fun Duration.truncatedTo(unit: Duration): Duration = unit.multipliedBy(this.dividedBy(unit.toMillis()).toMillis())

/**
 * Converts a [LocalDateTime] to milliseconds since the epoch.
 *
 * @param zoneOffset The timezone offset to use for the conversion (defaults to UTC)
 * @return The number of milliseconds since the epoch, or null if the receiver is null
 */
fun LocalDateTime?.toMillis(zoneOffset: ZoneOffset? = ZoneOffset.UTC): Long? {
	return this?.toEpochSecond(zoneOffset)?.times(1000)
}

/**
 * Converts a [LocalDate] to milliseconds since the epoch.
 *
 * Converts the date to the start of day in the specified timezone.
 *
 * @param zoneOffset The timezone offset to use for the conversion (defaults to UTC)
 * @return The number of milliseconds since the epoch, or null if the receiver is null
 */
fun LocalDate?.toMillis(zoneOffset: ZoneOffset? = ZoneOffset.UTC): Long? {
	return this?.atStartOfDay().toMillis(zoneOffset = zoneOffset)
}

/**
 * Converts milliseconds since the epoch to a [LocalDateTime].
 *
 * @param zoneOffset The timezone offset to use for the conversion (defaults to UTC)
 * @return A [LocalDateTime] representing the specified instant
 */
fun Long.millisToLocalDateTime(zoneOffset: ZoneOffset? = ZoneOffset.UTC): LocalDateTime {
	return LocalDateTime.ofEpochSecond(this / 1000, 0, zoneOffset)
}

/**
 * Converts milliseconds since the epoch to a [LocalDate].
 *
 * @param zoneOffset The timezone offset to use for the conversion (defaults to UTC)
 * @return A [LocalDate] representing the date part of the specified instant
 */
fun Long.millisToLocalDate(zoneOffset: ZoneOffset? = ZoneOffset.UTC): LocalDate {
	return this.millisToLocalDateTime(zoneOffset).toLocalDate()
}
