package uk.co.appoly.s3imageupload.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPreSignedUrlBody(
	@SerialName("file_name")
	val fileName: String
)