package uk.co.appoly.droid.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class GenericResponse<T>(
	val success: Boolean = false,
	val message: String? = null,
	val data: T? = null
)

@Serializable
data class ErrorBody(
	val success: Boolean = false,
	val message: String? = null,
	val errors: Map<String, List<String>>? = null
)