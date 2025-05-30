package uk.co.appoly.droid.s3upload.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response model for the pre-signed URL generation endpoint.
 *
 * This class represents the structure of the API response when requesting
 * a pre-signed URL for S3 file uploads.
 *
 * Example JSON:
 * ```json
 * {
 *   "success": true,
 *   "data": {
 *     "file_path": "images/profile/user123.jpg",
 *     "presigned_url": "https://bucket-name.s3.amazonaws.com/...",
 *     "headers": {
 *       "Host": ["bucket-name.s3.amazonaws.com"],
 *       "x-amz-acl": ["public-read"],
 *       "Content-Type": "image/jpeg"
 *     }
 *   }
 * }
 * ```
 *
 * @property success Indicates whether the request was successful
 * @property message Optional message providing additional information
 * @property data The pre-signed URL data, if successful
 */
@Serializable
data class GetPreSignedUrlResponse(
	val success: Boolean = false,
	val message: String? = "No message from server",
	val data: PreSignedURLData?
)

/**
 * Data model containing the pre-signed URL and related information.
 *
 * @property filePath The path/key where the file will be stored in S3
 * @property presignedUrl The generated pre-signed URL for the S3 upload
 * @property headers HTTP headers to include with the S3 upload request
 */
@Serializable
data class PreSignedURLData(
	@SerialName("file_path")
	val filePath: String,
	@SerialName("presigned_url")
	val presignedUrl: String,
	val headers: S3Headers
)

/**
 * Headers required for S3 upload requests.
 *
 * Amazon S3 requires specific headers for authenticated uploads using
 * pre-signed URLs. This class models those required headers.
 *
 * @property host The S3 bucket host name
 * @property xAmzAcl Access control list (ACL) settings for the uploaded file
 * @property contentType Content type (MIME type) of the file being uploaded
 */
@Serializable
data class S3Headers(
	@SerialName("Host")
	val host: List<String>,
	@SerialName("x-amz-acl")
	val xAmzAcl: List<String>,
	@SerialName("Content-Type")
	val contentType: String
) {
	/**
	 * Converts the header values to a map suitable for HTTP requests.
	 *
	 * Since some headers might have multiple values (stored as lists),
	 * this property joins them into single strings.
	 */
	val asMap: Map<String, String>
		get() = mapOf(
			Pair("Host", host.joinToString()),
			Pair("x-amz-acl", xAmzAcl.joinToString()),
			Pair("Content-Type", contentType)
		)
}
