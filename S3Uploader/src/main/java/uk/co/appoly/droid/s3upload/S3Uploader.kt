package uk.co.appoly.droid.s3upload

import android.webkit.MimeTypeMap
import com.duck.flexilogger.FlexiLog
import com.duck.flexilogger.LogType
import com.duck.flexilogger.LoggerWithLevel
import com.duck.flexilogger.LoggingLevel
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.errorBody
import com.skydoves.sandwich.retrofit.statusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import uk.co.appoly.droid.s3upload.interfaces.AuthTokenProvider
import uk.co.appoly.droid.s3upload.network.ErrorBody
import uk.co.appoly.droid.s3upload.network.GetPreSignedUrlResponse
import uk.co.appoly.droid.s3upload.network.PreSignedURLData
import uk.co.appoly.droid.s3upload.network.ProgressRequestBody
import uk.co.appoly.droid.s3upload.network.RetrofitClient
import uk.co.appoly.droid.s3upload.utils.S3UploadLogger
import uk.co.appoly.droid.s3upload.utils.firstNotNullOrBlank
import uk.co.appoly.droid.s3upload.utils.parseBody
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object S3Uploader {
	private lateinit var tokenProvider: AuthTokenProvider
	internal var loggingLevel: LoggingLevel = LoggingLevel.NONE
	internal var Log: FlexiLog = S3UploadLogger
	internal var LoggerWithLevel: LoggerWithLevel = Log.withLevel(loggingLevel)

	internal fun canLog(type: LogType): Boolean = loggingLevel.canLog(type)

	private fun isInitDone(): Boolean {
		return this::tokenProvider.isInitialized
	}

	fun initS3Uploader(
		tokenProvider: AuthTokenProvider,
		loggingLevel: LoggingLevel = LoggingLevel.NONE,
		logger: FlexiLog = Log
	) {
		this.tokenProvider = tokenProvider
		this.loggingLevel = loggingLevel
		this.Log = logger
		this.LoggerWithLevel = logger.withLevel(loggingLevel)
	}

	private fun getMimeType(file: File): String? {
		var mimeType: String? = null
		val extension: String = file.name.split(".").last()
		if (MimeTypeMap.getSingleton().hasExtension(extension)) {
			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
		}
		return mimeType
	}

	suspend fun uploadFileAsync(
		file: File,
		getPresignedUrlAPI: String,
		dispatcher: CoroutineDispatcher = Dispatchers.IO,
		progressFlow: MutableStateFlow<Float>? = null, // Optional progress tracking
	): Deferred<UploadResult>  = withContext(dispatcher) {
		async {
			uploadFile(
				file = file,
				getPresignedUrlAPI = getPresignedUrlAPI,
				progressFlow = progressFlow
			)
		}
	}

	suspend fun uploadFileAsync(
		file: File,
		mediaType: MediaType?,
		getPresignedUrlAPI: String,
		dispatcher: CoroutineDispatcher = Dispatchers.IO,
		progressFlow: MutableStateFlow<Float>? = null, // Optional progress tracking
	): Deferred<UploadResult> = withContext(dispatcher) {
		async {
			uploadFile(
				file = file,
				mediaType = mediaType,
				getPresignedUrlAPI = getPresignedUrlAPI,
				progressFlow = progressFlow
			)
		}
	}

	suspend fun uploadFile(
		file: File,
		getPresignedUrlAPI: String,
		progressFlow: MutableStateFlow<Float>? = null, // Optional progress tracking
	): UploadResult = uploadFile(
			file = file,
			mediaType = getMimeType(file)?.toMediaTypeOrNull(),
			getPresignedUrlAPI = getPresignedUrlAPI,
			progressFlow = progressFlow
		)

	suspend fun uploadFile(
		file: File,
		mediaType: MediaType?,
		getPresignedUrlAPI: String,
		progressFlow: MutableStateFlow<Float>? = null, // Optional progress tracking
	): UploadResult {
		if(!isInitDone()) {
			throw IllegalStateException("S3Uploader is not initialized. Please call S3Uploader.initS3Uploader() before using it.")
		}
		LoggerWithLevel.v(this, "Getting Pre-Signed URL for file: ${file.name}, from API:\"$getPresignedUrlAPI\"")
		return try {
			val response: ApiResponse<GetPreSignedUrlResponse> = RetrofitClient.apiService.getPreSignedURL(
				authToken = tokenProvider.provideToken(),
				url = getPresignedUrlAPI,
				file.name
			)
			when(response) {
				is ApiResponse.Success -> {
					val body = response.data
					val preSignedUrlData = body.data
					if (preSignedUrlData != null) {
						LoggerWithLevel.d(this, "Request is successful with response: $body")
						makeUploadRequestSuspend(file, mediaType, preSignedUrlData, progressFlow)
					} else {
						LoggerWithLevel.e(this, "Error getting pre-signed URL for file upload, PreSignedURLData was Null!")
						UploadResult.Error("Error getting pre-signed URL for file upload, PreSignedURLData was Null!")
					}
				}

				is ApiResponse.Failure.Error -> {
					val message = firstNotNullOrBlank({ response.errorBody.parseBody<ErrorBody>()?.message }, { response.message() }, fallback = "Unknown error")
					LoggerWithLevel.e(this, "Error getting pre-signed URL for file upload, $message")
					UploadResult.Error("Error generating presignedUrl", IOException("Response code: ${response.statusCode.code}"))
				}

				is ApiResponse.Failure.Exception -> {
					when (response.throwable) {
						is UnknownHostException,
						is ConnectException,
						is SocketException,
						is SocketTimeoutException -> {
							LoggerWithLevel.w(this, "Error getting pre-signed URL for file upload", response.throwable)
						}
						else -> {
							val message = firstNotNullOrBlank({ response.throwable.message }, { response.message() }, fallback = "Unknown error")
							LoggerWithLevel.e(this, "Error getting pre-signed URL for file upload, $message", response.throwable)
						}
					}
					UploadResult.Error("Error generating presignedUrl", response.throwable)
				}
			}
		} catch (e: Exception) {
			LoggerWithLevel.e(this, "Error getting pre-signed URL for file upload", e)
			UploadResult.Error("Error generating presignedUrl", e)
		}
	}

	private suspend fun makeUploadRequestSuspend(
		file: File,
		mediaType: MediaType?,
		data: PreSignedURLData,
		progressFlow: MutableStateFlow<Float>?
	): UploadResult {
		LoggerWithLevel.v(this, "Start uploading file:\"${file.name}\", mediaType:\"$mediaType\"")
		return try {
			val requestBody = if (progressFlow != null) {
				ProgressRequestBody(file, mediaType, progressFlow)
			} else {
				file.asRequestBody(mediaType)
			}
			val response = RetrofitClient.apiService.uploadToS3(
				uploadUrl = data.presignedUrl,
				headers = data.headers.asMap,
				body = requestBody
			)
			when(response) {
				is ApiResponse.Success -> {
					progressFlow?.value = 100f // Ensure progress hits 100% on success, if provided
					LoggerWithLevel.d(this, "file Uploading is successful!")
					UploadResult.Success(data.filePath)
				}
				is ApiResponse.Failure.Error -> {
					LoggerWithLevel.e(this, "Uploading is failed with code: ${response.statusCode.code}, message: ${response.message()}")
					UploadResult.Error("Error uploading file", IOException("Response code: ${response.statusCode.code}"))
				}
				is ApiResponse.Failure.Exception -> {
					LoggerWithLevel.e(this, "Uploading is failed with exception: ", response.throwable)
					UploadResult.Error("Error uploading file", response.throwable)
				}
			}
		} catch (e: Exception) {
			LoggerWithLevel.e(this, "Error uploading file", e)
			UploadResult.Error("Error uploading file", e)
		}
	}
}