package uk.co.appoly.droid.data.repo

import com.duck.flexilogger.FlexiLog
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.errorBody
import com.skydoves.sandwich.retrofit.statusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.co.appoly.droid.BaseRepoLogger
import uk.co.appoly.droid.data.remote.BaseService
import uk.co.appoly.droid.data.remote.BaseRetrofitClient
import uk.co.appoly.droid.data.remote.ServiceManager
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.remote.model.response.BaseResponse
import uk.co.appoly.droid.data.remote.model.response.ErrorBody
import uk.co.appoly.droid.data.remote.model.response.GenericResponse
import uk.co.appoly.droid.util.NoConnectivityException
import uk.co.appoly.droid.util.asNoConnectivityException
import uk.co.appoly.droid.util.firstNotNullOrBlank
import uk.co.appoly.droid.util.ifNullOrBlank
import uk.co.appoly.droid.util.parseBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
abstract class AppolyBaseRepo(
	val getRetrofitClient: () -> BaseRetrofitClient
) {
	open val logger: FlexiLog = BaseRepoLogger

	fun getServiceManager(): ServiceManager {
		return ServiceManager.getInstance(
			getRetrofitClient = { getRetrofitClient() },
			getLogger = { logger }
		)
	}

	companion object {
		const val RESPONSE_EXCEPTION_CODE = -1
	}

	/**
	 * Helper method to lazily initialize a service
	 */
	protected inline fun <reified T : BaseService.API> AppolyBaseRepo.lazyService(): Lazy<BaseService<T>> =
		lazy { getServiceManager().getService() }

	protected inline fun <T : Any> doAPICall(
		logDescription: String,
		call: () -> ApiResponse<GenericResponse<T>>
	): APIResult<T> {
		contract {
			callsInPlace(call, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
		}
		return when (val response = call()) {
			is ApiResponse.Success -> {
				val result = response.data
				if (result.success && result.data != null) {
					APIResult.Success(result.data)
				} else {
					val message = result.message.ifNullOrBlank { "Unknown error" }
					logger.e(
						this,
						"$logDescription failed! code:${response.statusCode.code}, message:\"$message\""
					)
					APIResult.Error(response.statusCode.code, message)
				}
			}

			is ApiResponse.Failure.Error -> {
				val message =
					firstNotNullOrBlank(
						{ response.errorBody.parseBody<ErrorBody>(getRetrofitClient())?.message },
						{ response.message() },
						fallback = { "Unknown error" }
					)
				logger.e(
					this,
					"$logDescription failed! code:${response.statusCode.code}, message:\"$message\""
				)
				APIResult.Error(response.statusCode.code, message)
			}

			is ApiResponse.Failure.Exception -> {
				when (response.throwable) {
					is NoConnectivityException,
					is UnknownHostException,
					is ConnectException,
					is SocketException,
					is SocketTimeoutException -> {
						logger.w(
							this,
							"$logDescription failed Due to No Connection!",
							response.throwable
						)
						APIResult.Error(
							RESPONSE_EXCEPTION_CODE,
							"No Internet Connection",
							response.throwable.asNoConnectivityException()
						)
					}

					else -> {
						val message = firstNotNullOrBlank(
							{ response.throwable.message },
							{ response.message() },
							fallback = { "Unknown error" }
						)
						logger.e(
							this,
							"$logDescription failed with exception! message:\"$message\"",
							response.throwable
						)
						APIResult.Error(RESPONSE_EXCEPTION_CODE, message, response.throwable)
					}
				}
			}
		}
	}

	protected inline fun doAPICallWithBaseResponse(
		logDescription: String,
		call: () -> ApiResponse<BaseResponse>
	): APIResult<BaseResponse> {
		contract {
			callsInPlace(call, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
		}
		return when (val response = call()) {
			is ApiResponse.Success -> {
				val result = response.data
				if (result.success) {
					APIResult.Success(result)
				} else {
					val message = result.message.ifNullOrBlank { "Unknown error" }
					logger.e(
						this,
						"$logDescription failed! code:${response.statusCode.code}, message:\"$message\""
					)
					APIResult.Error(response.statusCode.code, message)
				}
			}

			is ApiResponse.Failure.Error -> {
				val message =
					firstNotNullOrBlank(
						{ response.errorBody.parseBody<ErrorBody>(getRetrofitClient())?.message },
						{ response.message() },
						fallback = { "Unknown error" }
					)
				logger.e(
					this,
					"$logDescription failed! code:${response.statusCode.code}, message:\"$message\""
				)
				APIResult.Error(response.statusCode.code, message)
			}

			is ApiResponse.Failure.Exception -> {
				when (response.throwable) {
					is NoConnectivityException,
					is UnknownHostException,
					is ConnectException,
					is SocketException,
					is SocketTimeoutException -> {
						logger.w(
							this,
							"$logDescription failed Due to No Connection!",
							response.throwable
						)
						APIResult.Error(RESPONSE_EXCEPTION_CODE, "No Internet Connection", response.throwable)
					}

					else -> {
						val message = firstNotNullOrBlank(
							{ response.throwable.message },
							{ response.message() },
							fallback = { "Unknown error" }
						)
						logger.e(
							this,
							"$logDescription failed with exception! message:\"$message\"",
							response.throwable
						)
						APIResult.Error(RESPONSE_EXCEPTION_CODE, message, response.throwable)
					}
				}
			}
		}
	}

	protected inline fun <T : Any> callApiAsFlow(
		crossinline apiCall: suspend () -> APIResult<T>
	): Flow<APIFlowState<T>> = flow {
		emit(APIFlowState.Loading)
		emit(apiCall().asApiFlowState())
	}

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
}