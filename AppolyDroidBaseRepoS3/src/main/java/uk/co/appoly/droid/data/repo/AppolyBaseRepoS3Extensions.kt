package uk.co.appoly.droid.data.repo

import kotlinx.coroutines.flow.MutableStateFlow
import uk.co.appoly.droid.data.remote.model.APIResult
import uk.co.appoly.s3imageupload.S3Uploader
import uk.co.appoly.s3imageupload.UploadResult
import java.io.File

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
			APIResult.Success(uploadResult.filePath)
		}

		is UploadResult.Error -> {
			APIResult.Error(uploadResult.message, uploadResult.throwable)
		}
	}
}

suspend inline fun <T : Any> AppolyBaseRepo.uploadFileToS3(
	generatePresignedURL: String,
	file: File,
	sendPathApiCall: (String) -> APIResult<T>
): APIResult<T> {
	val uploadResult = S3Uploader.uploadFile(file, generatePresignedURL)
	return when (uploadResult) {
		is UploadResult.Error -> {
			APIResult.Error(uploadResult.message)
		}

		is UploadResult.Success -> {
			sendPathApiCall(uploadResult.filePath)
		}
	}
}