# DateHelperUtil-Room

Extension module for DateHelperUtil that provides Room database integration for date and time types.

## Features

- Room TypeConverters for LocalDateTime, ZonedDateTime, and other date types
- Seamless integration with Room database entities
- Consistent date/time serialization and deserialization

## Installation

```gradle.kts
// Requires base DateHelperUtil module
implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil:1.0.12")
implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil-Room:1.0.12")
```

## Usage

### Setting Up Room Type Converters

Add the converters to your Room database:

```kotlin
@Database(
    entities = [UserEntity::class],
    version = 1
)
@TypeConverters(
    DBDateConverters::class // Include the DBDateConverters class for date handling
) 
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val postDao: PostDao
}
```

### Using Date Types in Entities

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    // LocalDateTime will be automatically converted to/from String in the database
    val createdAt: LocalDateTime,
    val lastLogin: ZonedDateTime?
)
```

### Queries with Date Types

Room queries will now work with LocalDateTime and ZonedDateTime:

```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE createdAt > :date")
    fun getUsersCreatedAfter(date: LocalDateTime): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users ORDER BY lastLogin DESC")
    fun getUsersByLastLogin(): Flow<List<UserEntity>>
}
```

### Implementation Example

```kotlin
// Example repository using Room with DateHelperUtil-Room
class UserRepository(private val userDao: UserDao) {
    fun getUsersCreatedToday(): Flow<List<UserEntity>> {
        val startOfToday = LocalDate.now().atStartOfDay()
        return userDao.getUsersCreatedAfter(startOfToday)
    }
    
    suspend fun updateLastLogin(userId: Long) {
        userDao.updateLastLogin(userId, ZonedDateTime.now())
    }
}
```

## API Reference

### DBDateConverters

The main class containing type converters for Room:

```kotlin
class DBDateConverters {
    @TypeConverter
    fun localDateTimeToJson(date: LocalDateTime?): String?
    
    @TypeConverter
    fun jsonToLocalDateTime(json: String?): LocalDateTime?
    
    @TypeConverter
    fun localDateToJson(date: LocalDate?): String?
    
    @TypeConverter
    fun jsonToLocalDate(json: String?): LocalDate?
    
    @TypeConverter
    fun zonedDateTimeToJson(date: ZonedDateTime?): String?
    
    @TypeConverter
    fun jsonToZonedDateTime(json: String?): ZonedDateTime?
}
```

## Dependencies

- [DateHelperUtil](../DateHelperUtil/README.md) module
- [Room persistence library](https://developer.android.com/jetpack/androidx/releases/room)

## Notes

- The date and time values are stored as ISO-8601 formatted strings in the database
- Time zone information is preserved for ZonedDateTime values
- The converters handle null values gracefully
