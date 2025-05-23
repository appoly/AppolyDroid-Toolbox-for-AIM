package uk.co.appoly.droid.s3upload.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPreSignedUrlBody(
	@SerialName("file_name")
	val fileName: String
)