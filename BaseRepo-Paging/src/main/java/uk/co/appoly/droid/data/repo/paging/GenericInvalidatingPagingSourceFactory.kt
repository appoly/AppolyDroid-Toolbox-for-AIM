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
 * @param pagingSourceFactory The [GenericPagingSource] factory that returns a PagingSource when called
 *
 * @see [androidx.paging.InvalidatingPagingSourceFactory]
 */
class GenericInvalidatingPagingSourceFactory<Value : Any>(
	val pageSize: Int,
	val jumpingSupported: Boolean = false,
	val jumpPageThreshold: Float? = null,
	fetchPageCall: suspend (perPage: Int, page: Int) -> APIResult<PageData<Value>>
) : PagingSourceFactory<Int, Value> {
	private val pagingSourceFactory: () -> GenericPagingSource<Value> = {
		GenericPagingSource<Value>(
			fetchPage = fetchPageCall,
			pageSize = pageSize,
			jumpingSupported = jumpingSupported
		)
	}

	private val lock = ReentrantLock()

	private var pagingSources: List<GenericPagingSource<Value>> = emptyList()

	@VisibleForTesting
	internal fun pagingSources() = pagingSources

	/**
	 * @return [androidx.paging.PagingSource] which will be invalidated when this factory's [invalidate] method
	 * is called
	 */
	override fun invoke(): PagingSource<Int, Value> {
		return pagingSourceFactory().also {
			lock.withLock {
				pagingSources = pagingSources + it
			}
		}
	}

	/**
	 * Calls [PagingSource.invalidate] on each [PagingSource] that was produced by this
	 * [androidx.paging.InvalidatingPagingSourceFactory]
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