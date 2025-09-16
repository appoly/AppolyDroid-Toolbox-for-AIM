package uk.co.appoly.droid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.co.appoly.droid.ui.UiState

class UiStateDemoViewModel : ViewModel() {

	private val _uiState = MutableStateFlow<UiState>(UiState.Idle())
	val uiState: StateFlow<UiState> = _uiState.asStateFlow()

	private val _multiOperationState = MutableStateFlow<UiState>(UiState.Idle())
	val multiOperationState: StateFlow<UiState> = _multiOperationState.asStateFlow()

	fun simulateLoading() {
		_uiState.value = UiState.Loading()
		viewModelScope.launch {
			delay(2000) // Simulate network call
			_uiState.value = UiState.Success()
		}
	}

	fun simulateError() {
		_uiState.value = UiState.Loading()
		viewModelScope.launch {
			delay(1500)
			_uiState.value = UiState.Error("Failed to load data. Please try again.")
		}
	}

	fun simulateSuccess() {
		_uiState.value = UiState.Loading()
		viewModelScope.launch {
			delay(1000)
			_uiState.value = UiState.Success("Data loaded successfully!")
		}
	}

	fun resetState() {
		_uiState.value = UiState.Idle()
	}

	// Multi-operation demo
	fun loadUserProfile() {
		_multiOperationState.value = UiState.Loading(key = "profile")
		viewModelScope.launch {
			delay(2000)
			_multiOperationState.value = UiState.Success(key = "profile")
		}
	}

	fun loadUserPosts() {
		_multiOperationState.value = UiState.Loading(key = "posts")
		viewModelScope.launch {
			delay(3000)
			_multiOperationState.value = UiState.Error("Failed to load posts", key = "posts")
		}
	}

	fun resetMultiState() {
		_multiOperationState.value = UiState.Idle()
	}
}