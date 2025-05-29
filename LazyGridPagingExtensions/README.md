# LazyGridPagingExtensions

Extensions for Jetpack Compose LazyGrid with enhanced paging support, error handling, and loading states.

## Features

- Simplified integration of Jetpack Paging with Compose LazyGrid
- Built-in loading indicators and error states
- Empty state handling
- Support for placeholders
- Automatic retry mechanisms
- Clean DSL-style API

## Installation

```gradle.kts
implementation("com.github.appoly.AppolyDroid-Toolbox:LazyGridPagingExtensions:1.0.12")
```

## Usage

### Basic Implementation

```kotlin
@Composable
fun PhotosGrid(viewModel: PhotosViewModel) {
    val lazyPagingItems = viewModel.photosPagingFlow.collectAsLazyPagingItems()
    
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        lazyPagingItemsWithStates(
            lazyPagingItems = lazyPagingItems,
            emptyText = { "No photos found" },
            errorText = { error -> "Error: ${error.message}" },
            itemContent = { photo ->
                PhotoItem(photo = photo)
            },
            itemPlaceholderContent = {
                PhotoLoadingPlaceholder()
            }
        )
    }
}
```

### Full Example with All Options

```kotlin
@Composable
fun CompletePhotosGrid(viewModel: PhotosViewModel) {
    val lazyPagingItems = viewModel.photosPagingFlow.collectAsLazyPagingItems()
    
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        lazyPagingItemsWithStates(
            lazyPagingItems = lazyPagingItems,
            usingPlaceholders = true,
            loadingContent = {
                item(
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "loading"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            },
            emptyContent = {
                item(
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "empty"
                ) {
                    EmptyState(
                        message = "No photos found",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            },
            emptyText = { "No photos found" },
            errorContent = { error ->
                item(
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "error"
                ) {
                    ErrorState(
                        error = error,
                        onRetry = { lazyPagingItems.retry() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            },
            errorText = { error -> "Failed to load photos: ${error.message}" },
            loadStateFooterContent = { loadState ->
                when (loadState) {
                    is LoadState.Loading -> {
                        item(
                            span = { GridItemSpan(maxLineSpan) },
                            contentType = "footer_loading"
                        ) {
                            LoadingFooter()
                        }
                    }
                    is LoadState.Error -> {
                        item(
                            span = { GridItemSpan(maxLineSpan) },
                            contentType = "footer_error"
                        ) {
                            ErrorFooter(
                                error = loadState.error,
                                onRetry = { lazyPagingItems.retry() }
                            )
                        }
                    }
                    else -> {}
                }
            },
            itemSpan = { GridItemSpan(1) },
            itemKey = { index -> lazyPagingItems[index]?.id ?: index },
            itemContentType = { "photo_item" },
            itemContent = { photo ->
                PhotoItem(photo = photo)
            },
            itemPlaceholderContent = {
                PhotoLoadingPlaceholder()
            }
        )
    }
}
```

### Simple Grid with Basic Implementation

```kotlin
@Composable
fun SimplePhotosGrid(viewModel: PhotosViewModel) {
    val lazyPagingItems = viewModel.photosPagingFlow.collectAsLazyPagingItems()
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp)
    ) {
        lazyPagingItems(
            lazyPagingItems = lazyPagingItems,
            itemContent = { photo ->
                PhotoItem(photo = photo)
            }
        )
    }
}
```

### Using Custom Header and Mixed Content

