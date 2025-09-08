package uk.co.appoly.droid.data.remote.model.response

import kotlinx.serialization.Serializable

/**
 * Base response model for API responses.
 *
 * This class represents the common structure shared by all API responses
 * in the system, containing success status and an optional message.
 *
 * Example JSON:
 * ```json
 * {
 *   "status": "success"
 * }
 * ```
 *
 * Or with a message:
 * ```json
 * {
 *   "status": "success",
 *   "messages": ["Operation completed successfully"]
 * }
 * ```
 *
 * Or for an error (a message, while optional, is expected to be present in error cases):
 * ```json
 * {
 *   "status": "error",
 *   "messages": ["Invalid credentials"],
 *   "errors": ["Email format is incorrect", "Password is too short"]
 * }
 * ```
 *
 * @property status The status of the response, indicating success or error
 * @property messages Optional message(s) providing additional information about the response
 * @property errors Optional list of error messages, typically present in error responses
 */
@Serializable
data class BaseResponse(
	override val status: ResponseStatus = ResponseStatus.Error,
	override val messages: List<String>? = null,
	override val errors: List<String>? = null
) : RootJson
