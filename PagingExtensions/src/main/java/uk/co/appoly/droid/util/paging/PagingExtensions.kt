package uk.co.appoly.droid.util.paging

import androidx.paging.LoadState
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Enumeration of error types that can occur during paging operations.
 *
 * These types represent different locations in the paging sequence where errors can occur:
 * - [PREPEND]: Error occurred when loading data at the beginning of the list
 * - [APPEND]: Error occurred when loading data at the end of the list
 * - [REFRESH]: Error occurred when initially loading or refreshing the data
 */
enum class PagingErrorType {
	PREPEND,
	APPEND,
	REFRESH
}

/**
 * Extension function to check if a [LoadState] is currently loading.
 *
 * Uses Kotlin contracts to allow smart casting to [LoadState.Loading] when the result is true.
 *
 * Example usage:
 * ```
 * if (loadState.isLoading()) {
 *     // loadState is now smart-cast to LoadState.Loading
 *     showLoadingIndicator()
 * }
 * ```
 *
 * @return True if this [LoadState] is a [LoadState.Loading], false otherwise
 */
@OptIn(ExperimentalContracts::class)
fun LoadState.isLoading(): Boolean {
	contract { returns(true) implies (this@isLoading is LoadState.Loading) }
	return this is LoadState.Loading
}

/**
 * Extension function to check if a [LoadState] is in an error state.
 *
 * Uses Kotlin contracts to allow smart casting to [LoadState.Error] when the result is true.
 *
 * Example usage:
 * ```
 * if (loadState.isError()) {
 *     // loadState is now smart-cast to LoadState.Error
 *     showErrorMessage(loadState.error.message)
 * }
 * ```
 *
 * @return True if this [LoadState] is a [LoadState.Error], false otherwise
 */
@OptIn(ExperimentalContracts::class)
fun LoadState.isError(): Boolean {
	contract { returns(true) implies (this@isError is LoadState.Error) }
	return this is LoadState.Error
}
