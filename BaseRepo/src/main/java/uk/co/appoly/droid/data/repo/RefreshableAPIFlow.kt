package uk.co.appoly.droid.data.repo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.duck.flexilogger.FlexiLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.appoly.droid.data.remote.ServiceManager
import uk.co.appoly.droid.data.remote.model.APIResult
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * A wrapper around a [Flow] that fetches data from an API and emits [APIFlowState] of [T] for
 * loading, success or error states, and provides a method to [refresh] the api call.
 *
 * @param T The type of data returned by the API
 * @param initialValue Optional initial value to use before the API call completes
 * @param initialRefresh Whether to automatically refresh the data when created
 * @param apiCall The suspend function that fetches the data from the API
 * @param scope The [CoroutineScope] to use for API calls
 */
@OptIn(ExperimentalAtomicApi::class)
class RefreshableAPIFlow<T : Any>(
	initialValue: T?,
	initialRefresh: Boolean = initialValue == null,
	private val apiCall: suspend () -> APIResult<T>,
	private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : Flow<APIFlowState<T>> {
	/**
	 * Constructor that creates a [RefreshableAPIFlow] with an optional initial value.
	 *
	 * @param initialValue Optional initial value to use before the API call completes
	 * @param apiCall The suspend function that fetches the data from the API
	 * @param scope The [CoroutineScope] to use for API calls
	 */
	constructor(
		initialValue: T?,
		apiCall: suspend () -> APIResult<T>,
		scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
	) : this(
		initialValue = initialValue,
		initialRefresh = initialValue == null,
		apiCall = apiCall,
		scope = scope
	)

	/**
	 * Constructor that creates a [RefreshableAPIFlow] with no initial value.
	 * Will automatically refresh on creation.
	 *
	 * @param apiCall The suspend function that fetches the data from the API
	 * @param scope The [CoroutineScope] to use for API calls
	 */
	constructor(
		apiCall: suspend () -> APIResult<T>,
		scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
	) : this(
		initialValue = null,
		initialRefresh = true,
		apiCall = apiCall,
		scope = scope
	)

	/**
	 * Logger instance used for logging API calls and errors.
	 */
	private val Log: FlexiLog
		get() = ServiceManager.getLogger()

	private val isRefreshing = AtomicBoolean(false)
	private val internalFlow = MutableStateFlow<APIFlowState<T>>(APIFlowState.Loading)

	/**
	 * Converts this flow into a [SharedFlow] that can be shared among multiple collectors.
	 *
	 * @return A [SharedFlow] that emits the same values as this flow
	 */
	fun asSharedFlow(): SharedFlow<APIFlowState<T>> = internalFlow.asSharedFlow()

	/**
	 * Refresh the API call and emit the new state.
	 *
	 * @param simulatedError Optional simulated error to emit instead of making an API call,
	 * this is useful for testing purposes.
	 * @param onComplete Optional callback to be invoked when the refresh operation completes
	 */
	fun refresh(
		simulatedError: APIFlowState.Error? = null,
		onComplete: (() -> Unit)? = null
	) {
		if (isRefreshing.compareAndSet(expectedValue = false, newValue = true)) {
			scope.launch {
				try {
					internalFlow.emit(APIFlowState.Loading)
					if (simulatedError != null) {
						Log.v(this@RefreshableAPIFlow, "Simulated error: ${simulatedError.message}")
						delay(350) // Simulate network delay
						internalFlow.emit(simulatedError)
					} else {
						internalFlow.emit(apiCall().asApiFlowState())
					}
				} catch (e: Exception) {
					Log.w(this@RefreshableAPIFlow, "Exception in refresh", e)
					internalFlow.emit(APIFlowState.Error(AppolyBaseRepo.RESPONSE_EXCEPTION_CODE, e.message ?: "Unknown error"))
				} finally {
					isRefreshing.store(false)
					onComplete?.invoke()
				}
			}
		} else {
			onComplete?.invoke()
		}
	}

	/**
	 * Implements the [Flow.collect] method to collect values from this flow.
	 *
	 * @param collector The [FlowCollector] to collect values from this flow
	 */
	override suspend fun collect(collector: FlowCollector<APIFlowState<T>>) = internalFlow.collect(collector)

	/**
	 * Manually updates the flow with a success state containing the provided value.
	 *
	 * @param value The value to emit as a success state
	 */
	suspend fun manualUpdate(value: T) {
		internalFlow.emit(APIFlowState.Success(value))
	}

	/**
	 * Updates the current state using the provided transformation function.
	 *
	 * @param function A function that transforms the current state into a new state
	 */
	fun updateState(function: (APIFlowState<T>?) -> APIFlowState<T>) =
		internalFlow.update(function = function)

	/**
	 * Updates the current data value using the provided transformation function.
	 * The current state must be a success state, or the transformation will be
	 * applied to null.
	 *
	 * @param function A function that transforms the current data value into a new value
	 */
	fun update(function: (T?) -> T) =
		internalFlow.update {
			APIFlowState.Success(function(it.successData()))
		}

	init {
		scope.launch {
			if (initialValue != null) {
				Log.v(this@RefreshableAPIFlow, "Initial value provided, emitting success state")
				internalFlow.emit(APIFlowState.Success(initialValue))
			}
			if (initialRefresh) {
				Log.v(this@RefreshableAPIFlow, "Initial refresh requested")
				refresh()
			}
		}
	}
}

/**
 * Creates a [StateFlow] from this [RefreshableAPIFlow] that can be shared among multiple collectors.
 *
 * @param scope The [CoroutineScope] in which sharing is to be started
 * @param started The strategy that controls when sharing is started and stopped
 * @param initialValue The initial value to emit before any value from the source flow is emitted
 * @return A [StateFlow] that emits the same values as this flow
 */
fun <T : Any> RefreshableAPIFlow<T>.stateIn(
	scope: CoroutineScope,
	started: SharingStarted = SharingStarted.WhileSubscribed(5000),
	initialValue: APIFlowState<T> = APIFlowState.Loading
) = this.stateIn(scope, started, initialValue)

/**
 * Uses the [scan] operator to cache the most recent success data from this flow.
 * When the flow emits a [APIFlowState.Success], the data is transformed using [map] and cached.
 * For [APIFlowState.Loading] and [APIFlowState.Error] states, the previously cached value is retained.
 *
 * This is useful for maintaining stable data in Compose UI during refresh operations,
 * preventing UI flicker when the flow transitions between Loading and Success states.
 *
 * @param T The type of data in the success state
 * @param R The type of the cached/transformed data
 * @param initial The initial value to emit before any success state is received
 * @param map A transformation function that converts success data of type [T] to cached data of type [R]
 * @return A [Flow] that emits the cached/transformed success data or the initial value
 *
 * @sample
 * ```kotlin
 * // Cache user names from a user data flow
 * val userNameFlow = userApiFlow.cacheSuccessData("Unknown") { user -> user.name }
 *
 * // Cache the entire data object
 * val cachedUserFlow = userApiFlow.cacheSuccessData(null) { user -> user }
 * ```
 */
inline fun <T, R> Flow<APIFlowState<T>>.cacheSuccessData(
	initial: R,
	crossinline map: (T) -> R
): Flow<R> =
	scan<APIFlowState<T>, R>(initial) { cachedValue, apiFlowState ->
		when (apiFlowState) {
			is APIFlowState.Success -> map(apiFlowState.data)
			else -> cachedValue
		}
	}

/**
 * Collects values from this flow and represents its latest value as [State] in a composable.
 *
 * @return A [State] object that represents the latest value from this flow
 */
@Composable
fun <T : Any> RefreshableAPIFlow<T>.collectAsState(): State<APIFlowState<T>> =
	this.collectAsState(APIFlowState.Loading)

/**
 * Creates a [androidx.compose.runtime.remember] 'Cache' of the data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to null.
 *
 * @return the cached value as a [androidx.compose.runtime.State].
 */
@Composable
fun <T> APIFlowState<T>.rememberSuccessDataAsState(initialValue: T? = null): State<T?> {
	val cache = remember {
		mutableStateOf(initialValue)
	}
	val successData = successData()
	LaunchedEffect(successData) {
		successData?.let {
			cache.value = it
		}
	}
	return cache
}

/**
 * Creates a [androidx.compose.runtime.saveable.rememberSaveable] 'Cache' of the data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to null.
 *
 * @return the cached value as a [androidx.compose.runtime.State].
 */
@Composable
fun <T> APIFlowState<T>.rememberSaveableSuccessDataAsState(initialValue: T? = null): State<T?> {
	val cache = rememberSaveable {
		mutableStateOf(initialValue)
	}
	val successData = successData()
	LaunchedEffect(successData) {
		successData?.let {
			cache.value = it
		}
	}
	return cache
}

/**
 * Creates a [androidx.compose.runtime.remember] 'Cache' of the List data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to [emptyList].
 *
 * @return the cached value as a [androidx.compose.runtime.State].
 */
@Composable
fun <T> APIFlowState<List<T>>.rememberSuccessListAsState(initialValue: List<T> = emptyList()): State<List<T>> {
	val cache = remember {
		mutableStateOf(initialValue)
	}
	val successData = successData()
	LaunchedEffect(successData) {
		successData?.let {
			cache.value = it
		}
	}
	return cache
}

/**
 * Creates a [androidx.compose.runtime.saveable.rememberSaveable] 'Cache' of the List data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to [emptyList].
 *
 * @return the cached value as a [androidx.compose.runtime.State].
 */
@Composable
fun <T> APIFlowState<List<T>>.rememberSaveableSuccessListAsState(initialValue: List<T> = emptyList()): State<List<T>> {
	val cache = rememberSaveable {
		mutableStateOf(initialValue)
	}
	val successData = successData()
	LaunchedEffect(successData) {
		successData?.let {
			cache.value = it
		}
	}
	return cache
}

/**
 * Creates a [androidx.compose.runtime.remember] 'Cache' of the data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to null.
 *
 * @return the cached value.
 */
@Composable
fun <T> APIFlowState<T>.rememberSuccessData(initialValue: T? = null): T? {
	var cache by remember {
		mutableStateOf(initialValue)
	}
	successData()?.let {
		cache = it
	}
	return cache
}

/**
 * Creates a [androidx.compose.runtime.saveable.rememberSaveable] 'Cache' of the data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to null.
 *
 * @return the cached value.
 */
@Composable
fun <T> APIFlowState<T>.rememberSaveableSuccessData(initialValue: T? = null): T? {
	var cache by rememberSaveable {
		mutableStateOf(initialValue)
	}
	successData()?.let {
		cache = it
	}
	return cache
}

/**
 * Creates a [androidx.compose.runtime.remember] 'Cache' of the List data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to [emptyList].
 *
 * @return the cached value.
 */
@Composable
fun <T> APIFlowState<List<T>>.rememberSuccessList(initialValue: List<T> = emptyList()): List<T> {
	var cache by remember {
		mutableStateOf(initialValue)
	}
	successData()?.let {
		cache = it
	}
	return cache
}

/**
 * Creates a [androidx.compose.runtime.saveable.rememberSaveable] 'Cache' of the List data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to [emptyList].
 *
 * @return the cached value.
 */
@Composable
fun <T> APIFlowState<List<T>>.rememberSaveableSuccessList(initialValue: List<T> = emptyList()): List<T> {
	var cache by rememberSaveable {
		mutableStateOf(initialValue)
	}
	successData()?.let {
		cache = it
	}
	return cache
}
