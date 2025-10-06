# BaseRepo-Paging

An extension module for BaseRepo that adds Jetpack Paging 3 support for efficient data loading in RecyclerViews and Jetpack Compose.

## Features

- Seamless integration with BaseRepo and APIResult pattern
- Support for Jetpack Paging 3 library
- Standardized paging source implementation
- Thread-safe invalidation capabilities for refreshing data
- Support for jumping to specific pages
- Integration with both traditional RecyclerView and Jetpack Compose

## Installation

```gradle.kts
// Requires the base BaseRepo module
implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo:1.0.34")
implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo-Paging:1.0.34")

// For Compose UI integration
implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:LazyListPagingExtensions:1.0.34") // For LazyColumn
implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:LazyGridPagingExtensions:1.0.34") // For LazyGrid
```

## API Response Format

This module requires your paginated API responses to follow a flat structure as shown below:

```json
{
  "status": "success",
  "messages": [
    "Data retrieved successfully"
  ],
  "data": [
    {
      "id": 1,
      "name": "Item 1"
    },
    {
      "id": 2,
      "name": "Item 2"
    }
  ],
  "total_records": 48,
  "filtered_records": 10
}
```

## Usage

### Step 1: Create API Service Interface

First, create your API service interface that returns paginated responses:

```kotlin
interface SitesAPI : BaseService.API {
	@POST("api/v1/sites")
	suspend fun searchSites(
		@Query("start") startIndex: Int,
		@Query("length") length: Int,
		@Query("search") search: String?,
	): ApiResponse<GenericPagedResponse<SitesItem>>
}
```

### Step 2: Add Repository Method to Fetch Pages

In your repository, create a method that uses `doPagedAPICall` to fetch a page:

```kotlin
class SitesRepository : AppolyBaseRepo({ YourRetrofitClient }) {
	private val sitesService by lazyService<SitesAPI>()

    // Function to fetch a single page
	suspend fun fetchSitesPage(
		startIndex: Int,
		length: Int,
		search: String?
	): APIResult<PageData<SitesItem>> = doPagedAPICall("fetchSitesPage", startIndex, length) {
		sitesService.api.searchSites(
			startIndex = startIndex,
			length = length,
			search = search
        )
    }
}
```

### Step 3: Create a PagingSource Factory

Create a factory that will generate paging sources on demand:

```kotlin
class SitesRepository : AppolyBaseRepo({ YourRetrofitClient }) {
    // ...existing code...

	fun getSitesPagingSourceFactory(
		search: String?,
        pageSize: Int = 20,
        jumpingSupported: Boolean = true,
        jumpPageThreshold: Float = 2f
	): GenericInvalidatingPagingSourceFactory<SitesItem> =
        GenericInvalidatingPagingSourceFactory(
            pageSize = pageSize,
            jumpingSupported = jumpingSupported,
            jumpPageThreshold = jumpPageThreshold
        ) { perPage, page ->
			val startIndex = (page - 1) * perPage
			fetchSitesPage(startIndex, perPage, search)
        }
}
```

### Step 4: Use in a ViewModel

```kotlin
class SitesViewModel(
	private val sitesRepository: SitesRepository
) : ViewModel() {
	private var currentSearch: String? = null

    // Create factory and pager
    private val pagingSourceFactory =
		sitesRepository.getSitesPagingSourceFactory(currentSearch)

    // Create flow to collect in UI
    val items = pagingSourceFactory.getPager()
        .flow
        .cachedIn(viewModelScope)

    // Function to refresh data
    fun refresh() {
        pagingSourceFactory.invalidate()
    }

    // Function to update search parameters
	fun search(search: String?) {
		currentSearch = search
        pagingSourceFactory.invalidate()
    }
}
```

### Step 5: Collect in UI

#### In Jetpack Compose

```kotlin
@Composable
fun SitesScreen(viewModel: SitesViewModel) {
    val items = viewModel.items.collectAsLazyPagingItems()

    LazyColumn {
        items(
            count = items.itemCount,
            key = { index -> items[index]?.id ?: index }
        ) { index ->
            val item = items[index]
            if (item != null) {
				SitesItemCard(item = item)
            } else {
                // Placeholder for loading state
                LoadingItemPlaceholder()
            }
        }

        // Handle different loading states
        when (items.loadState.refresh) {
            is LoadState.Loading -> item { FullScreenLoader() }
            is LoadState.Error -> item { ErrorView(onRetry = { items.retry() }) }
            else -> Unit
        }

        // Append loading indicator
        when (items.loadState.append) {
            is LoadState.Loading -> item { LoadingIndicator() }
            is LoadState.Error -> item { RetryButton(onRetry = { items.retry() }) }
            else -> Unit
        }
    }
}
```

#### In Traditional RecyclerView (with DataBinding)

```kotlin
class SitesFragment : Fragment() {
	private val viewModel: SitesViewModel by viewModels()
	private val adapter = SitesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view)

        binding.recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadingStateAdapter { adapter.retry() },
            footer = LoadingStateAdapter { adapter.retry() }
        )

        // Collect paging data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        // Set up swipe refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        // Monitor load state
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadState ->
                binding.swipeRefresh.isRefreshing =
                    loadState.refresh is LoadState.Loading

                // Show error if initial load fails
                if (loadState.refresh is LoadState.Error) {
                    showError((loadState.refresh as LoadState.Error).error.message)
                }
            }
        }
    }
}
```

## Converting APIFlowState to PagingData

If you have an existing Flow of `APIFlowState<List<T>>`, you can convert it to a PagingData flow:

```kotlin
class ItemsViewModel(private val repository: ItemsRepository) : ViewModel() {
    // Convert a standard flow to a paging flow
    val itemsPaging = repository.getItemsFlow()
        .mapToPagingData()
        .cachedIn(viewModelScope)
}
```

## Advanced Usage

### Controlling Jump Threshold

The `jumpPageThreshold` parameter determines when Paging will "jump" to a specific page instead of scrolling:

```kotlin
// Create a factory that allows jumping 5 pages when fast scrolling
pagingSourceFactory = GenericInvalidatingPagingSourceFactory(
    pageSize = 20,
    jumpingSupported = true,
    jumpPageThreshold = 5f
) { perPage, page ->
    fetchPage(perPage, page)
}
```

### Testing

The factory provides a `pagingSources()` method for testing purposes:

```kotlin
@Test
fun testInvalidation() {
    val factory = GenericInvalidatingPagingSourceFactory(
        pageSize = 10,
        fetchPageCall = { _, _ -> mockSuccessResult() }
    )

    // Create a paging source
    val pagingSource = factory.invoke()

    // Verify paging source is tracked
    assertEquals(1, factory.pagingSources().size)

    // Invalidate and verify tracking is reset
    factory.invalidate()
    assertEquals(0, factory.pagingSources().size)
}
```

## Key Components

### GenericPagedResponse

Models the nested paged response from your API.

### PageData

A flattened, non-nullable representation of page data with computed properties like `itemsBefore` and `itemsAfter`.

### GenericPagingSource

Implements Android's `PagingSource` for seamless integration with Paging 3.

### GenericInvalidatingPagingSourceFactory

Thread-safe factory that creates and tracks paging sources, allowing for invalidation.

## Dependencies

- [BaseRepo](../BaseRepo/README.md) - Core repository pattern implementation
- [Jetpack Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) - Android paging library
