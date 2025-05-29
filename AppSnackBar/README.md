# AppSnackBar

Enhanced Snackbar implementation for Android applications with Jetpack Compose, providing styled notifications with different types.

## Features

- Pre-styled Snackbars for different message types (Info, Success, Error)
- Custom colors for each Snackbar type
- Support for action buttons
- Easy integration with Jetpack Compose
- Customizable appearance

## Installation

```gradle.kts
implementation("com.github.appoly.AppolyDroid-Toolbox:AppSnackBar:1.0.12")
```

## Usage

### Basic Usage

```kotlin
@Composable
fun MyScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) { snackbarData ->
                // Use the custom AppSnackBar instead of default Snackbar
                AppSnackBar(snackbarData = snackbarData)
            }
        }
    ) { contentPadding ->
        // Your content
        Column(modifier = Modifier.padding(contentPadding)) {
            Button(onClick = {
                // Launch a coroutine to show snackbar
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Action performed successfully!",
                        duration = SnackbarDuration.Short
                    )
                }
            }) {
                Text("Show Basic Snackbar")
            }
        }
    }
}
```

### Using Different Snackbar Types

```kotlin
@Composable
fun MyScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                // AppSnackBar automatically handles the snackbar type from the visuals
                AppSnackBar(snackbarData = snackbarData)
            }
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            Button(onClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Information message",
                        duration = SnackbarDuration.Short,
                        type = SnackBarType.Info
                    )
                }
            }) {
                Text("Show Info Snackbar")
            }
            
            Button(onClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Operation successful!",
                        duration = SnackbarDuration.Short,
                        type = SnackBarType.Success
                    )
                }
            }) {
                Text("Show Success Snackbar")
            }
            
            Button(onClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Error: Operation failed",
                        duration = SnackbarDuration.Long,
                        type = SnackBarType.Error
                    )
                }
            }) {
                Text("Show Error Snackbar")
            }
        }
    }
}
```

### Snackbar with Action Button

```kotlin
@Composable
fun MyScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                AppSnackBar(snackbarData = snackbarData)
            }
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            Button(onClick = {
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Item deleted",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Long,
                        type = SnackBarType.Error
                    )
                    
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            // Handle undo action
                            println("Undo action performed")
                        }
                        SnackbarResult.Dismissed -> {
                            // Handle dismissal
                            println("Snackbar dismissed")
                        }
                    }
                }
            }) {
                Text("Delete Item")
            }
        }
    }
}
```

### Using Custom Colors

You can provide custom colors through the `LocalAppSnackBarColors` composition local:

```kotlin
@Composable
fun MyApp() {
    val customColors = AppSnackBarColors(
        info = Color(0xFF2196F3),    // Custom blue
        success = Color(0xFF4CAF50), // Custom green
        error = Color(0xFFE91E63)    // Custom red
    )
    
    CompositionLocalProvider(
        LocalAppSnackBarColors provides customColors
    ) {
        // Your app content
        MyScreen()
    }
}
```

## API Reference

### AppSnackBar

```kotlin
@Composable
fun AppSnackBar(
    snackbarData: SnackbarData
)
```

### SnackBarType

```kotlin
enum class SnackBarType {
    Info,
    Success,
    Error
}
```

### AppSnackBarColors

```kotlin
data class AppSnackBarColors(
    val info: Color,
    val success: Color,
    val error: Color
)
```

### Extension Functions

```kotlin
suspend fun SnackbarHostState.showSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    type: SnackBarType = SnackBarType.Info
): SnackbarResult
```

## Dependencies

- Jetpack Compose Material3

## See Also

- [AppSnackBar-UiState](../AppSnackBar-UiState/README.md) - Integration with UiState module
