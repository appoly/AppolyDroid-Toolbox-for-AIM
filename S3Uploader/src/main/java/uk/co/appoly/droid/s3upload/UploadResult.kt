package uk.co.appoly.droid.s3upload

sealed class UploadResult {
	data class Success(val filePath: String): UploadResult()
	data class Error(val message: String, val throwable: Throwable? = null): UploadResult()
}