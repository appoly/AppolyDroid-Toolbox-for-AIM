package uk.co.appoly.droid.s3upload.network

import kotlinx.serialization.Serializable

@Serializable
internal data class ErrorBody(
	val message: String?,
	val errors: Map<String, List<String>>?
)