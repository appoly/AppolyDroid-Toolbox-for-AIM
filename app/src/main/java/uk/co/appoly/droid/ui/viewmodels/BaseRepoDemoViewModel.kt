package uk.co.appoly.droid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.repo.APIFlowState
import uk.co.appoly.droid.data.repo.asApiFlowState

// Mock data classes for demo
data class UserData(val id: Int, val name: String, val email: String)
data class PostData(val id: Int, val title: String, val content: String)

class BaseRepoDemoViewModel : ViewModel() {

	private val _userState = MutableStateFlow<APIFlowState<UserData>>(APIFlowState.Loading)
	val userState: StateFlow<APIFlowState<UserData>> = _userState.asStateFlow()

	private val _postsState = MutableStateFlow<APIFlowState<List<PostData>>>(APIFlowState.Loading)
	val postsState: StateFlow<APIFlowState<List<PostData>>> = _postsState.asStateFlow()

	private val _lastApiResult = MutableStateFlow<String>("")
	val lastApiResult: StateFlow<String> = _lastApiResult.asStateFlow()

	// Mock API calls that simulate different scenarios
	fun fetchUserSuccess() {
		_userState.value = APIFlowState.Loading
		_lastApiResult.value = "Fetching user..."

		viewModelScope.launch {
			delay(2000) // Simulate network delay
			val mockUser = UserData(1, "John Doe", "john.doe@example.com")
			_userState.value = APIResult.Success(mockUser).asApiFlowState()
			_lastApiResult.value = "✅ User fetched successfully: $mockUser"
		}
	}

	fun fetchUserError() {
		_userState.value = APIFlowState.Loading
		_lastApiResult.value = "Fetching user..."

		viewModelScope.launch {
			delay(1500)
			_userState.value = APIResult.Error(404, "User not found").asApiFlowState()
			_lastApiResult.value = "❌ API Error: User not found (404)"
		}
	}

	fun fetchUserNetworkError() {
		_userState.value = APIFlowState.Loading
		_lastApiResult.value = "Fetching user..."

		viewModelScope.launch {
			delay(1000)
			_userState.value = APIResult.Error(-1, "No Internet Connection").asApiFlowState()
			_lastApiResult.value = "❌ Network Error: No Internet Connection"
		}
	}

	fun fetchPostsSuccess() {
		_postsState.value = APIFlowState.Loading
		_lastApiResult.value = "Fetching posts..."

		viewModelScope.launch {
			delay(2500)
			val mockPosts = listOf(
				PostData(1, "First Post", "This is the content of the first post"),
				PostData(2, "Second Post", "This is the content of the second post"),
				PostData(3, "Third Post", "This is the content of the third post")
			)
			_postsState.value = APIResult.Success(mockPosts).asApiFlowState()
			_lastApiResult.value = "✅ Posts fetched successfully: ${mockPosts.size} posts"
		}
	}

	fun fetchPostsError() {
		_postsState.value = APIFlowState.Loading
		_lastApiResult.value = "Fetching posts..."

		viewModelScope.launch {
			delay(1800)
			_postsState.value = APIResult.Error(500, "Internal server error").asApiFlowState()
			_lastApiResult.value = "❌ API Error: Internal server error (500)"
		}
	}

	fun resetUserState() {
		_userState.value = APIFlowState.Loading
		_lastApiResult.value = "User state reset"
	}

	fun resetPostsState() {
		_postsState.value = APIFlowState.Loading
		_lastApiResult.value = "Posts state reset"
	}
}