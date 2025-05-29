# UiState

A lightweight UI state management module for Android applications built with Jetpack Compose.

## Features

- Simple, consistent UI state representation
- Type-safe state management
- Loading, Error, Success, and Idle states
- Easy integration with ViewModels and Compose
- Smooth integration with [FlexiLogger](https://github.com/projectdelta6/FlexiLogger) for debug logging

## Installation

```gradle.kts
implementation("com.github.appoly.AppolyDroid-Toolbox:UiState:1.0.12")
```

## Usage

### Basic State Management

```kotlin
// In your ViewModel
class MyViewModel : ViewModel() {
    private var _uiState = mutableStateOf<UiState>(UiState.Idle())
    val uiState: State<UiState> = _uiState

    fun loadData() {
        _uiState.value = UiState.Loading()
        viewModelScope.launch {
            try {
                val result = repository.fetchData()
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load data: ${e.message}")
            }
        }
    }
}
```

### Integrating with Compose UI

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val uiState = viewModel.uiState.value
    
    when (uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorMessage(uiState.message)
        is UiState.Success -> {
            val data = uiState.data
            SuccessContent(data)
        }
        is UiState.Idle -> {
            // Show initial state
            InitialContent()
        }
    }
}
```

### Using Extension Functions

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val uiState = viewModel.uiState.value
    
    // Show loading indicator if loading
    if (uiState.isLoading()) {
        LoadingIndicator()
    }
    
    // Show error if error
    uiState.errorMessage()?.let { message ->
        ErrorMessage(message)
    }
    
    // Access success data safely
    uiState.successDataOrNull<MyData>()?.let { data ->
        SuccessContent(data)
    }
}
```

### With State Hoisting Pattern

```kotlin
// In a ViewModel or other state holder
data class MyScreenState(
    val state: UiState = UiState.Idle()
)

class MyViewModel : ViewModel() {
    var uiState by mutableStateOf(MyScreenState())
        private set
        
    fun loadData() {
        uiState = uiState.copy(state = UiState.Loading())
        viewModelScope.launch {
            try {
                val result = repository.fetchData()
                uiState = uiState.copy(state = UiState.Success(result))
            } catch (e: Exception) {
                uiState = uiState.copy(state = UiState.Error("Failed to load: ${e.message}"))
            }
        }
    }
    
    fun resetState() {
        uiState = uiState.copy(state = UiState.Idle())
    }
}
```

### Handling Multiple States

```kotlin
class UserProfileViewModel : ViewModel() {
    var userState by mutableStateOf<UiState>(UiState.Idle())
        private set
        
    var postsState by mutableStateOf<UiState>(UiState.Idle())
        private set
        
    fun loadUserProfile(userId: String) {
        userState = UiState.Loading()
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(userId)
                userState = UiState.Success(user)
                loadUserPosts(userId)
            } catch (e: Exception) {
                userState = UiState.Error("Failed to load user: ${e.message}")
            }
        }
    }
    
    fun loadUserPosts(userId: String) {
        postsState = UiState.Loading()
        viewModelScope.launch {
            try {
                val posts = postsRepository.getPosts(userId)
                postsState = UiState.Success(posts)
            } catch (e: Exception) {
                postsState = UiState.Error("Failed to load posts: ${e.message}")
            }
        }
    }
}
```

## API Reference

### UiState

The core sealed class representing UI state:

```kotlin
sealed class UiState {
    data class Idle(val message: String? = null) : UiState()
    data class Loading(val message: String? = null) : UiState()
    data class Error(val message: String? = null) : UiState()
    data class Success(val data: Any? = null) : UiState()
}
```

### Extension Functions

```kotlin
fun UiState.isLoading(): Boolean
fun UiState.isError(): Boolean 
fun UiState.isSuccess(): Boolean
fun UiState.isIdle(): Boolean
fun UiState.errorMessage(): String?
fun <T> UiState.successDataOrNull(): T?
```

### Usage with APIFlowState

The UiState module pairs well with APIFlowState from the BaseRepo module:

```kotlin
fun mapAPIFlowStateToUiState(apiFlowState: APIFlowState<*>): UiState {
    return when (apiFlowState) {
        is APIFlowState.Loading -> UiState.Loading()
        is APIFlowState.Error -> UiState.Error(apiFlowState.message)
        is APIFlowState.Success -> UiState.Success(apiFlowState.data)
    }
}
```

## Dependencies

- Kotlin Coroutines
- Optional integration with [FlexiLogger](https://github.com/projectdelta6/FlexiLogger)

## See Also

- [AppSnackBar-UiState](../AppSnackBar-UiState/README.md) for integration with AppSnackBar module
