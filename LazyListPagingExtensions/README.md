# LazyListPagingExtensions

Extensions for Jetpack Compose LazyList with enhanced paging support, error handling, and loading states.

## Features

- Simplified integration of Jetpack Paging with Compose LazyList
- Built-in loading indicators and error states
- Empty state handling
- Support for placeholders
- Automatic retry mechanisms
- Clean DSL-style API

## Installation

```gradle.kts
implementation("com.github.appoly.AppolyDroid-Toolbox:LazyListPagingExtensions:1.0.12")
```

## Usage

### Basic Implementation

```kotlin
@Composable
fun ItemsList(viewModel: MyViewModel) {
    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        lazyPagingItemsWithStates(
            lazyPagingItems = lazyPagingItems,
            emptyText = { "No items found" },
            errorText = { error -> "Error: ${error.message}" },
            itemContent = { item ->
                ItemRow(item = item)
            },
            itemPlaceholderContent = {
                ItemLoadingPlaceholder()
            }
        )
    }
}
```

### Full Example with All Options

```kotlin
@Composable
fun CompleteItemsList(viewModel: MyViewModel) {
    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        lazyPagingItemsWithStates(
            lazyPagingItems = lazyPagingItems,
            usingPlaceholders = true,
            loadingContent = {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            },
            emptyContent = {
                item {
                    EmptyState(
                        message = "No items found",
                        modifier = Modifier.fillParentMaxSize()
                    )
                }
            },
            emptyText = { "No items found" },
            errorContent = { error ->
                item {
                    ErrorState(
                        error = error,
                        onRetry = { lazyPagingItems.retry() },
                        modifier = Modifier.fillParentMaxSize()
                    )
                }
            },
            errorText = { error -> "Failed to load items: ${error.message}" },
            loadStateFooterContent = { loadState ->
                when (loadState) {
                    is LoadState.Loading -> {
                        item {
                            LoadingFooter()
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            ErrorFooter(
                                error = loadState.error,
                                onRetry = { lazyPagingItems.retry() }
                            )
                        }
                    }
                    else -> {}
                }
            },
            itemKey = { index -> lazyPagingItems[index]?.id ?: index },
            itemContentType = { "ItemType" },
            itemContent = { item ->
                ItemRow(item = item)
            },
            itemPlaceholderContent = {
                ItemPlaceholder()
            }
        )
    }
}
```

### Simple Items List with Basic Implementation

```kotlin
@Composable
fun SimpleItemsList(viewModel: MyViewModel) {
    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        lazyPagingItems(
            lazyPagingItems = lazyPagingItems,
            itemContent = { item ->
                ItemRow(item = item)
            }
        )
    }
}
```

### Using Custom Header and Footer

```kotlin
@Composable
fun ItemsListWithHeader(viewModel: MyViewModel) {
    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        item {
            Text(
                text = "My Items",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        lazyPagingItemsWithStates(
            lazyPagingItems = lazyPagingItems,
            itemContent = { item ->
                ItemRow(item = item)
            }
        )
        
        item {
            Text(
                text = "End of list",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
```

### Handling Append States for Infinite Scrolling

```kotlin
@Composable
fun InfiniteScrollingList(viewModel: MyViewModel) {
    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        lazyPagingItemsWithStates(
            lazyPagingItems = lazyPagingItems,
            itemContent = { item ->
                ItemRow(item = item)
            },
            loadStateFooterContent = { loadState ->
                when (loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Failed to load more items")
                                Button(onClick = { lazyPagingItems.retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is LoadState.NotLoading -> {}
                }
            }
        )
    }
}
```

## API Reference

### Main Extension Functions

```kotlin
fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
    lazyPagingItems: LazyPagingItems<T>,
    usingPlaceholders: Boolean = false,
    loadingContent: (LazyListScope.() -> Unit)? = null,
    emptyContent: (LazyListScope.() -> Unit)? = null,
    emptyText: @Composable () -> String = { "No items found" },
    errorContent: (LazyListScope.(LoadState.Error) -> Unit)? = null,
    errorText: @Composable (LoadState.Error) -> String = { it.error.message ?: "Unknown error" },
    loadStateHeaderContent: (LoadState) -> LazyListScope.() -> Unit = { _ -> ({}) },
    loadStateFooterContent: (LoadState) -> LazyListScope.() -> Unit = { _ -> ({}) },
    itemKey: ((index: Int) -> Any)? = null,
    itemContentType: ((index: Int) -> Any?)? = null,
    itemContent: @Composable BoxScope.(item: T) -> Unit,
    itemPlaceholderContent: @Composable BoxScope.() -> Unit = {}
)

fun <T : Any> LazyListScope.lazyPagingItems(
    lazyPagingItems: LazyPagingItems<T>,
    key: ((index: Int) -> Any)? = null,
    contentType: ((index: Int) -> Any?)? = null,
    itemContent: @Composable BoxScope.(item: T) -> Unit
)
```

### Helper Extension Functions

```kotlin
fun <T : Any> LazyPagingItems<T>.isEmpty(): Boolean
fun <T : Any> LazyPagingItems<T>.isError(): Boolean
val <T : Any> LazyPagingItems<T>.firstError: Throwable?
```

## Example ViewModel Setup

```kotlin
class MyViewModel : ViewModel() {
    private val dataRepo: DataRepo = // get repository instance
    
    private val pagingSourceFactory = dataRepo.getItemsPagingSourceFactory(
        searchQuery = "",
        pageSize = 20,
        jumpingSupported = true
    )
    
    val pagingDataFlow = pagingSourceFactory
        .getPager(enablePlaceholders = true)
        .flow
        .cachedIn(viewModelScope)
    
    fun refresh() {
        pagingSourceFactory.invalidate()
    }
}
```

## Real-world Example

```kotlin
LazyColumn {
    // Show local items that are being uploaded
    if (localItems.isNotEmpty()) {
        item(key = "uploading_header") {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(R.string.uploading),
                style = AppTextStyle.Body2,
                color = Grey.c600
            )
        }
        
        items(
            items = localItems,
            key = { item -> item.itemId }
        ) { item ->
            when (item) {
                is LocalItem.Item -> {
                    LocalItemRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        item = item.item,
                        onRemove = {
                            viewModel.removeLocalItem(context, item.item)
                        }
                    )
                }
                is LocalItem.Placeholder -> {
                    LocalItemPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }
        }
        
        item(key = "uploading_footer") {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
    
    // Show remote items with paging support
    lazyPagingItemsWithStates(
        lazyPagingItems = itemsPagingItems,
        usingPlaceholders = true,
        emptyText = { stringResource(R.string.no_items_yet) },
        errorText = { error -> 
            stringResource(R.string.error_loading_items, error.error.message ?: "Unknown error") 
        },
        itemContent = { item ->
            ItemRow(
                item = item,
                onClick = { selectedItem = item },
                onDelete = { showDeleteConfirmFor = item }
            )
        },
        itemPlaceholderContent = {
            ItemPlaceholder()
        }
    )
}
```

## Dependencies

- Jetpack Compose
- [Jetpack Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
- [BaseRepo-Paging](../BaseRepo-Paging/README.md) (optional but recommended)

## See Also

- [LazyGridPagingExtensions](../LazyGridPagingExtensions/README.md) - Similar extensions for LazyGrid
- [BaseRepo-Paging](../BaseRepo-Paging/README.md) - Paging source implementations
