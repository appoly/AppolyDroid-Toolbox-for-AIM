package uk.co.appoly.s3imageupload.network.api

import com.skydoves.sandwich.ApiResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url
import uk.co.appoly.s3imageupload.network.GetPreSignedUrlBody
import uk.co.appoly.s3imageupload.network.GetPreSignedUrlResponse

internal interface APIs {
	@POST
	suspend fun getPreSignedURL(
		@Header("Accepts") accepts: String,
		@Url url: String,
		@Body body: GetPreSignedUrlBody
	): ApiResponse<GetPreSignedUrlResponse>
	@POST
	suspend fun getPreSignedURL(
		@Header("Authorization") authorization: String,
		@Header("Accepts") accepts: String,
		@Url url: String,
		@Body body: GetPreSignedUrlBody
	): ApiResponse<GetPreSignedUrlResponse>

	@PUT
	suspend fun uploadToS3(
		@Url url: String,
		@HeaderMap headersMap: Map<String, String>,
		@Body file: RequestBody
	): ApiResponse<Unit>
}