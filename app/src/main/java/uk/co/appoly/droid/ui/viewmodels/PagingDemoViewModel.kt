package uk.co.appoly.droid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.remote.model.response.PageData
import uk.co.appoly.droid.data.repo.paging.GenericPagingSource

// Mock data for demo
data class Product(
	val id: Int,
	val name: String,
	val price: Double,
	val category: String
)

class PagingDemoViewModel : ViewModel() {

	// Mock paging source that simulates API calls
	private val pagingSource = GenericPagingSource<Product>(
		fetchPage = { perPage, page ->
			delay(1000) // Simulate network delay

			// Simulate different scenarios based on page number
			when {
				page > 5 -> {
					// Simulate end of data
					APIResult.Success(
						PageData(
							data = emptyList(),
							currentPage = page,
							lastPage = 5,
							perPage = perPage,
							from = 0,
							to = 0,
							total = 50
						)
					)
				}

				page == 3 -> {
					// Simulate error on page 3
					APIResult.Error(500, null, listOf("Server error on page $page"))
				}

				else -> {
					// Generate mock products
					val startId = (page - 1) * perPage + 1
					val products = (0 until perPage).map { index ->
						val id = startId + index
						Product(
							id = id,
							name = "Product $id",
							price = (10.0 + id) * 1.5,
							category = when (id % 4) {
								0 -> "Electronics"
								1 -> "Books"
								2 -> "Clothing"
								else -> "Home & Garden"
							}
						)
					}

					APIResult.Success(
						PageData(
							data = products,
							currentPage = page,
							lastPage = 5,
							perPage = perPage,
							from = startId,
							to = startId + products.size - 1,
							total = 50
						)
					)
				}
			}
		},
		pageSize = 10
	)

	// Create Pager with the mock paging source
	val productsFlow: Flow<PagingData<Product>> = Pager(
		config = PagingConfig(
			pageSize = 10,
			enablePlaceholders = true,
			initialLoadSize = 10
		),
		pagingSourceFactory = { pagingSource }
	).flow.cachedIn(viewModelScope)
}