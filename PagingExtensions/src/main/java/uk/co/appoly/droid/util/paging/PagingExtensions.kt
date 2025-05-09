package uk.co.appoly.droid.util.paging

import androidx.paging.LoadState
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

enum class PagingErrorType {
	PREPEND,
	APPEND,
	REFRESH
}

@OptIn(ExperimentalContracts::class)
fun LoadState.isLoading(): Boolean {
	contract { returns(true) implies (this@isLoading is LoadState.Loading) }
	return this is LoadState.Loading
}

@OptIn(ExperimentalContracts::class)
fun LoadState.isError(): Boolean {
	contract { returns(true) implies (this@isError is LoadState.Error) }
	return this is LoadState.Error
}