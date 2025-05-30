package uk.co.appoly.droid.ui.snackbar

import uk.co.appoly.droid.ui.UiState

/**
 * Extension property that converts a [UiState] to the appropriate [SnackBarType].
 *
 * This property makes it easy to show snackbars that automatically match the current UI state:
 * - [UiState.Success] → [SnackBarType.Success]
 * - [UiState.Error] → [SnackBarType.Error]
 * - All other states → [SnackBarType.Info]
 *
 * Example usage:
 * ```kotlin
 * snackbarHostState.showSnackbar(
 *     message = "Operation completed",
 *     type = uiState.snackBarType
 * )
 * ```
 *
 * @return The appropriate [SnackBarType] for the current [UiState]
 */
val UiState?.snackBarType: SnackBarType
	get() = when (this) {
		is UiState.Success -> SnackBarType.Success
		is UiState.Error -> SnackBarType.Error
		else -> SnackBarType.Info
	}
