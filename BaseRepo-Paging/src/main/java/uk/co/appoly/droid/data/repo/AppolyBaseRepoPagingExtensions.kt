package uk.co.appoly.droid.data.repo

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.errorBody
import com.skydoves.sandwich.retrofit.statusCode
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.remote.model.response.BaseResponse
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

/**
 * Extension function for AppolyBaseRepo to handle API calls that return paginated data
 * with a nested structure.
 *
 * This function is similar to [AppolyBaseRepo.doAPICall] but specifically processes
 * [GenericNestedPagedResponse] responses and converts them to [PageData] for easier
 * consumption by Paging components.
 *
 * Example usage:
 * ```kotlin
 * suspend fun fetchUserList(page: Int): APIResult<PageData<User>> =
 *     doNestedPagedAPICall("fetchUserList") {
 *         userService.api.getUsers(page = page, perPage = 20)
 *     }
 * ```
 *
 * @param T The type of items in the paginated list
 * @param logDescription Description for logging purposes
 * @param call Lambda that performs the API call and returns an [ApiResponse] with [GenericNestedPagedResponse]
 * @return An [APIResult] wrapping [PageData] with the normalized pagination data
 */
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
					caller = this,
					msg = "$logDescription failed! code:${response.statusCode.code}, message:\"$message\""
				)
				APIResult.Error(
					responseCode = response.statusCode.code,
					errors = listOf(message)
				)
			}
		}

		is ApiResponse.Failure.Error -> {
			var messages: List<String>? = null
			var errors: List<String>
			try {
				val errorBody = response.errorBody.parseBody<BaseResponse>(getRetrofitClient())
				errors = errorBody?.errors ?: listOf("Unknown error")
				messages = errorBody?.messages
				logger.e(
					caller = this,
					msg = "$logDescription failed! code:${response.statusCode.code}, messages:\"$messages\", errors:\"$errors\""
				)
			} catch (e: Exception) {
				logger.e(
					caller = this,
					msg = "$logDescription failed! code:${response.statusCode.code} - Failed to parse error body",
					tr = e
				)
				errors = listOf("Unknown error")
			}
			APIResult.Error(
				responseCode = response.statusCode.code,
				messages = messages,
				errors = errors
			)
		}

		is ApiResponse.Failure.Exception -> {
			when (response.throwable) {
				is NoConnectivityException,
				is UnknownHostException,
				is ConnectException,
				is SocketException,
				is SocketTimeoutException -> {
					logger.w(
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
					logger.e(
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
}
