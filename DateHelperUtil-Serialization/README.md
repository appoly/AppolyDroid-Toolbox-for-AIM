# DateHelperUtil-Serialization

Extension module for DateHelperUtil that provides Kotlinx Serialization support for date and time types.

## Features

- Custom serializers for LocalDateTime, ZonedDateTime, and other date types
- Seamless integration with Kotlinx Serialization for API requests/responses
- Consistent date/time serialization and deserialization across your app

## Installation

```gradle.kts
// Requires base DateHelperUtil module
implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil:1.0.12")
implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil-Serialization:1.0.12")
```

## Usage

### Setting Up JSON Configuration

Configure your JSON serialization to use the date serializers:

```kotlin
val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    // Additional configuration...
}
```

### Using in Data Classes

```kotlin
@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val lastLogin: ZonedDateTime?
)
```

### Retrofit Integration

When setting up Retrofit with Kotlinx Serialization:

```kotlin
Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(
        json.asConverterFactory("application/json".toMediaType())
    )
    // Additional configuration...
    .build()
```

### API Request/Response Example

```kotlin
@Serializable
data class EventRequest(
    val title: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startTime: LocalDateTime,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val endTime: ZonedDateTime
)

// API interface
interface EventApi {
    @POST("events")
    suspend fun createEvent(
        @Body event: EventRequest
    ): Response<EventResponse>
}
```

### Manual Serialization Example

```kotlin
// Manually serialize an object
val event = EventDto(
    title = "Team Meeting",
    startTime = LocalDateTime.now().plusDays(1),
    endTime = ZonedDateTime.now().plusDays(1).plusHours(1)
)

val jsonString = json.encodeToString(EventDto.serializer(), event)

// Deserialize from JSON
val parsedEvent = json.decodeFromString(EventDto.serializer(), jsonString)
```

## API Reference

### Serializer Classes

```kotlin
object LocalDateTimeSerializer : KSerializer<LocalDateTime>
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime>
object LocalDateSerializer : KSerializer<LocalDate>
object LocalTimeSerializer : KSerializer<LocalTime>
```

### Extension Methods

```kotlin
// Extensions to help with JSON encoding/decoding
fun Json.encodeToJsonElement(serializer: SerializationStrategy<*>, value: Any): JsonElement
inline fun <reified T> Json.decodeFromJsonElement(element: JsonElement): T
```

## Dependencies

- [DateHelperUtil](../DateHelperUtil/README.md) module
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) library

## Notes

- All date/time types are serialized in ISO-8601 format for maximum compatibility
- Time zone information is preserved for ZonedDateTime values
- The serializers handle null values gracefully