```kotlin
@Composable
fun PhotosGridWithHeader(viewModel: PhotosViewModel) {
    val lazyPagingItems = viewModel.photosPagingFlow.collectAsLazyPagingItems()
    
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp)
    ) {
        item(
            span = { GridItemSpan(maxLineSpan) },
            contentType = "header"
        ) {
            Text(
                text = "Photo Gallery",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        lazyPagingItemsWithStates(
            lazyPagingItems = lazyPagingItems,
            itemContent = { photo ->
                PhotoItem(photo = photo)
            }
        )
        
        item(
            span = { GridItemSpan(maxLineSpan) },
            contentType = "footer"
        ) {
            Text(
                text = "End of gallery",
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

### Handling Different Item Spans

```kotlin
@Composable
fun DynamicSpanPhotosGrid(viewModel: PhotosViewModel) {
    val lazyPagingItems = viewModel.photosPagingFlow.collectAsLazyPagingItems()
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(4.dp)
    ) {
        lazyPagingItemsWithStates(
            lazyPagingItems = lazyPagingItems,
            itemSpan = { photo ->
                // Featured photos take up 2 columns
                GridItemSpan(if (photo.isFeatured) 2 else 1)
            },
            itemContent = { photo ->
                PhotoItem(
                    photo = photo,
                    isFeatured = photo.isFeatured
                )
            }
        )
    }
}
```

## API Reference

### Main Extension Functions

```kotlin
fun <T : Any> LazyGridScope.lazyPagingItemsWithStates(
    lazyPagingItems: LazyPagingItems<T>,
    usingPlaceholders: Boolean = false,
    loadingContent: (LazyGridScope.() -> Unit)? = null,
    emptyContent: (LazyGridScope.() -> Unit)? = null,
    emptyText: @Composable () -> String = { "No items found" },
    errorContent: (LazyGridScope.(LoadState.Error) -> Unit)? = null,
    errorText: @Composable (LoadState.Error) -> String = { it.error.message ?: "Unknown error" },
    loadStateHeaderContent: (LoadState) -> LazyGridScope.() -> Unit = { _ -> ({}) },
    loadStateFooterContent: (LoadState) -> LazyGridScope.() -> Unit = { _ -> ({}) },
    itemSpan: (T) -> GridItemSpan = { GridItemSpan(1) },
    itemKey: ((index: Int) -> Any)? = null,
    itemContentType: ((index: Int) -> Any?)? = null,
    itemContent: @Composable BoxScope.(item: T) -> Unit,
    itemPlaceholderContent: @Composable BoxScope.() -> Unit = {}
)

fun <T : Any> LazyGridScope.lazyPagingItems(
    lazyPagingItems: LazyPagingItems<T>,
    itemSpan: (T) -> GridItemSpan = { GridItemSpan(1) },
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
class PhotosViewModel : ViewModel() {
    private val photoRepo: PhotoRepository = // get repository instance
    
    private val pagingSourceFactory = photoRepo.getPhotosPagingSourceFactory(
        albumId = albumId,
        pageSize = 30,
        jumpingSupported = true
    )
    
    val photosPagingFlow = pagingSourceFactory
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
// Example media grid implementation
LazyVerticalGrid(
    modifier = Modifier.fillMaxSize(),
    columns = GridCells.Adaptive(110.dp),
    contentPadding = PaddingValues(
        top = 24.dp,
        bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        start = 19.dp,
        end = 19.dp
    ),
    verticalArrangement = Arrangement.spacedBy(3.5.dp),
    horizontalArrangement = Arrangement.spacedBy(3.5.dp)
) {
    lazyPagingItemsWithStates(
        lazyPagingItems = mediaItemsPagingItems,
        usingPlaceholders = true,
        emptyText = { stringResource(R.string.no_items_yet) },
        errorText = { error ->
            stringResource(R.string.error_loading_media, error.error.message?.ifBlank { null } ?: "Unknown error")
        },
        itemContentType = {
            "MediaItem"
        },
        itemContent = { mediaItem ->
            MediaItemTile(
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
                    .aspectRatio(1f),
                mediaItem = mediaItem,
                onClick = {
                    selectedMediaItem = mediaItem
                },
                appData = appStateData,
                onDelete = {
                    showConfirmDeleteFor = mediaItem
                }
            )
        },
        itemPlaceholderContent = {
            MediaItemPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .animateItem()
            )
        }
    )
}
```

## Dependencies

- Jetpack Compose
- [Jetpack Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
- [BaseRepo-Paging](../BaseRepo-Paging/README.md) (optional but recommended)

## See Also

- [LazyListPagingExtensions](../LazyListPagingExtensions/README.md) - Similar extensions for LazyList
- [BaseRepo-Paging](../BaseRepo-Paging/README.md) - Paging source implementations
