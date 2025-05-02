package uk.co.appoly.droid.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Adjusts the [ZonedDateTime] to UTC timezone
 */
fun ZonedDateTime.toUTC(): ZonedDateTime {
	return this.withZoneSameInstant(ZoneOffset.UTC)
}

@OptIn(ExperimentalContracts::class)
fun LocalDateTime?.isFuture(): Boolean {
	contract { returns(true) implies (this@isFuture != null) }
	return this?.isAfter(LocalDateTime.now()) == true
}

@OptIn(ExperimentalContracts::class)
fun ZonedDateTime?.isFuture(): Boolean {
	contract { returns(true) implies (this@isFuture != null) }
	return this?.isAfter(ZonedDateTime.now()) == true
}

@OptIn(ExperimentalContracts::class)
fun LocalDateTime?.isPassed(): Boolean {
	contract { returns(true) implies (this@isPassed != null) }
	return this?.isBefore(LocalDateTime.now()) == true
}

@OptIn(ExperimentalContracts::class)
fun ZonedDateTime?.isPassed(): Boolean {
	contract { returns(true) implies (this@isPassed != null) }
	return this?.isBefore(ZonedDateTime.now()) == true
}

fun LocalDateTime.deviceToUTC(): LocalDateTime {
	return this.atZone(ZoneOffset.systemDefault()).toUTC().toLocalDateTime()
}

/**
 * Adjusts the [ZonedDateTime] to system default timezone
 */
fun ZonedDateTime.toDeviceZone(): ZonedDateTime {
	return this.withZoneSameInstant(ZoneOffset.systemDefault())
}

fun Duration.truncatedTo(unit: Duration): Duration = unit.multipliedBy(this.dividedBy(unit.toMillis()).toMillis())

fun LocalDateTime?.toMillis(zoneOffset: ZoneOffset? = ZoneOffset.UTC): Long? {
	return this?.toEpochSecond(zoneOffset)?.times(1000)
}

fun LocalDate?.toMillis(zoneOffset: ZoneOffset? = ZoneOffset.UTC): Long? {
	return this?.atStartOfDay().toMillis(zoneOffset = zoneOffset)
}

fun Long.millisToLocalDateTime(zoneOffset: ZoneOffset? = ZoneOffset.UTC): LocalDateTime {
	return LocalDateTime.ofEpochSecond(this / 1000, 0, zoneOffset)
}

fun Long.millisToLocalDate(zoneOffset: ZoneOffset? = ZoneOffset.UTC): LocalDate {
	return this.millisToLocalDateTime(zoneOffset).toLocalDate()
}