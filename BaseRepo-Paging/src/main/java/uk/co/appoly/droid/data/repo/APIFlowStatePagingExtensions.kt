package uk.co.appoly.droid.data.repo

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T : Any> Flow<APIFlowState<List<T>>>.mapToPagingData(): Flow<PagingData<T>> =
	map { it.asPagingData() }

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