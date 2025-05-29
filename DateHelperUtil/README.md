# DateHelperUtil

A utility module for standardized date and time operations in Android applications.

## Features

- Easy date and time formatting
- Date parsing with error handling
- Time zone conversions
- Common date operations
- Localization support

## Installation

```gradle.kts
implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil:1.0.12")
```

## Usage

### Basic Date Formatting

```kotlin
// Format a date to standard string format
val dateString = DateHelper.formatDate(date)

// Format with specific pattern
val customFormatted = DateHelper.formatDate(date, "yyyy-MM-dd HH:mm")

// Format with localization
val localizedDate = DateHelper.formatDateLocalized(date, DateFormat.MEDIUM)
```

### Parsing Date Strings

```kotlin
// Parse ISO date string
val date = DateHelper.parseIsoDate("2025-05-29T12:30:00Z")

// Parse with custom format
val customDate = DateHelper.parseDate("29/05/2025 12:30", "dd/MM/yyyy HH:mm")

// Safe parsing with fallback
val safeDate = DateHelper.parseDateSafe("invalid date", "yyyy-MM-dd") ?: LocalDateTime.now()
```

### Date Calculations

```kotlin
// Add days to a date
val tomorrow = DateHelper.addDays(today, 1)

// Check if a date is in the past
val isPast = DateHelper.isPast(date)

// Get time difference
val daysBetween = DateHelper.daysBetween(startDate, endDate)
```

### Time Zone Handling

```kotlin
// Convert to UTC
val utcDate = DateHelper.toUtc(localDate)

// Convert from UTC to local time zone
val localDate = DateHelper.fromUtc(utcDate)

// Format with specific time zone
val formattedDate = DateHelper.formatDateWithTimeZone(date, "Europe/London", "yyyy-MM-dd HH:mm z")
```

### Date Comparison

```kotlin
// Compare dates
val isSameDay = DateHelper.isSameDay(date1, date2)

// Check if date is today
val isToday = DateHelper.isToday(date)

// Check if date is between two other dates
val isBetween = DateHelper.isBetween(date, startDate, endDate)
```

## API Reference

### Core Methods

#### Formatting

```kotlin
fun formatDate(date: LocalDateTime?, pattern: String = DEFAULT_DATE_FORMAT): String?
fun formatDate(date: ZonedDateTime?, pattern: String = DEFAULT_DATE_FORMAT): String?
fun formatDateLocalized(date: LocalDateTime?, dateStyle: Int = DateFormat.SHORT): String?
```

#### Parsing

```kotlin
fun parseDate(dateString: String?, pattern: String = DEFAULT_DATE_FORMAT): LocalDateTime?
fun parseIsoDate(isoDateString: String?): ZonedDateTime?
fun parseDateSafe(dateString: String?, pattern: String = DEFAULT_DATE_FORMAT): LocalDateTime?
```

#### Date Operations

```kotlin
fun addDays(date: LocalDateTime, days: Long): LocalDateTime
fun isPast(date: LocalDateTime?): Boolean
fun daysBetween(startDate: LocalDateTime?, endDate: LocalDateTime?): Long
```

### Constants

```kotlin
const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
const val ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
```

## Dependencies

- Java 8 Time API
- Android Core KTX (for extension functions)

## See Also

- [DateHelperUtil-Room](../DateHelperUtil-Room/README.md) - Room database integration
- [DateHelperUtil-Serialization](../DateHelperUtil-Serialization/README.md) - Kotlinx Serialization support
