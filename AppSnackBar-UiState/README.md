# AppSnackBar-UiState

Integration module that combines the AppSnackBar module with UiState for a seamless error and notification handling system in Android applications.

## Features

- Automatic Snackbar display based on UiState changes
- Handles error states automatically with styled error Snackbars
- Support for different message types (Info, Success, Error)
- Easy integration with existing UiState and AppSnackBar implementations
- Clean separation of UI state and notification display logic

## Installation

```gradle.kts
// Requires base modules
implementation("com.github.appoly.AppolyDroid-Toolbox:UiState:1.0.12")
implementation("com.github.appoly.AppolyDroid-Toolbox:AppSnackBar:1.0.12")
implementation("com.github.appoly.AppolyDroid-Toolbox:AppSnackBar-UiState:1.0.12")
```

## Usage

### Basic Integration

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = viewModel.uiState
    
    // Automatically show snackbar when UI state changes to error
    LaunchedEffect(key1 = uiState.state) {
        if (uiState.state.isError()) {
            // Extension function from AppSnackBar-UiState
            snackbarHostState.showSnackbar(
                message = uiState.state.errorMessage() ?: "An error occurred",
                duration = SnackbarDuration.Long,
                type = SnackBarType.Error
            )
            // Reset UI state after showing error
            viewModel.resetState()
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                AppSnackBar(snackbarData = data)
            }
        }
    ) {
        // Your content
    }
}
```

### Comprehensive Example

```kotlin
@Composable
fun MyAppScreen(viewModel: MyViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = viewModel.uiState
    val state = uiState.state
    
    // Handle different UI states
    if (state.isError()) {
        LaunchedEffect(key1 = state) {
            snackbarHostState.showSnackbar(
                message = state.errorMessage() ?: "Unknown error",
                duration = SnackbarDuration.Long,
                type = SnackBarType.Error
            )
            viewModel.resetState()
        }
    } else if (state is UiState.Success) {
        LaunchedEffect(key1 = state) {
            snackbarHostState.showSnackbar(
                message = "Operation completed successfully!",
                duration = SnackbarDuration.Short,
                type = SnackBarType.Success
            )
            viewModel.resetState()
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
            ) { data ->
                AppSnackBar(snackbarData = data)
            }
        }
    ) {
        // Your app content
        if (state.isLoading()) {
            LoadingIndicator()
        } else {
            MainContent(
                onButtonClick = {
                    viewModel.performAction()
                }
            )
        }
    }
}
```

### Using with Multiple UI States

```kotlin
@Composable
fun ComplexScreen(
    userViewModel: UserViewModel,
    postViewModel: PostViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val userUiState = userViewModel.uiState.state
    val postUiState = postViewModel.uiState.state
    
    // Handle user state errors
    LaunchedEffect(userUiState) {
        if (userUiState.isError()) {
            snackbarHostState.showSnackbar(
                message = "User error: ${userUiState.errorMessage()}",
                duration = SnackbarDuration.Long,
                type = SnackBarType.Error
            )
            userViewModel.resetState()
        }
    }
    
    // Handle post state errors
    LaunchedEffect(postUiState) {
        if (postUiState.isError()) {
            snackbarHostState.showSnackbar(
                message = "Post error: ${postUiState.errorMessage()}",
                duration = SnackbarDuration.Long,
                type = SnackBarType.Error
            )
            postViewModel.resetState()
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                AppSnackBar(snackbarData = data)
            }
        }
    ) {
        // Complex screen content
    }
}
```

### Integration with API Calls

```kotlin
class MyViewModel : ViewModel() {
    var uiState by mutableStateOf(UiScreenState())
        private set
        
    fun fetchData() {
        uiState = uiState.copy(state = UiState.Loading())
        viewModelScope.launch {
            try {
                val result = repository.fetchData()
                uiState = uiState.copy(
                    state = UiState.Success(result),
                    message = "Data loaded successfully"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    state = UiState.Error(e.message ?: "Unknown error")
                )
            }
        }
    }
    
    fun resetState() {
        uiState = uiState.copy(state = UiState.Idle())
    }
}

data class UiScreenState(
    val state: UiState = UiState.Idle(),
    val message: String? = null
)
```

## Extension Functions

### Core Extensions

```kotlin
// Extension property to convert UiState to appropriate SnackBarType
val UiState?.snackBarType: SnackBarType
    get() = when (this) {
        is UiState.Success -> SnackBarType.Success
        is UiState.Error -> SnackBarType.Error
        else -> SnackBarType.Info
    }

// Helper function to show snackbar based on UiState
suspend fun SnackbarHostState.showSnackbarForUiState(
    uiState: UiState,
    resetState: () -> Unit
): SnackbarResult? {
    return when (uiState) {
        is UiState.Error -> {
            val result = showSnackbar(
                message = uiState.message ?: "An error occurred",
                duration = SnackbarDuration.Long,
                type = SnackBarType.Error
            )
            resetState()
            result
        }
        is UiState.Success -> {
            if (uiState.message != null) {
                val result = showSnackbar(
                    message = uiState.message,
                    duration = SnackbarDuration.Short,
                    type = SnackBarType.Success
                )
                resetState()
                result
            } else null
        }
        else -> null
    }
}
```

## Dependencies

- [UiState](../UiState/README.md) module
- [AppSnackBar](../AppSnackBar/README.md) module
- Jetpack Compose

## Integration with APIFlowState

This module works well with APIFlowState from the BaseRepo module:

```kotlin
@Composable
fun ApiScreen(viewModel: ApiViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val apiState by viewModel.dataFlow.collectAsState()
    
    LaunchedEffect(apiState) {
        when (apiState) {
            is APIFlowState.Error -> {
                val errorState = apiState as APIFlowState.Error
                snackbarHostState.showSnackbar(
                    message = errorState.message ?: "API Error",
                    duration = SnackbarDuration.Long,
                    type = SnackBarType.Error
                )
            }
            is APIFlowState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Data loaded successfully",
                    duration = SnackbarDuration.Short,
                    type = SnackBarType.Success
                )
            }
            else -> {}
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                AppSnackBar(snackbarData = data)
            }
        }
    ) {
        // Content
    }
}
```
