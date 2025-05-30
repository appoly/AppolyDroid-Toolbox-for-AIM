package uk.co.appoly.droid.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response model for paginated API responses with a nested data structure.
 *
 * This class represents paginated API responses where pagination metadata is nested
 * inside a data field, following the standard response format with success and message fields.
 *
 * Example JSON:
 * ```json
 * {
 *   "success": true,
 *   "message": "Data retrieved successfully",
 *   "data": {
 *     "data": [
 *       { "id": 1, "name": "Item 1" },
 *       { "id": 2, "name": "Item 2" }
 *     ],
 *     "current_page": 1,
 *     "last_page": 5,
 *     "per_page": 10,
 *     "from": 1,
 *     "to": 10,
 *     "total": 48
 *   }
 * }
 * ```
 *
 * @param T The type of items in the paginated list
 * @property success Indicates whether the API request was successful
 * @property message Optional message providing additional information about the response
 * @property pageData Nested page data containing the items and pagination metadata
 */
@Serializable
data class GenericNestedPagedResponse<T>(
	val success: Boolean = false,
	val message: String?,
	@SerialName("data")
	val pageData: NestedPageData<T>?
)

/**
 * Represents the nested page data structure within the API response.
 *
 * Contains both the list of items and the pagination metadata.
 *
 * @param T The type of items in the paginated list
 * @property data The list of items in the current page
 * @property currentPage The current page number
 * @property lastPage The last page number (total number of pages)
 * @property perPage Number of items per page
 * @property from Index of the first item on the current page
 * @property to Index of the last item on the current page
 * @property total Total number of items across all pages
 */
@Serializable
data class NestedPageData<T>(
	val data: List<T>?,
	@SerialName("current_page")
	val currentPage: Int?,
	@SerialName("last_page")
	val lastPage: Int?,
	@SerialName("per_page")
	val perPage: Int?,
	val from: Int?,
	val to: Int?,
	val total: Int?
)

/**
 * Flattened representation of paginated data with normalized properties.
 *
 * This class provides a simpler access pattern to the paginated data by
 * extracting the nested data structure into a flat object with non-null
 * properties and additional helper properties for pagination.
 *
 * @param T The type of items in the paginated list
 * @property data The list of items in the current page (never null, empty list if no data)
 * @property currentPage The current page number (1-based)
 * @property lastPage The last page number (total number of pages)
 * @property perPage Number of items per page
 * @property from Index of the first item on the current page
 * @property to Index of the last item on the current page
 * @property total Total number of items across all pages
 * @property itemsBefore Number of items before the current page
 * @property itemsAfter Number of items after the current page
 * @property prevPage Previous page number or null if current page is the first page
 * @property nextPage Next page number or null if current page is the last page
 */
@Serializable
data class PageData<T>(
	val data: List<T>,
	@SerialName("current_page")
	val currentPage: Int,
	@SerialName("last_page")
	val lastPage: Int,
	@SerialName("per_page")
	val perPage: Int,
	val from: Int,
	val to: Int,
	val total: Int
) {
	/**
	 * Constructs a PageData instance from a GenericNestedPagedResponse.
	 *
	 * This constructor handles null values in the nested response by providing
	 * sensible defaults.
	 *
	 * @param response The nested paged response to convert
	 */
	constructor(response: GenericNestedPagedResponse<T>) : this(
		data = response.pageData?.data ?: emptyList(),
		currentPage = response.pageData?.currentPage ?: 1,
		lastPage = response.pageData?.lastPage ?: 1,
		perPage = response.pageData?.perPage ?: 0,
		from = response.pageData?.from ?: 0,
		to = response.pageData?.to ?: 0,
		total = response.pageData?.total ?: 0
	)

	/**
	 * Number of items before the current page.
	 * Calculated based on the 'from' field.
	 */
	val itemsBefore: Int = if (from > 0) from - 1 else 0

	/**
	 * Number of items after the current page.
	 * Calculated based on the 'to' and 'total' fields.
	 */
	val itemsAfter: Int = if (to < total) total - to else 0

	/**
	 * Previous page number or null if current page is the first page.
	 */
	val prevPage: Int? = if (currentPage > 1) currentPage - 1 else null

	/**
	 * Next page number or null if current page is the last page.
	 */
	val nextPage: Int? = if (currentPage < lastPage) currentPage + 1 else null
}
