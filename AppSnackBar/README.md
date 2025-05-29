# AppSnackBar

Enhanced Snackbar implementation for Android applications with Jetpack Compose, providing styled notifications with different types and customization options.

## Features

- Pre-styled Snackbars for different message types (Info, Success, Warning, Error)
- Custom icons and colors for each Snackbar type
- Support for action buttons
- Easy integration with Jetpack Compose
- Customizable appearance
- Accessible design

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
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) { snackbarData ->
                // Use the custom AppSnackBar instead of default Snackbar
                AppSnackBar(snackbarData = snackbarData)
            }
        }
    ) {
        // Your content
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
                AppSnackBar(
                    snackbarData = snackbarData,
                    // Get type from snackbarData extras if available
                    type = snackbarData.visuals.extras?.get("type") as? SnackBarType
                        ?: SnackBarType.Info
                )
            }
        }
    ) {
        Column {
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
                        message = "Warning: This action has consequences",
                        duration = SnackbarDuration.Long,
                        type = SnackBarType.Warning
                    )
                }
            }) {
                Text("Show Warning Snackbar")
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
    ) {
        Button(onClick = {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Item deleted",
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Long,
                    type = SnackBarType.Warning
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
```

### Extension Function for Easy Access

```kotlin
// Extension function to show snackbar with type
suspend fun SnackbarHostState.showSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    type: SnackBarType = SnackBarType.Info
): SnackbarResult {
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration,
        withDismissAction = false,
        SnackbarVisuals(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
            extras = mapOf("type" to type)
        )
    )
}
```

### Customizing AppSnackBar

```kotlin
@Composable
fun MyScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { snackbarData ->
                AppSnackBar(
                    snackbarData = snackbarData,
                    modifier = Modifier.padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                    iconSize = 20.dp
                )
            }
        }
    ) {
        // Your content
    }
}
```

## API Reference

### AppSnackBar

```kotlin
@Composable
fun AppSnackBar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    type: SnackBarType = SnackBarType.Info,
    dismissAction: @Composable (() -> Unit)? = null,
    actionOnNewLine: Boolean = false,
    shape: Shape = SnackbarDefaults.shape,
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = SnackbarDefaults.contentColor,
    actionColor: Color = SnackbarDefaults.actionColor,
    iconSize: Dp = 24.dp
)
```

### SnackBarType

```kotlin
enum class SnackBarType {
    Info,
    Success,
    Warning,
    Error
}
```

### Extension Functions

```kotlin
suspend fun SnackbarHostState.showSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    type: SnackBarType = SnackBarType.Info
): SnackbarResult
```

## Dependencies

- Jetpack Compose Material3
- [FlexiLogger](https://github.com/projectdelta6/FlexiLogger) (optional for logging)

## See Also

- [AppSnackBar-UiState](../AppSnackBar-UiState/README.md) - Integration with UiState module
