package uk.co.appoly.droid.ui

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class UiState {
	abstract val key: Any?

	data class Idle(
		override val key: Any? = null
	) : UiState()

	data class Loading(
		override val key: Any? = null
	) : UiState()

	data class Success(
		override val key: Any? = null
	) : UiState()

	data class Error(
		val message: String,
		override val key: Any? = null
	) : UiState()
}

/**
 * Returns true if the [UiState] is [UiState.Idle].
 *
 * @return true if the [UiState] is [UiState.Idle].
 *
 * @see UiState.Idle
 */
@OptIn(ExperimentalContracts::class)
fun UiState?.isIdle(): Boolean {
	contract {
		returns(true) implies (this@isIdle is UiState.Idle)
	}
	return this is UiState.Idle
}

/**
 * Returns true if the [UiState] is [UiState.Loading].
 *
 * @return true if the [UiState] is [UiState.Loading].
 *
 * @see UiState.Loading
 */
@OptIn(ExperimentalContracts::class)
fun UiState?.isLoading(): Boolean {
	contract {
		returns(true) implies (this@isLoading is UiState.Loading)
	}
	return this is UiState.Loading
}

/**
 * Returns true if the [UiState] is [UiState.Success].
 *
 * @return true if the [UiState] is [UiState.Success].
 *
 * @see UiState.Success
 */
@OptIn(ExperimentalContracts::class)
fun UiState?.isNotLoading(): Boolean {
	contract {
		returns(true) implies (this@isNotLoading !is UiState.Loading)
	}
	return this !is UiState.Loading
}

/**
 * Returns true if the [UiState] is not [UiState.Error].
 *
 * @return true if the [UiState] is not [UiState.Error].
 *
 * @see UiState.Error
 */
@OptIn(ExperimentalContracts::class)
fun UiState?.isNotError(): Boolean {
	contract { returns(true) implies (this@isNotError !is UiState.Error) }
	return this !is UiState.Error
}

/**
 * Returns true if the [UiState] is [UiState.Success].
 *
 * @return true if the [UiState] is [UiState.Success].
 *
 * @see UiState.Success
 */
@OptIn(ExperimentalContracts::class)
fun UiState?.isSuccess(): Boolean {
	contract {
		returns(true) implies (this@isSuccess is UiState.Success)
	}
	return this is UiState.Success
}

/**
 * Returns true if the [UiState] is [UiState.Error].
 *
 * @return true if the [UiState] is [UiState.Error].
 *
 * @see UiState.Error
 */
@OptIn(ExperimentalContracts::class)
fun UiState?.isError(): Boolean {
	contract {
		returns(true) implies (this@isError is UiState.Error)
	}
	return this is UiState.Error
}