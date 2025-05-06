package uk.co.appoly.droid.ui.snackbar

import uk.co.appoly.droid.ui.UiState

val UiState?.snackBarType: SnackBarType
	get() = when (this) {
		is UiState.Success -> SnackBarType.Success
		is UiState.Error -> SnackBarType.Error
		else -> SnackBarType.Info
	}