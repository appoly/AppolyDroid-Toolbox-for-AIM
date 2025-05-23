package uk.co.appoly.droid.s3upload.utils

import androidx.annotation.WorkerThread
import okhttp3.ResponseBody
import uk.co.appoly.droid.s3upload.network.RetrofitClient
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

internal fun <C : CharSequence> firstNotNullOrBlank(vararg options: () -> C?, fallback: C): C {
	for (option in options) {
		val value = try {
			option()
		} catch (e: Exception) {
			S3UploadLogger.w("firstNotNullOrBlank", "Exception thrown in option", e)
			null
		}
		if (!value.isNullOrBlank()) {
			return value
		}
	}
	return fallback
}

@WorkerThread
internal inline fun <reified T> ResponseBody?.parseBody(): T? = this?.let {
	val source = source()
	source.request(Long.MAX_VALUE) // Buffer the entire body.
	val buffer = source.buffer
	val contentType = contentType()
	val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
	val stringBody = buffer.clone().readString(charset)
	RetrofitClient.json.decodeFromString<T>(stringBody)
}