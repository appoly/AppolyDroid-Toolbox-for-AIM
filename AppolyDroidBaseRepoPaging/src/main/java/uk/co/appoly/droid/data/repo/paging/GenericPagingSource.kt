package uk.co.appoly.droid.data.repo.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.remote.model.response.PageData

/**
 * GenericPagingSource is a generic implementation of PagingSource that can be used to load
 * data from a remote API.
 *
 * It takes a function that fetches a page of data from the API and returns an [APIResult]
 * containing the page data.
 *
 * The page data is expected to be of type [PageData]<T>, where T is the type of data
 * being loaded.
 *
 * The pageSize parameter is used to specify the number of items to be loaded per page.
 *
 * The jumpingSupported parameter is used to specify whether the PagingSource supports
 * jumping to a specific page.
 *
 * The load method is responsible for loading the data from the API and returning
 * the result as a LoadResult.
 *
 * The getRefreshKey method is used to get the key for the current page being loaded.
 *
 * The GenericPagingSource is thread-safe and can be used in a multi-threaded
 * environment.
 *
 * @param T The type of data being loaded
 * @param fetchPage A function that fetches a page of data from the API
 * @param pageSize The number of items to be loaded per page
 * @param jumpingSupported Whether the PagingSource supports jumping to a specific page
 *
 * @see [PagingSource]
 */
class GenericPagingSource<T : Any>(
	private val fetchPage: suspend (perPage: Int, page: Int) -> APIResult<PageData<T>>,
	val pageSize: Int,
	override val jumpingSupported: Boolean = false
) : PagingSource<Int, T>() {
	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
		val nextPageNumber = params.key ?: 1
		val response: APIResult<PageData<T>> = fetchPage(pageSize, nextPageNumber)
		return when (response) {
			is APIResult.Success -> {
				val page = response.data
				LoadResult.Page(
					itemsBefore = page.itemsBefore,
					itemsAfter = page.itemsAfter,
					data = page.data,
					prevKey = page.prevPage,
					nextKey = page.nextPage
				)
			}

			is APIResult.Error -> {
				LoadResult.Error(Exception(response.message, response.throwable))
			}
		}
	}

	override fun getRefreshKey(state: PagingState<Int, T>): Int? {
		return if (!jumpingSupported) {
			state.anchorPosition?.let { anchorPosition ->
				val anchorPage = state.closestPageToPosition(anchorPosition)
				anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
			}
		} else {
			// For jumping support
			state.anchorPosition?.let { anchorPosition ->
				(anchorPosition / pageSize) + 1
			}
		}
	}
}