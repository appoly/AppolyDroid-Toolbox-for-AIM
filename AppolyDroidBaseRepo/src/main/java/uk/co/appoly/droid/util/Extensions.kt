package uk.co.appoly.droid.util

import androidx.annotation.WorkerThread
import okhttp3.ResponseBody
import uk.co.appoly.droid.data.remote.BaseRetrofitClient
import uk.co.appoly.droid.data.remote.ServiceManager
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

inline fun <C : CharSequence> C?.ifNullOrBlank(defaultValue: () -> C): C =
	if (isNullOrBlank()) defaultValue() else this

inline fun <C : CharSequence> C?.ifNullOrBlank2(defaultValue: () -> C?): C? =
	if (isNullOrBlank()) defaultValue() else this

fun <C : CharSequence> firstNotNullOrBlank(vararg options: () -> C?, fallback: () -> C): C {
	for (option in options) {
		val value = try {
			option()
		} catch (e: Exception) {
			ServiceManager.getLogger().w("firstNotNullOrBlank", "Exception thrown in option", e)
			null
		}
		if (!value.isNullOrBlank()) {
			return value
		}
	}
	return fallback()
}

@WorkerThread
inline fun <reified T> ResponseBody?.parseBody(retrofitClient: BaseRetrofitClient): T? = this?.let {
	val source = source()
	source.request(Long.MAX_VALUE) // Buffer the entire body.
	val buffer = source.buffer
	val contentType = contentType()
	val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
	val stringBody = buffer.clone().readString(charset)
	retrofitClient.json.decodeFromString<T>(stringBody)
}