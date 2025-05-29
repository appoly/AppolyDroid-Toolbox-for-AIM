# BaseRepo

Foundation module for implementing the repository pattern with standardized API call handling.

## Features

- Standardized API call handling with error management
- Support for coroutines and Flow
- Integration with [FlexiLogger](https://github.com/projectdelta6/FlexiLogger)
- Built-in retry mechanisms
- Refreshable data flows

## Installation

```gradle.kts
implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo:1.0.12")
```

## Usage

### Basic Repository Setup

Create a base repository class that extends `AppolyBaseRepo`:

```kotlin
abstract class BaseRepo : AppolyBaseRepo({ RetrofitClient }), KoinComponent {
    override val logger: FlexiLog = Log
    protected val prefsHelper: PrefsHelper by inject()
    protected val db: AppDatabase by inject()

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val SEARCH_DEBOUNCE = 300L
    }
}
```

### Making API Calls

Use the `doAPICall` method for standardized API request handling:

```kotlin
suspend fun fetchUser(userId: Int): APIResult<UserData> = doAPICall("fetchUser") {
    userService.api.getUser(userId)
}
```

### Using with Flow

Convert API calls to Flow:

```kotlin
fun getUserFlow(userId: Int): Flow<APIFlowState<UserData>> = flow {
    emit(APIFlowState.Loading)
    emit(fetchUser(userId).asApiFlowState())
}
```

### Creating Refreshable API Flows

Create a refreshable data flow that can be manually refreshed:

```kotlin
val userDataRefreshFlow = RefreshableAPIFlow(
    apiCall = { fetchUser(userId) },
    scope = viewModelScope
)

val userDataState: StateFlow<APIFlowState<UserData>> = userDataRefreshFlow.stateIn(viewModelScope)

// To refresh:
userDataRefreshFlow.refresh()
```

### Processing API Results

Process API results with extension functions:

```kotlin
fetchUser(userId).onSuccess { user ->
    // Handle success
    println("User: ${user.name}")
}.onError { error ->
    // Handle error
    println("Error: ${error.message}")
}
```

Or using when expression:

```kotlin
when (val result = fetchUser(userId)) {
    is APIResult.Success -> {
        val user = result.data
        // Use user data
    }
    is APIResult.Error -> {
        val errorMessage = result.message
        // Handle error
    }
}
```

### Using the Result Extensions

```kotlin
// Get success data or null
val userData = fetchUser(userId).successOrNull()

// Transform success data
val userName = fetchUser(userId).mapSuccess { it.name }

// Check state
val isLoading = userDataState.isLoading()
val hasError = userDataState.isError()
val errorMsg = userDataState.errorMessage()
```

## API Reference

### Core Classes

#### `AppolyBaseRepo`
Base class for repositories with API handling capabilities.

#### `APIResult<T>`
Sealed class representing an API result:
- `APIResult.Success<T>`: Contains successful data
- `APIResult.Error`: Contains error information

#### `APIFlowState<T>`
Sealed class representing a Flow API state:
- `APIFlowState.Loading`: Loading state
- `APIFlowState.Success<T>`: Success state with data
- `APIFlowState.Error`: Error state with message

#### `RefreshableAPIFlow<T>`
A Flow wrapper that can be refreshed on demand.

### Key Methods

#### `doAPICall`
```kotlin
suspend fun <T> doAPICall(tag: String, apiCall: suspend () -> ApiResponse<GenericResponse<T>>): APIResult<T>
```

#### `doAPICallWithBaseResponse`
```kotlin
suspend fun doAPICallWithBaseResponse(tag: String, apiCall: suspend () -> ApiResponse<BaseResponse>): APIResult<BaseResponse>
```

## Dependencies

- [FlexiLogger](https://github.com/projectdelta6/FlexiLogger) - For logging
- [Sandwich](https://github.com/skydoves/sandwich) - For API response handling
- Kotlin Coroutines and Flow

## See Also

- [BaseRepo-Paging](../BaseRepo-Paging/README.md) - Adds Jetpack Paging support
- [BaseRepo-S3Uploader](../BaseRepo-S3Uploader/README.md) - Adds S3 upload capabilities
