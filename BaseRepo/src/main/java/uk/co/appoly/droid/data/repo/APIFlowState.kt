package uk.co.appoly.droid.data.repo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import uk.co.appoly.droid.data.remote.model.APIResult
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Represents the state of an API request flow.
 *
 * This sealed class is used to model the different states that an API request can be in:
 * loading, success with data, or error with details.
 *
 * @param T The type of data expected in case of success
 */
sealed class APIFlowState<out T> {
	/**
	 * Represents the loading state of an API request.
	 */
	data object Loading : APIFlowState<Nothing>()

	/**
	 * Represents a successful API request that returned data.
	 *
	 * @property data The data returned by the API request
	 */
	data class Success<T>(val data: T) : APIFlowState<T>()

	/**
	 * Represents an error that occurred during an API request.
	 *
	 * @property responseCode The HTTP status code or error code
	 * @property message The error message describing what went wrong
	 */
	data class Error(val responseCode: Int, val message: String) : APIFlowState<Nothing>() {
		constructor(other: Error) : this(other.responseCode, other.message)
		constructor(other: APIResult.Error) : this(other.responseCode, other.message)
	}
}

/**
 * Converts an [APIResult] to an [APIFlowState].
 *
 * @return An [APIFlowState] representing the same state as the original [APIResult]
 */
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

/**
 * Creates a [remember] 'Cache' of the data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to null.
 *
 * @return the cached value as a [State].
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
 * Creates a [rememberSaveable] 'Cache' of the data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to null.
 *
 * @return the cached value as a [State].
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
 * Creates a [remember] 'Cache' of the List data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to [emptyList].
 *
 * @return the cached value as a [State].
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
 * Creates a [rememberSaveable] 'Cache' of the List data with initial value of [initialValue],
 * Updates the cache if the [APIFlowState] is [APIFlowState.Success].
 *
 * Using this results in a stable value between refreshes when the [APIFlowState] changes between [APIFlowState.Loading] and [APIFlowState.Success].
 *
 * @param initialValue the initial value of the cache. Defaults to [emptyList].
 *
 * @return the cached value as a [State].
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
 * Creates a [remember] 'Cache' of the data with initial value of [initialValue],
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
 * Creates a [rememberSaveable] 'Cache' of the data with initial value of [initialValue],
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
 * Creates a [remember] 'Cache' of the List data with initial value of [initialValue],
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
 * Creates a [rememberSaveable] 'Cache' of the List data with initial value of [initialValue],
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

/**
 * Uses the [scan] operator to cache the most recent success data from this flow.
 * When the flow emits a [APIFlowState.Success], the data is cached directly.
 * For [APIFlowState.Loading] and [APIFlowState.Error] states, the previously cached value is retained.
 *
 * This is useful for maintaining stable data in Compose UI during refresh operations,
 * preventing UI flicker when the flow transitions between Loading and Success states.
 *
 * @param T The type of data in the success state
 * @param initial The initial value to emit before any success state is received
 * @return A [Flow] that emits the cached success data or the initial value
 *
 * @sample
 * ```kotlin
 * // Cache user data from a user data flow
 * val userFlow = userApiFlow.cacheSuccessData(User()) // Assuming User is a data
 * // data class with a default constructor
 * ```
 */
fun <T> Flow<APIFlowState<T>>.cacheSuccessData(
	initial: T
) = scan<APIFlowState<T>, T>(initial) { cachedValue, apiFlowState ->
	when (apiFlowState) {
		is APIFlowState.Success -> apiFlowState.data
		else -> cachedValue
	}
}

/**
 * Uses the [scan] operator to cache the most recent success data from this flow.
 * When the flow emits a [APIFlowState.Success], the data is cached directly.
 * For [APIFlowState.Loading] and [APIFlowState.Error] states, the previously cached value is retained.
 *
 * This is useful for maintaining stable data in Compose UI during refresh operations,
 * preventing UI flicker when the flow transitions between Loading and Success states.
 *
 * @param T The type of data in the success state
 * @param scope The [CoroutineScope] in which sharing is to be started
 * @param started The strategy that controls when sharing is started and stopped
 * @param initial The initial value to emit before any success state is received
 * @return A [StateFlow] that emits the cached success data or the initial value
 *
 * @sample
 * ```kotlin
 * // Cache user data from a user data flow
 * val userFlow = userApiFlow.cacheSuccessDataStateIn(
 *    initial = User(), // Assuming User is a data class with a default constructor
 *    scope = viewModelScope
 *    started = SharingStarted.WhileSubscribed(5000)
 * )
 * ```
 */
fun <T> Flow<APIFlowState<T>>.cacheSuccessData(
	scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
	started: SharingStarted = SharingStarted.WhileSubscribed(5000),
	initial: T
) = scan<APIFlowState<T>, T>(initial) { cachedValue, apiFlowState ->
	when (apiFlowState) {
		is APIFlowState.Success -> apiFlowState.data
		else -> cachedValue
	}
}.stateIn(scope = scope, started = started, initialValue = initial)

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
) = scan<APIFlowState<T>, R>(initial) { cachedValue, apiFlowState ->
	when (apiFlowState) {
		is APIFlowState.Success -> map(apiFlowState.data)
		else -> cachedValue
	}
}

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
 * @param scope The [CoroutineScope] in which sharing is to be started
 * @param started The strategy that controls when sharing is started and stopped
 * @param initial The initial value to emit before any success state is received
 * @param map A transformation function that converts success data of type [T] to cached data of type [R]
 * @return A [StateFlow] that emits the cached/transformed success data or the initial value
 *
 * @sample
 * ```kotlin
 * // Cache user names from a user data flow
 * val userNameFlow = userApiFlow.cacheSuccessDataStateIn(
 *    initial = "Unknown",
 *    map = { user -> user.name },
 *    scope = viewModelScope,
 *    started = SharingStarted.WhileSubscribed(5000)
 * )
 *
 * // Cache the entire data object
 * val cachedUserFlow = userApiFlow.cacheSuccessDataStateIn(
 *    initial = null,
 *    map = { user -> user },
 *    scope = viewModelScope,
 *    started = SharingStarted.WhileSubscribed(5000)
 * )
 * ```
 */
inline fun <T, R> Flow<APIFlowState<T>>.cacheSuccessData(
	scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
	started: SharingStarted = SharingStarted.WhileSubscribed(5000),
	initial: R,
	crossinline map: (T) -> R
) = scan<APIFlowState<T>, R>(initial) { cachedValue, apiFlowState ->
	when (apiFlowState) {
		is APIFlowState.Success -> map(apiFlowState.data)
		else -> cachedValue
	}
}.stateIn(scope = scope, started = started, initialValue = initial)

/**
 * Maps the data in the [APIFlowState] to a new type [R].
 * If the state is [APIFlowState.Loading], it remains unchanged.
 * If the state is [APIFlowState.Error], it remains unchanged.
 * If the state is [APIFlowState.Success], the data is transformed using the provided [transform] function.
 *
 * @param T The type of data in the success state
 * @param R The type of data in the resulting success state
 * @param transform A function that transforms the success data of type [T] to type [R]
 * @return A new [APIFlowState] with the transformed data
 */
inline fun <T, R : Any> APIFlowState<T>.map(crossinline transform: (value: T) -> R): APIFlowState<R> {
	return when (this) {
		is APIFlowState.Loading -> APIFlowState.Loading
		is APIFlowState.Error -> APIFlowState.Error(this)
		is APIFlowState.Success -> APIFlowState.Success(transform(this.data))
	}
}