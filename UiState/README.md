# UiState

A lightweight UI state management module for Android applications built with Jetpack Compose.

## Features

- Simple, consistent UI state representation
- Type-safe state management
- Loading, Error, Success, and Idle states
- Easy integration with ViewModels and Compose
- Support for state identification with keys

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
                _uiState.value = UiState.Success()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load data: ${e.message}")
            }
        }
    }
}
```

### Using with Keys

Keys allow you to identify specific states when handling multiple operations:

```kotlin
// In your ViewModel with multiple operations
class ProductsViewModel : ViewModel() {
    private var _uiState = mutableStateOf<UiState>(UiState.Idle())
    val uiState: State<UiState> = _uiState

    fun loadProducts() {
        _uiState.value = UiState.Loading(key = "products")
        viewModelScope.launch {
            try {
                repository.getProducts()
                _uiState.value = UiState.Success(key = "products")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load products", key = "products")
            }
        }
    }
    
    fun deleteProduct(id: String) {
        _uiState.value = UiState.Loading(key = "delete_$id")
        viewModelScope.launch {
            try {
                repository.deleteProduct(id)
                _uiState.value = UiState.Success(key = "delete_$id")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to delete product", key = "delete_$id")
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
        is UiState.Success -> SuccessContent()
        is UiState.Idle -> InitialContent()
    }
}
```

### Using Extension Functions

The library provides useful extension functions with Kotlin contracts for type safety:

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val uiState = viewModel.uiState.value
    
    // Show loading indicator if loading
    if (uiState.isLoading()) {
        LoadingIndicator()
    }
    
    // Show content when not loading
    if (uiState.isNotLoading()) {
        SomeContent()
    }
    
    // Show error if error
    if (uiState.isError()) {
        ErrorMessage((uiState as UiState.Error).message)
    }
    
    // Show success content if success
    if (uiState.isSuccess()) {
        SuccessContent()
    }
    
    // Check if state is idle
    if (uiState.isIdle()) {
        IdleContent()
    }
    
    // Check if state is not an error
    if (uiState.isNotError()) {
        NonErrorContent()
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
                repository.fetchData()
                uiState = uiState.copy(state = UiState.Success())
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
                userRepository.getUser(userId)
                userState = UiState.Success()
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
                postsRepository.getPosts(userId)
                postsState = UiState.Success()
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
    abstract val key: Any?
    
    data class Idle(override val key: Any? = null) : UiState()
    data class Loading(override val key: Any? = null) : UiState()
    data class Success(override val key: Any? = null) : UiState()
    data class Error(val message: String, override val key: Any? = null) : UiState()
}
```

### Extension Functions

```kotlin
// State type checking with Kotlin contracts for type safety
fun UiState?.isIdle(): Boolean
fun UiState?.isLoading(): Boolean
fun UiState?.isNotLoading(): Boolean
fun UiState?.isError(): Boolean
fun UiState?.isNotError(): Boolean
fun UiState?.isSuccess(): Boolean
```

## Dependencies

- Kotlin Coroutines
- Kotlin Contracts (for type-safe extension functions)

## See Also

- [AppSnackBar-UiState](../AppSnackBar-UiState/README.md) for integration with AppSnackBar module

