package uk.co.appoly.s3imageupload.network

import kotlinx.serialization.Serializable

@Serializable
internal data class ErrorBody(
	val message: String?,
	val errors: Map<String, List<String>>?
)