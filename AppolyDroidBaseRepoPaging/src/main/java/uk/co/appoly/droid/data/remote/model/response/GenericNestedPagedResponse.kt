package uk.co.appoly.droid.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenericNestedPagedResponse<T>(
	val success: Boolean = false,
	val message: String?,
	@SerialName("data")
	val pageData: NestedPageData<T>?
)

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
	constructor(response: GenericNestedPagedResponse<T>) : this(
		data = response.pageData?.data ?: emptyList(),
		currentPage = response.pageData?.currentPage ?: 1,
		lastPage = response.pageData?.lastPage ?: 1,
		perPage = response.pageData?.perPage ?: 0,
		from = response.pageData?.from ?: 0,
		to = response.pageData?.to ?: 0,
		total = response.pageData?.total ?: 0
	)

	val itemsBefore: Int = if (from > 0) from - 1 else 0
	val itemsAfter: Int = if (to < total) total - to else 0
	val prevPage: Int? = if (currentPage > 1) currentPage - 1 else null
	val nextPage: Int? = if (currentPage < lastPage) currentPage + 1 else null
}