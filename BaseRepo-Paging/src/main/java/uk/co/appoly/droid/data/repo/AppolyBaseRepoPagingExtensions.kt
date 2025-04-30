package uk.co.appoly.droid.data.repo

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.errorBody
import com.skydoves.sandwich.retrofit.statusCode
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.remote.model.response.ErrorBody
import uk.co.appoly.droid.data.remote.model.response.GenericNestedPagedResponse
import uk.co.appoly.droid.data.remote.model.response.PageData
import uk.co.appoly.droid.data.repo.AppolyBaseRepo.Companion.RESPONSE_EXCEPTION_CODE
import uk.co.appoly.droid.util.NoConnectivityException
import uk.co.appoly.droid.util.asNoConnectivityException
import uk.co.appoly.droid.util.firstNotNullOrBlank
import uk.co.appoly.droid.util.ifNullOrBlank
import uk.co.appoly.droid.util.parseBody
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

inline fun <T : Any> AppolyBaseRepo.doNestedPagedAPICall(
	logDescription: String,
	call: () -> ApiResponse<GenericNestedPagedResponse<T>>
): APIResult<PageData<T>> {
	return when (val response = call()) {
		is ApiResponse.Success -> {
			val result = response.data
			if (result.success && result.pageData != null) {
				APIResult.Success(PageData(result))
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