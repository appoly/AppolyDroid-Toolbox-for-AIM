package uk.co.appoly.droid.data.repo.paging

import androidx.annotation.VisibleForTesting
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingSourceFactory
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.remote.model.response.PageData
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.roundToInt

/**
 * Wrapper class for a [GenericPagingSource] factory intended for usage in [androidx.paging.Pager] construction.
 *
 * Calling [invalidate] on this [GenericInvalidatingPagingSourceFactory] will forward invalidate signals
 * to all active [GenericPagingSource]s that were produced by calling [invoke].
 *
 * This class is thread-safe for concurrent calls to any mutative operations including both
 * [invoke] and [invalidate].
 *
 * @param pageSize The size of each page to be loaded
 * @param jumpingSupported Whether the PagingSource supports jumping to a specific page
 * @param jumpPageThreshold Optional multiplier for determining the jump threshold (pageSize * jumpPageThreshold)
 * @param fetchPageCall Function to fetch a page of data from the API
 *
 * @see [androidx.paging.InvalidatingPagingSourceFactory]
 */
class GenericInvalidatingPagingSourceFactory<Value : Any>(
	val pageSize: Int,
	val jumpingSupported: Boolean = false,
	val jumpPageThreshold: Float? = null,
	fetchPageCall: suspend (perPage: Int, page: Int) -> APIResult<PageData<Value>>
) : PagingSourceFactory<Int, Value> {
	/**
	 * Factory function that creates new [GenericPagingSource] instances
	 */
	private val pagingSourceFactory: () -> GenericPagingSource<Value> = {
		GenericPagingSource<Value>(
			fetchPage = fetchPageCall,
			pageSize = pageSize,
			jumpingSupported = jumpingSupported
		)
	}

	/**
	 * Lock for thread-safe operations on [pagingSources]
	 */
	private val lock = ReentrantLock()

	/**
	 * List of active paging sources created by this factory
	 */
	private var pagingSources: List<GenericPagingSource<Value>> = emptyList()

	/**
	 * Returns the current list of active paging sources.
	 * This method is primarily intended for testing.
	 *
	 * @return List of active [GenericPagingSource] instances
	 */
	@VisibleForTesting
	internal fun pagingSources() = pagingSources

	/**
	 * Creates a new [GenericPagingSource] instance and adds it to the factory's tracking list.
	 *
	 * This method is thread-safe and can be called concurrently.
	 *
	 * @return A new [GenericPagingSource] instance that will be invalidated when [invalidate] is called
	 */
	override fun invoke(): PagingSource<Int, Value> {
		return pagingSourceFactory().also {
			lock.withLock {
				pagingSources = pagingSources + it
			}
		}
	}

	/**
	 * Invalidates all active [GenericPagingSource] instances created by this factory.
	 *
	 * This causes the Paging library to create a new PagingSource and reload its data.
	 * After invalidation, the factory clears its list of tracked PagingSources.
	 *
	 * This method is thread-safe and can be called concurrently.
	 */
	fun invalidate() {
		val previousList = lock.withLock {
			pagingSources.also {
				pagingSources = emptyList()
			}
		}
		for (pagingSource in previousList) {
			if (!pagingSource.invalid) {
				pagingSource.invalidate()
			}
		}
	}

	/**
	 * Creates a [Pager] instance configured with this factory.
	 *
	 * This is a convenience method that creates a properly configured [Pager]
	 * with the appropriate settings for page size and jumping threshold.
	 *
	 * @param initialKey The initial page key to use (default is 1)
	 * @param enablePlaceholders Whether to enable placeholders in the [Pager] (default is false)
	 * @return A configured [Pager] instance ready for collecting as a [androidx.paging.PagingData] flow
	 */
	fun getPager(
		initialKey: Int = 1,
		enablePlaceholders: Boolean = false
	): Pager<Int, Value> {
		return Pager(
			config = PagingConfig(
				pageSize = pageSize,
				enablePlaceholders = enablePlaceholders,
				jumpThreshold = if (this.jumpingSupported) {
					((jumpPageThreshold ?: 3f) * pageSize).roundToInt()
				} else {
					PagingSource.LoadResult.Page.Companion.COUNT_UNDEFINED
				}
			),
			initialKey = initialKey,
			pagingSourceFactory = this
		)
	}
}
