package uk.co.appoly.s3imageupload.network

import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import java.io.File

class ProgressRequestBody(
	private val file: File,
	private val mediaType: MediaType?,
	private val progressFlow: MutableStateFlow<Float>
) : RequestBody() {

	override fun contentType(): MediaType? = mediaType

	override fun contentLength(): Long = file.length()

	override fun writeTo(sink: BufferedSink) {
		val source = file.source()
		val buffer = Buffer()
		val totalBytes = contentLength()
		var uploadedBytes = 0L

		source.use { input ->
			var bytesRead: Long
			while (input.read(buffer, 8 * 1024).also { bytesRead = it } != -1L) {
				sink.write(buffer, bytesRead)
				uploadedBytes += bytesRead
				val progress = (uploadedBytes * 100f / totalBytes).coerceIn(0f, 100f)
				progressFlow.value = progress
			}
		}
	}
}