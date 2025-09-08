package uk.co.appoly.droid.data.repo

import kotlinx.coroutines.flow.MutableStateFlow
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.droid.s3upload.S3Uploader
import uk.co.appoly.droid.s3upload.UploadResult
import java.io.File

/**
 * Uploads a file to Amazon S3 storage using a pre-signed URL.
 *
 * This extension function simplifies the process of uploading files to S3 by handling
 * the upload process and converting the [UploadResult] to the standard [APIResult] format
 * used throughout the repository layer.
 *
 * Example usage:
 * ```
 * val result = uploadFileToS3(
 *     generatePresignedURL = "https://api.example.com/generate-url",
 *     file = File("/path/to/file.jpg"),
 *     progressFlow = progressStateFlow
 * )
 * ```
 *
 * @param generatePresignedURL The API endpoint URL that generates a pre-signed S3 URL
 * @param file The file to upload to S3
 * @param progressFlow Optional MutableStateFlow to track upload progress (0.0f to 1.0f)
 * @return [APIResult.Success] with the S3 file path if successful, or [APIResult.Error] with error details if failed
 */
suspend inline fun AppolyBaseRepo.uploadFileToS3(
	generatePresignedURL: String,
	file: File,
	progressFlow: MutableStateFlow<Float>? = null
): APIResult<String> {
	val uploadResult = S3Uploader.uploadFile(
		file = file,
		getPresignedUrlAPI = generatePresignedURL,
		progressFlow = progressFlow
	)
	return when (uploadResult) {
		is UploadResult.Success -> {
			APIResult.Success(data = uploadResult.filePath)
		}

		is UploadResult.Error -> {
			APIResult.Error(
				message = null,
				error = uploadResult.message,
				throwable = uploadResult.throwable
			)
		}
	}
}

/**
 * Uploads a file to Amazon S3 storage and then sends the resulting file path to another API endpoint.
 *
 * This function combines two operations:
 * 1. Upload a file to S3 using a pre-signed URL
 * 2. Send the resulting file path to another API endpoint
 *
 * This is useful for scenarios where you need to upload a file and then associate it with a database record
 * or perform additional server-side processing on the uploaded file.
 *
 * Example usage:
 * ```
 * val result = uploadFileToS3(
 *     generatePresignedURL = "https://api.example.com/generate-url",
 *     file = File("/path/to/file.jpg"),
 *     sendPathApiCall = { path -> userRepository.updateProfilePicture(userId, path) }
 * )
 * ```
 *
 * @param generatePresignedURL The API endpoint URL that generates a pre-signed S3 URL
 * @param file The file to upload to S3
 * @param sendPathApiCall Lambda function that takes the S3 file path and makes an additional API call
 * @return [APIResult] from the secondary API call if the upload was successful, or [APIResult.Error] if the upload failed
 */
suspend inline fun <T : Any> AppolyBaseRepo.uploadFileToS3(
	generatePresignedURL: String,
	file: File,
	sendPathApiCall: (String) -> APIResult<T>
): APIResult<T> {
	val uploadResult = S3Uploader.uploadFile(file, generatePresignedURL)
	return when (uploadResult) {
		is UploadResult.Error -> {
			APIResult.Error(message = null, error = uploadResult.message)
		}

		is UploadResult.Success -> {
			sendPathApiCall(uploadResult.filePath)
		}
	}
}
