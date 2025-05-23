package uk.co.appoly.s3imageupload.network

import com.skydoves.sandwich.ApiResponse
import okhttp3.RequestBody
import uk.co.appoly.s3imageupload.S3Uploader
import uk.co.appoly.s3imageupload.network.api.APIs

internal class APIService {
	private var internalClient: APIs? = null

	private val client: APIs
		get() {
			if (internalClient == null) {
				synchronized(this) {
					if (internalClient == null) {
						internalClient = RetrofitClient.createService(APIs::class.java)
					}
				}
			}
			return internalClient!!
		}

	suspend fun getPreSignedURL(
		authToken: String?,
		url: String,
		fileName: String
	): ApiResponse<GetPreSignedUrlResponse> {
		return try {
			if(authToken.isNullOrBlank()) {
				client.getPreSignedURL(
					accepts = "application/json",
					url = url,
					body = GetPreSignedUrlBody(fileName)
				)
			} else {
				client.getPreSignedURL(
					authorization = "Bearer $authToken",
					accepts = "application/json",
					url = url,
					body = GetPreSignedUrlBody(fileName)
				)
			}
		} catch (e: Exception) {
			S3Uploader.Log.e(this, "Exception in getPreSignedURL! url=\"$url\"", e)
			ApiResponse.Failure.Exception(e)
		}
	}

	suspend fun uploadToS3(
		uploadUrl: String,
		headers: Map<String, String>,
		body: RequestBody
	): ApiResponse<Unit> {
		return try {
			client.uploadToS3(
				uploadUrl,
				headers,
				body
			)
		}
		catch (e: Exception) {
			S3Uploader.Log.e(this, "Exception in uploadToS3! url=\"$uploadUrl\"", e)
			ApiResponse.Failure.Exception(e)
		}
	}
}