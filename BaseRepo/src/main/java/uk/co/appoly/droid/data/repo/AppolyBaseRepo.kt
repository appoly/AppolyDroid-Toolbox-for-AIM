package uk.co.appoly.droid.data.repo

import com.duck.flexilogger.FlexiLog
import com.duck.flexilogger.LoggingLevel
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.errorBody
import com.skydoves.sandwich.retrofit.statusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.co.appoly.droid.BaseRepoLog
import uk.co.appoly.droid.BaseRepoLogger
import uk.co.appoly.droid.data.remote.BaseRetrofitClient
import uk.co.appoly.droid.data.remote.BaseService
import uk.co.appoly.droid.data.remote.ServiceManager
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.remote.model.response.BaseResponse
import uk.co.appoly.droid.data.remote.model.response.GenericResponse
import uk.co.appoly.droid.data.remote.model.response.RootJson
import uk.co.appoly.droid.util.NoConnectivityException
import uk.co.appoly.droid.util.asNoConnectivityException
import uk.co.appoly.droid.util.firstNotNullOrBlank
import uk.co.appoly.droid.util.parseBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Abstract base class for repository implementations.
 *
 * This class provides common functionality for API communication, error handling,
 * and response processing. It serves as the foundation for all repository classes
 * in the application.
 *
 * @property getRetrofitClient Lambda that provides a [BaseRetrofitClient] instance for API communication
 * @property logger Logger instance for logging API calls and errors
 */
