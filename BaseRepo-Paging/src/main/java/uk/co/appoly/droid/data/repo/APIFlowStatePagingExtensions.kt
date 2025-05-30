package uk.co.appoly.droid.data.repo

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Converts a Flow of [APIFlowState] containing a list to a Flow of [PagingData].
 *
 * This extension function enables integration between the APIFlowState pattern
 * used in BaseRepo and the Jetpack Paging library by converting flow states to
 * the appropriate PagingData representation.
 *
 * Example usage:
 * ```
 * // Convert repository flow to PagingData flow
 * val pagingFlow = repository.getItemsFlow()
 *     .mapToPagingData()
 *     .cachedIn(viewModelScope)
 * ```
 *
 * @param T The type of items in the list
 * @return A Flow of PagingData that can be collected by Paging UI components
 */
fun <T : Any> Flow<APIFlowState<List<T>>>.mapToPagingData(): Flow<PagingData<T>> =
	map { it.asPagingData() }

/**
 * Converts an [APIFlowState] containing a list to [PagingData].
 *
 * This extension function maps different states of APIFlowState to appropriate
 * PagingData representations:
 * - Success: Creates PagingData with the list items
 * - Loading: Creates empty PagingData with loading state
 * - Error: Creates empty PagingData with error state
 *
 * @param T The type of items in the list
 * @return PagingData representing the current state
 */
fun <T : Any> APIFlowState<List<T>>.asPagingData(): PagingData<T> = when (this) {
	is APIFlowState.Error -> {
		PagingData.empty(
			sourceLoadStates = LoadStates(
				refresh = LoadState.Error(
					error = Throwable(this.message)
				),
				prepend = LoadState.NotLoading(false),
				append = LoadState.NotLoading(false)
			)
		)
	}

	APIFlowState.Loading -> {
		PagingData.empty(
			sourceLoadStates = LoadStates(
				refresh = LoadState.Loading,
				prepend = LoadState.NotLoading(false),
				append = LoadState.NotLoading(false)
			)
		)
	}

	is APIFlowState.Success -> {
		PagingData.from(this.data)
	}
}
