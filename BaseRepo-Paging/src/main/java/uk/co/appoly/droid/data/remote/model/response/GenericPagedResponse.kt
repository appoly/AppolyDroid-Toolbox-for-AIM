package uk.co.appoly.droid.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Response model for paginated API responses with a flat data structure.
 *
 * This class represents paginated API responses where pagination metadata is included
 * alongside the list of items, following a standard response format with status, messages, and errors fields.
 *
 * Example JSON:
 * ```json
 * {
 *   "status": "success",
 *   "messages": ["Data retrieved successfully"],
 *   "data": [
 *     { "id": 1, "name": "Item 1" },
 *     { "id": 2, "name": "Item 2" }
 *   ],
 *   "total_records": 48,
 *   "filtered_records": 10
 * }
 * ```
 *
 * @param T The type of items in the paginated list
 * @property status Indicates the status of the API request (e.g., success, error)
 * @property messages Optional list of messages providing additional information about the response
 * @property errors Optional list of error messages if the request failed
 * @property data The list of items in the current page
 * @property totalRecords Total number of items across all pages
 * @property filteredRecords Number of items in the current filtered result set
 */
@Serializable
data class GenericPagedResponse<T>(
	override val status: ResponseStatus = ResponseStatus.Error,
	override val messages: List<String>? = null,
	override val errors: List<String>? = null,
	val data: List<T>,
	@SerialName("total_records")
	val totalRecords: Int,
	@SerialName("filtered_records")
	val filteredRecords: Int,
) : RootJson