@OptIn(ExperimentalContracts::class)
abstract class AppolyBaseRepo(
	val getRetrofitClient: () -> BaseRetrofitClient,
	logger: FlexiLog = BaseRepoLogger,
	loggingLevel: LoggingLevel = LoggingLevel.V
) {
	init {
		BaseRepoLog.updateLogger(logger, loggingLevel)
	}

	/**
	 * Gets or creates a [ServiceManager] instance for managing API services.
	 *
	 * @return A [ServiceManager] instance configured with this repository's retrofit client and logger
	 */
	fun getServiceManager(): ServiceManager {
		return ServiceManager.getInstance(
			getRetrofitClient = getRetrofitClient,
		)
	}

	companion object {
		/**
		 * Response code used for general exceptions that don't have a specific HTTP status code
		 */
		const val RESPONSE_EXCEPTION_CODE = -1
	}

	/**
	 * Helper method to lazily initialize a service
	 *
	 * @param T The API interface type to get a service for
	 * @return A lazy-initialized [BaseService] instance for the requested API type
	 */
	protected inline fun <reified T : BaseService.API> AppolyBaseRepo.lazyService(): Lazy<BaseService<T>> =
		lazy { getServiceManager().getService() }

	/**
	 * Executes an API call and processes the response into an [APIResult].
	 *
	 * This method handles successful responses, error responses, and exceptions,
	 * converting them all into the appropriate [APIResult] type.
	 *
	 * @param logDescription Description of the API call for logging purposes
	 * @param call Lambda that performs the actual API call and returns an [ApiResponse]
	 * @return An [APIResult] representing the outcome of the API call
	 */
	protected inline fun <T : Any> doAPICall(
		logDescription: String,
		call: () -> ApiResponse<GenericResponse<T>>
	): APIResult<T> {
		contract {
			callsInPlace(call, InvocationKind.EXACTLY_ONCE)
		}
		return when (val response = call()) {
			is ApiResponse.Success -> {
				val result = response.data
				if (result.success && result.data != null) {
					APIResult.Success(result.data)
				} else {
					handleFailure(
						result = result,
						statusCode = response.statusCode.code,
						logDescription = logDescription
					)
				}
			}

			is ApiResponse.Failure.Error -> {
				handleFailureError(
					response = response,
					logDescription = logDescription
				)
			}

			is ApiResponse.Failure.Exception -> {
				handleFailureException(
					response = response,
					logDescription = logDescription
				)
			}
		}
	}

	/**
	 * Executes an API call that returns a [BaseResponse] and processes the response into an [APIResult].
	 *
	 * This method is similar to [doAPICall] but handles API calls that return a [BaseResponse]
	 * instead of a [GenericResponse].
	 *
	 * @param logDescription Description of the API call for logging purposes
	 * @param call Lambda that performs the actual API call and returns an [ApiResponse]
	 * @return An [APIResult] representing the outcome of the API call
	 */
	protected inline fun doAPICallWithBaseResponse(
		logDescription: String,
		call: () -> ApiResponse<BaseResponse>
	): APIResult<BaseResponse> {
		contract {
			callsInPlace(call, InvocationKind.EXACTLY_ONCE)
		}
		return when (val response = call()) {
			is ApiResponse.Success -> {
				val result = response.data
				if (result.success) {
					APIResult.Success(result)
				} else {
					handleFailure(
						result = result,
						statusCode = response.statusCode.code,
						logDescription = logDescription
					)
				}
			}

			is ApiResponse.Failure.Error -> {
				handleFailureError(
					response = response,
					logDescription = logDescription
				)
			}

			is ApiResponse.Failure.Exception -> {
				handleFailureException(
					response = response,
					logDescription = logDescription
				)
			}
		}
	}

	/**
	 * Converts an API call into a Flow that emits loading state followed by the API result.
	 *
	 * @param apiCall Suspend function that performs the actual API call
	 * @return A Flow that emits [APIFlowState.Loading] followed by the result of the API call
	 */
	protected inline fun <T : Any> callApiAsFlow(
		crossinline apiCall: suspend () -> APIResult<T>
	): Flow<APIFlowState<T>> = flow {
		emit(APIFlowState.Loading)
		emit(apiCall().asApiFlowState())
	}

	/**
	 * Creates a [RefreshableAPIFlow] that wraps an API call, providing refresh functionality.
	 *
	 * @param initialValue Optional initial value to use before the first API call completes
	 * @param initialRefresh Whether to automatically refresh the data when created (defaults to true if initialValue is null)
	 * @param scope CoroutineScope to use for API calls
	 * @param apiCall Suspend function that performs the actual API call
	 * @return A [RefreshableAPIFlow] that wraps the API call
	 */
	protected fun <T : Any> callApiAsRefreshableFlow(
		initialValue: T? = null,
		initialRefresh: Boolean = initialValue == null,
		scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
		apiCall: suspend () -> APIResult<T>
	): RefreshableAPIFlow<T> {
		return RefreshableAPIFlow(
			initialValue = initialValue,
			initialRefresh = initialRefresh,
			apiCall = apiCall,
			scope = scope
		)
	}

	protected fun handleFailure(
		result: RootJson,
		statusCode: Int,
		logDescription: String
	): APIResult.Error {
		val errors = result.errors ?: listOf("Unknown error")
		val messages = result.messages
		BaseRepoLog.e(
			caller = this,
			msg = "$logDescription failed! code:$statusCode, messages:\"$messages\", errors:\"$errors\""
		)
		return APIResult.Error(responseCode = statusCode, messages = messages, errors = errors)
	}

	protected fun handleFailureError(response: ApiResponse.Failure.Error, logDescription: String): APIResult.Error {
		var messages: List<String>? = null
		var errors: List<String>
		try {
			val errorBody = response.errorBody.parseBody<BaseResponse>(getRetrofitClient())
			errors = errorBody?.errors ?: listOf("Unknown error")
			messages = errorBody?.messages
			BaseRepoLog.e(
				caller = this,
				msg = "$logDescription failed! code:${response.statusCode.code}, messages:\"$messages\", errors:\"$errors\""
			)
		} catch (e: Exception) {
			BaseRepoLog.e(
				caller = this,
				msg = "$logDescription failed! code:${response.statusCode.code} - Failed to parse error body",
				tr = e
			)
			errors = listOf("Unknown error")
		}
		return APIResult.Error(responseCode = response.statusCode.code, messages = messages, errors = errors)
	}

	protected fun handleFailureException(response: ApiResponse.Failure.Exception, logDescription: String): APIResult.Error {
		return when (response.throwable) {
			is NoConnectivityException,
			is UnknownHostException,
			is ConnectException,
			is SocketException,
			is SocketTimeoutException -> {
				BaseRepoLog.w(
					caller = this,
					msg = "$logDescription failed Due to No Connection!",
					tr = response.throwable
				)
				APIResult.Error(
					responseCode = RESPONSE_EXCEPTION_CODE,
					errors = listOf("No Internet Connection"),
					throwable = response.throwable.asNoConnectivityException()
				)
			}

			else -> {
				val message = firstNotNullOrBlank(
					{ response.throwable.message },
					{ response.message() },
					fallback = { "Unknown error" }
				)
				BaseRepoLog.e(
					caller = this,
					msg = "$logDescription failed with exception! message:\"$message\"",
					tr = response.throwable
				)
				APIResult.Error(
					responseCode = RESPONSE_EXCEPTION_CODE,
					errors = listOf(message),
					throwable = response.throwable
				)
			}
		}
	}
}
