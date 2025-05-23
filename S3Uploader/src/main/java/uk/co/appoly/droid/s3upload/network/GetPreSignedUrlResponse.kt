package uk.co.appoly.droid.s3upload.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPreSignedUrlResponse(
	val success: Boolean = false,
	val message: String? = "No message from server",
	val data: PreSignedURLData?
)

@Serializable
data class PreSignedURLData(
	@SerialName("file_path")
	val filePath: String,
	@SerialName("presigned_url")
	val presignedUrl: String,
	val headers: S3Headers
)

@Serializable
data class S3Headers(
	@SerialName("Host")
	val host: List<String>,
	@SerialName("x-amz-acl")
	val xAmzAcl: List<String>,
	@SerialName("Content-Type")
	val contentType: String
) {
	val asMap: Map<String, String>
		get() = mapOf(
			Pair("Host", host.joinToString()),
			Pair("x-amz-acl", xAmzAcl.joinToString()),
			Pair("Content-Type", contentType)
		)
}