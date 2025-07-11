# LazyListPagingExtensions

Extension functions for easy integration of Jetpack Paging 3 with Compose LazyColumn and LazyRow components.

## Features

- Easy-to-use extensions for LazyColumn and LazyRow
- Simplified state handling for loading, error and empty states
- Automatic placeholder management
- Customizable UI through CompositionLocal providers
- Supports standard Jetpack Paging patterns

## Installation

```gradle.kts
// Requires the base PagingExtensions module
implementation("com.github.appoly.AppolyDroid-Toolbox:PagingExtensions:1.0.20")
implementation("com.github.appoly.AppolyDroid-Toolbox:LazyListPagingExtensions:1.0.20")

// Make sure to include Jetpack Paging Compose
implementation("androidx.paging:paging-compose:3.3.6")
```

## Usage

### Basic Implementation

```kotlin
@Composable
fun ItemsList(viewModel: ItemsViewModel) {
    val items = viewModel.itemsFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        lazyPagingItemsWithStates(
            lazyPagingItems = items,
            emptyText = { "No items found" },
            errorText = { error -> error.localizedMessage ?: "An error occurred" }
        ) { item ->
            // Your item composable here
            ItemRow(item = item)
        }
    }
}
```

### Advanced Implementation with Custom Keys and Placeholders

```kotlin
@Composable
fun AdvancedItemsList(viewModel: AdvancedViewModel) {
    val items = viewModel.itemsFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        lazyPagingItemsWithStates(
            lazyPagingItems = items,
            usingPlaceholders = true,  // Enable placeholders support
            emptyText = { "No items found" },
            errorText = { error -> error.localizedMessage ?: "An error occurred" },
            retry = { items.retry() },  // Custom retry action
            itemKey = { it.id },  // Custom key function
            itemContentType = { it.type },  // Content type for recycling
            placeholderItemContent = {
                // Custom placeholder UI
                ItemPlaceholder()
            }
        ) { item ->
            // Item UI
            ItemRow(item = item)
        }
    }
}
```

### Custom Loading, Error, and Empty States

```kotlin
@Composable
fun CustomStatesItemsList(viewModel: ItemsViewModel) {
    val items = viewModel.itemsFlow.collectAsLazyPagingItems()
    
    // Provide custom UI providers
    CompositionLocalProvider(
        LocalLoadingState provides MyLoadingStateProvider(),
        LocalErrorState provides MyErrorStateProvider(),
        LocalEmptyState provides MyEmptyStateProvider()
    ) {
        LazyColumn {
            lazyPagingItemsWithStates(
                lazyPagingItems = items,
                emptyText = { "No items found" },
                errorText = { error -> error.localizedMessage ?: "An error occurred" }
            ) { item ->
                ItemRow(item = item)
            }
        }
    }
}
```

### Manual Items Content

If you need more control over how items are rendered, you can use the itemsContent parameter:

```kotlin
@Composable
fun CustomItemsList(viewModel: ItemsViewModel) {
    val items = viewModel.itemsFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        lazyPagingItemsWithStates(
            lazyPagingItems = items,
            emptyText = { "No items found" },
            errorText = { error -> "Error: ${error.localizedMessage}" }
        ) { lazyPagingItems ->
            // Custom items rendering logic
            items(lazyPagingItems.itemCount) { index ->
                val item = lazyPagingItems[index]
                if (item != null) {
                    ItemRow(item = item)
                } else {
                    ItemPlaceholder()
                }
            }
        }
    }
}
```

### Custom Error Handling

```kotlin
@Composable
fun ErrorHandlingList(viewModel: ItemsViewModel) {
    val items = viewModel.itemsFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        lazyPagingItemsWithStates(
            lazyPagingItems = items,
            emptyContent = {
                item(key = "empty") {
                    Text(
                        text = "No items found",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            },
            errorContent = { errorType, error ->
                item(key = "${errorType}_error") {
                    when (errorType) {
                        PagingErrorType.REFRESH -> FullScreenError(error, items::retry)
                        PagingErrorType.APPEND -> AppendError(error, items::retry)
                        PagingErrorType.PREPEND -> PrependError(error, items::retry)
                    }
                }
            }
        ) { item ->
            ItemRow(item = item)
        }
    }
}
```

## Main Functions

### lazyPagingItems

Simplified way to add items from a LazyPagingItems instance to a LazyColumn/LazyRow.

```kotlin
fun <T : Any> LazyListScope.lazyPagingItems(
    lazyPagingItems: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    contentType: (item: T) -> Any? = { null },
    placeholderItemContent: @Composable (LazyItemScope.() -> Unit) = {},
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
)
```

### lazyPagingItemsWithStates

Complete solution that handles all states (loading, error, empty) and the items.

```kotlin
fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
    lazyPagingItems: LazyPagingItems<T>,
    usingPlaceholders: Boolean = false,
    emptyText: @Composable () -> String,
    errorText: @Composable (LoadState.Error) -> String,
    retry: () -> Unit = { lazyPagingItems.retry() },
    itemKey: ((item: T) -> Any)? = null,
    itemContentType: (item: T) -> Any? = { null },
    itemPlaceholderContent: @Composable (LazyItemScope.() -> Unit) = {},
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
)
```

## Dependencies

- [PagingExtensions](../PagingExtensions/README.md) - Core paging utility module
- [Jetpack Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) - Android paging library
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android UI toolkit
