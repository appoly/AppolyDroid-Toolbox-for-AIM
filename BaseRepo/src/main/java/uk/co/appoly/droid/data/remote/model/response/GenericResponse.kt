package uk.co.appoly.droid.data.remote.model.response

import kotlinx.serialization.Serializable

/**
 * Generic response model for API responses that include data payload.
 *
 * This class extends the common response structure by adding a typed data field
 * that can contain any type of response data.
 *
 * Example JSON for a successful user profile response:
 * ```json
 * {
 *   "status": "success",
 *   "messages": ["Profile retrieved successfully"],
 *   "data": {
 *     "id": 123,
 *     "name": "John Doe",
 *     "email": "john.doe@example.com"
 *   }
 * }
 * ```
 *
 * Example JSON for a successful list response:
 * ```json
 * {
 *   "status": "success",
 *   "data": [
 *     { "id": 1, "name": "Item 1" },
 *     { "id": 2, "name": "Item 2" }
 *   ]
 * }
 * ```
 *
 * Example JSON for an error:
 * ```json
 * {
 *   "status": "error",
 *   "messages": ["Resource not found"],
 *   "errors": ["The requested item does not exist"],
 *   "data": null
 * }
 * ```
 *
 * @param T The type of data payload contained in the response
 * @property status The status of the response, indicating success or error
 * @property messages Optional message(s) providing additional information about the response
 * @property errors Optional list of error messages, typically present in error responses
 * @property data Optional payload data returned by the API
 */
@Serializable
data class GenericResponse<T>(
	override val status: ResponseStatus = ResponseStatus.Error,
	override val messages: List<String>? = null,
	override val errors: List<String>? = null,
	val data: T? = null
) : RootJson

/**
 * Error response model for API error responses.
 *
 * This class represents error responses from the API, including validation errors
 * with field-specific error messages.
 *
 * Example JSON for a general error:
 * ```json
 * {
 *   "status": "error",
 *   "messages": ["Authentication failed"]
 * }
 * ```
 *
 * Example JSON for validation errors:
 * ```json
 * {
 *   "status": "error",
 *   "messages": ["Validation failed"]
 *   "errors": ["Email is required", "Password must be at least 8 characters long"]
 * }
 * ```
 *
 * @property status The status of the response, indicating success or error
 * @property messages Optional message(s) providing additional information about the response
 * @property errors Optional list of error messages, typically present in error responses
 *
 * @deprecated Use [BaseResponse] instead, as it serves the same purpose with a more general name.
 */
@Deprecated("Use BaseResponse instead", ReplaceWith("BaseResponse"))
@Serializable
data class ErrorBody(
	val status: ResponseStatus = ResponseStatus.Error,
	val messages: List<String>? = null,
	val errors: List<String>? = null
)
