package uk.co.appoly.droid.data.repo

import uk.co.appoly.droid.data.remote.model.APIResult
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class APIFlowState<out T> {
	data object Loading : APIFlowState<Nothing>()

	data class Success<T>(val data: T) : APIFlowState<T>()
	data class Error(val responseCode: Int, val message: String) : APIFlowState<Nothing>() {
		constructor(other: Error) : this(other.responseCode, other.message)
		constructor(other: APIResult.Error) : this(other.responseCode, other.message)
	}
}

fun <T : Any> APIResult<T>.asApiFlowState(): APIFlowState<T> {
	return when (this) {
		is APIResult.Success -> APIFlowState.Success(data)
		is APIResult.Error -> APIFlowState.Error(responseCode, message)
	}
}

/**
 * Returns true if the [APIFlowState] is [APIFlowState.Loading].
 *
 * @return true if the [APIFlowState] is [APIFlowState.Loading].
 *
 * @see APIFlowState.Loading
 */
@OptIn(ExperimentalContracts::class)
fun <T> APIFlowState<T>?.isLoading(): Boolean {
	contract {
		returns(true) implies (this@isLoading is APIFlowState.Loading)
	}
	return this is APIFlowState.Loading
}

/**
 * Returns true if the [APIFlowState] is not [APIFlowState.Loading].
 *
 * @return true if the [APIFlowState] is not [APIFlowState.Loading].
 *
 * @see APIFlowState.Loading
 */
@OptIn(ExperimentalContracts::class)
fun <T> APIFlowState<T>?.isNotLoading(): Boolean {
	contract {
		returns(true) implies (this@isNotLoading !is APIFlowState.Loading)
	}
	return this !is APIFlowState.Loading
}

/**
 * Returns true if the [APIFlowState] is [APIFlowState.Success].
 *
 * @return true if the [APIFlowState] is [APIFlowState.Success].
 *
 * @see APIFlowState.Success
 */
@OptIn(ExperimentalContracts::class)
fun <T> APIFlowState<T>?.isSuccess(): Boolean {
	contract {
		returns(true) implies (this@isSuccess is APIFlowState.Success)
	}
	return this is APIFlowState.Success
}

/**
 * Returns the data if the [APIFlowState] is [APIFlowState.Success], otherwise null.
 *
 * @return the data if the [APIFlowState] is [APIFlowState.Success], otherwise null.
 *
 * @see APIFlowState.Success
 */
@OptIn(ExperimentalContracts::class)
fun <T> APIFlowState<T>?.successData(): T? {
	contract {
		returnsNotNull() implies (this@successData is APIFlowState.Success)
	}
	return if (this.isSuccess()) {
		this.data
	} else {
		null
	}
}

/**
 * Returns the List data if the [APIFlowState] is [APIFlowState.Success], otherwise [emptyList].
 *
 * @return the List data if the [APIFlowState] is [APIFlowState.Success], otherwise [emptyList].
 *
 * @see APIFlowState.Success
 */
@OptIn(ExperimentalContracts::class)
fun <T> APIFlowState<List<T>>?.successList(): List<T> {
	contract {
		returnsNotNull() implies (this@successList is APIFlowState.Success)
	}
	return if (this.isSuccess()) {
		this.data
	} else {
		emptyList()
	}
}

/**
 * Returns true if the [APIFlowState] is [APIFlowState.Error].
 *
 * @return true if the [APIFlowState] is [APIFlowState.Error].
 *
 * @see APIFlowState.Error
 */
@OptIn(ExperimentalContracts::class)
fun <T> APIFlowState<T>?.isError(): Boolean {
	contract {
		returns(true) implies (this@isError is APIFlowState.Error)
	}
	return this is APIFlowState.Error
}

/**
 * Returns the message if the [APIFlowState] is [APIFlowState.Error], otherwise null.
 *
 * @return the message if the [APIFlowState] is [APIFlowState.Error], otherwise null.
 *
 * @see APIFlowState.Error
 */
@OptIn(ExperimentalContracts::class)
fun <T> APIFlowState<T>?.errorMessage(): String? {
	contract {
		returnsNotNull() implies (this@errorMessage is APIFlowState.Error)
	}
	return if (this.isError()) {
		this.message
	} else {
		null
	}
}