package uk.co.appoly.droid.data.repo

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.retrofit.statusCode
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.data.remote.model.response.GenericPagedResponse
import uk.co.appoly.droid.data.remote.model.response.PageData
import uk.co.appoly.droid.data.remote.model.response.ResponseStatus

/**
 * Extension function for AppolyBaseRepo to handle API calls that return paginated data
 * with a flat structure.
 *
 * This function processes [GenericPagedResponse] responses and converts them to [PageData]
 * for easier consumption by Paging components. It correctly handles both filtered and
 * unfiltered result sets by using `filteredRecords` for pagination calculations.
 *
 * ## Pagination Behavior
 * - When `length > 0`: Returns a specific page of results with pagination metadata
 * - When `length == -1`: Returns all remaining items starting from `startIndex` as a single page
 *
 * ## Important Notes
 * - This function assumes the API accepts start index and length parameters for pagination
 * - Pagination metadata in [PageData] is calculated based on client-side `startIndex`/`length`
 *   and the server's `filteredRecords` count
 * - `filteredRecords` is used for all pagination calculations, ensuring correct page counts
 *   when server-side filtering is applied
 * - Input validation ensures `startIndex >= 0` and `length` is either `-1` or positive
 * - Server response validation ensures `filteredRecords` is non-negative and `lastPage >= 1`
 *
 * ## Example usage
 * ```kotlin
 * // Fetch page 2 with 20 items per page
 * suspend fun fetchUserList(page: Int, perPage: Int): APIResult<PageData<User>> =
 *    doPagedAPICall("fetchUserList", startIndex = (page - 1) * perPage, length = perPage) {
 *        userService.api.getUsers(start = (page - 1) * perPage, length = perPage)
 *    }
 *
 * // Fetch all users starting from index 0
 * suspend fun fetchAllUsers(): APIResult<PageData<User>> =
 *    doPagedAPICall("fetchAllUsers", startIndex = 0, length = -1) {
 *        userService.api.getUsers(start = 0, length = -1)
 *    }
 * ```
 *
 * @param T The type of items in the paginated list
 * @param logDescription Description for logging purposes
 * @param startIndex The starting index for pagination (0-based, passed to the API). Must be >= 0
 * @param length The number of items to fetch per page. Use -1 to fetch all remaining items, or any positive value for specific page size
 * @param call Lambda that performs the API call and returns an [ApiResponse] with [GenericPagedResponse]
 * @return An [APIResult] wrapping [PageData] with pagination metadata based on filtered results
 * @throws [IllegalArgumentException] if startIndex < 0 or length == 0 or length < -1
 */
inline fun <T : Any> AppolyBaseRepo.doPagedAPICall(
	logDescription: String,
	startIndex: Int = 0,
	length: Int = 50,
	call: () -> ApiResponse<GenericPagedResponse<T>>
): APIResult<PageData<T>> {
	// Validate inputs
	require(startIndex >= 0) { "startIndex must be non-negative" }
	require(length == -1 || length > 0) { "length must be -1 (all items) or positive non-zero value" }
	return when (val response = call()) {
		is ApiResponse.Success -> {
			val result = response.data
			if (result.status == ResponseStatus.Success) {
				// Validate server response
				val validFilteredRecords = result.filteredRecords.coerceAtLeast(0)
				APIResult.Success(
					PageData(
						data = result.data,
						currentPage = if (length == -1) 1 else (startIndex / length) + 1,
						lastPage = if (length == -1) {
							1
						} else {
							// Safe division - prevent overflow
							maxOf(1, (validFilteredRecords + length - 1) / length)
						},
						perPage = if (length == -1) validFilteredRecords else length,
						from = if (result.data.isNotEmpty()) startIndex + 1 else 0,
						to = if (result.data.isNotEmpty()) startIndex + result.data.size else 0,
						total = validFilteredRecords
					)
				)
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
