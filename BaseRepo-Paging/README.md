# BaseRepo-Paging

An extension module for BaseRepo that adds Jetpack Paging 3 support for efficient data loading in RecyclerViews and Jetpack Compose.

## Features

- Seamless integration with BaseRepo
- Support for Jetpack Paging 3 library
- Standardized paging source implementation
- Invalidation capabilities for refreshing data
- Support for jumping to specific pages

## Installation

```gradle.kts
// Requires the base BaseRepo module
implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo:1.0.12")
implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo-Paging:1.0.12")
```

## Usage

### Creating a Paging Source Factory

```kotlin
class DataRepo : BaseRepo() {
    fun getWenWesLibraryPagingSourceFactory(
        searchQuery: String,
        status: Status,
        type: Type,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        jumpingSupported: Boolean = false,
        jumpPageThreshold: Float? = null
    ) = GenericInvalidatingPagingSourceFactory(
        pageSize = pageSize,
        jumpingSupported = jumpingSupported,
        jumpPageThreshold = jumpPageThreshold
    ) { perPage, page ->
        fetchWenWesLibraryPage(perPage, page, searchQuery, status, type)
    }
    
    // API call to fetch a page
    suspend fun fetchWenWesLibraryPage(
        perPage: Int,
        page: Int,
        searchQuery: String,
        status: Status,
        type: Type
    ): APIResult<PageData<ItemData>> = doNestedPagedAPICall("fetchWenWesLibraryPage") {
        dataService.api.fetchWenWesLibraryPage(
            perPage = perPage,
            page = page,
            body = SearchBody(
                searchQuery = searchQuery,
                status = status.id,
                type = type.id
            )
        )
    }
}
```

### Using the Paging Flow in a ViewModel

```kotlin
class MyViewModel : ViewModel() {
    private val dataRepo: DataRepo = // get repository instance
    
    // Create paging flow
    private val pagingSourceFactory = dataRepo.getWenWesLibraryPagingSourceFactory(
        searchQuery = "",
        status = Status.ACTIVE,
        type = Type.ALL,
        jumpingSupported = true
    )
    
    val pagingDataFlow = pagingSourceFactory
        .getPager(enablePlaceholders = true)
        .flow
        .cachedIn(viewModelScope)
    
    // Call this to refresh/invalidate the data
    fun refresh() {
        pagingSourceFactory.invalidate()
    }
}
```

### Using in Jetpack Compose

```kotlin
@Composable
fun ItemsList(viewModel: MyViewModel) {
    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        items(
            count = lazyPagingItems.itemCount,
            key = { index -> lazyPagingItems[index]?.id ?: index }
        ) { index ->
            val item = lazyPagingItems[index]
            if (item != null) {
                ItemRow(item = item)
            } else {
                // Placeholder for loading items
                ItemLoadingPlaceholder()
            }
        }
    }
}
```

### Using with Enhanced Extensions

For a more complete implementation with error handling, loading state, and placeholders, consider using the [LazyListPagingExtensions](../LazyListPagingExtensions/README.md) or [LazyGridPagingExtensions](../LazyGridPagingExtensions/README.md) modules.

```kotlin
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
```

## API Reference

### GenericInvalidatingPagingSourceFactory

The main class for creating invalidatable paging sources.

```kotlin
class GenericInvalidatingPagingSourceFactory<T : Any>(
    private val pageSize: Int,
    private val jumpingSupported: Boolean = false,
    private val jumpPageThreshold: Float? = null,
    private val fetchPage: suspend (perPage: Int, page: Int) -> APIResult<PageData<T>>
)
```

#### Methods

- `getPager(enablePlaceholders: Boolean)`: Creates a Pager for this source
- `invalidate()`: Invalidates the current paging source, triggering a refresh

#### Parameters

- `pageSize`: Number of items per page
- `jumpingSupported`: If true, allows jumping ahead to specific pages
- `jumpPageThreshold`: When to use jumping vs. sequential loading
- `fetchPage`: Suspend function to fetch a specific page of data

### doNestedPagedAPICall

Extension function for BaseRepo to handle paged API responses:

```kotlin
suspend fun <T : Any> doNestedPagedAPICall(
    tag: String, 
    apiCall: suspend () -> ApiResponse<GenericNestedPagedResponse<T>>
): APIResult<PageData<T>>
```

## Dependencies

- [BaseRepo](../BaseRepo/README.md) module
- [Jetpack Paging 3 library](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
- Kotlin Coroutines and Flow

## See Also

- [LazyListPagingExtensions](../LazyListPagingExtensions/README.md) - For enhanced LazyList integration
- [LazyGridPagingExtensions](../LazyGridPagingExtensions/README.md) - For enhanced LazyGrid integration
