package uk.co.appoly.droid.ui

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Represents the different states of a user interface component or operation.
 *
 * This sealed class provides a standard way to represent UI states throughout an application,
 * making it easier to handle loading, success, and error states consistently.
 *
 * Each state can optionally include a [key] to identify which specific operation or UI element
 * the state applies to, enabling multiple concurrent operations to be tracked independently.
 *
 * @property key An optional identifier to track specific operations or UI elements
 */
sealed class UiState {
	abstract val key: Any?

	/**
	 * Represents the initial state before any operation has started.
	 *
	 * @property key Optional identifier for the operation or UI element
	 */
	data class Idle(
		override val key: Any? = null
	) : UiState()

	/**
	 * Represents an operation in progress.
	 *
	 * @property key Optional identifier for the operation or UI element
	 */
	data class Loading(
		override val key: Any? = null
	) : UiState()

	/**
	 * Represents a successfully completed operation.
	 *
	 * @property key Optional identifier for the operation or UI element
	 */
	data class Success(
		override val key: Any? = null
	) : UiState()

	/**
	 * Represents a failed operation.
	 *
	 * @property message The error message describing what went wrong
	 * @property key Optional identifier for the operation or UI element
	 */
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
