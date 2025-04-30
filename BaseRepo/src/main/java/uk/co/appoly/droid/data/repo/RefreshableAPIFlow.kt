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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
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
 * @param apiCall The suspend function that fetches the data from the API
 * @param scope The [CoroutineScope] to use for the API call, Default is [Dispatchers.Default]
 */
@OptIn(ExperimentalAtomicApi::class)
class RefreshableAPIFlow<T : Any>(
	initialValue: T?,
	initialRefresh: Boolean = initialValue == null,
	private val apiCall: suspend () -> APIResult<T>,
	private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : Flow<APIFlowState<T>> {
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

	constructor(
		apiCall: suspend () -> APIResult<T>,
		scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
	) : this(
		initialValue = null,
		initialRefresh = true,
		apiCall = apiCall,
		scope = scope
	)

	private val Log: FlexiLog
		get() = ServiceManager.getLogger()

	private val isRefreshing = AtomicBoolean(false)
	private val internalFlow = MutableStateFlow<APIFlowState<T>>(APIFlowState.Loading)

	fun asSharedFlow(): SharedFlow<APIFlowState<T>> = internalFlow.asSharedFlow()

	/**
	 * Refresh the API call and emit the new state
	 */
	fun refresh(onComplete: (() -> Unit)? = null) {
		if (isRefreshing.compareAndSet(false, true)) {
			scope.launch {
				try {
					internalFlow.emit(APIFlowState.Loading)
					internalFlow.emit(apiCall().asApiFlowState())
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

	override suspend fun collect(collector: FlowCollector<APIFlowState<T>>) = internalFlow.collect(collector)

	suspend fun manualUpdate(value: T) {
		internalFlow.emit(APIFlowState.Success(value))
	}

	fun updateState(function: (APIFlowState<T>?) -> APIFlowState<T>) =
		internalFlow.update(function = function)

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

fun <T : Any> RefreshableAPIFlow<T>.stateIn(
	scope: CoroutineScope,
	started: SharingStarted = SharingStarted.WhileSubscribed(5000),
	initialValue: APIFlowState<T> = APIFlowState.Loading
) = this.stateIn(scope, started, initialValue)

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