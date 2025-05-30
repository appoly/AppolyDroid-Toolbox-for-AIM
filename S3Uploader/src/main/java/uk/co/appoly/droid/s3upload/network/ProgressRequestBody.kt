package uk.co.appoly.droid.s3upload.network

import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import java.io.File

/**
 * A custom OkHttp RequestBody implementation that tracks upload progress.
 *
 * This class wraps a file and reports upload progress to a MutableStateFlow
 * as the file is being uploaded. Progress values range from 0.0 to 100.0.
 *
 * @property file The file to be uploaded
 * @property mediaType The MIME type of the file content
 * @property progressFlow A flow that receives progress updates during upload (0.0f-100.0f)
 */
class ProgressRequestBody(
	private val file: File,
	private val mediaType: MediaType?,
	private val progressFlow: MutableStateFlow<Float>
) : RequestBody() {

	/**
	 * Returns the content type (MIME type) of the request body.
	 *
	 * @return The MIME type of the file, or null if unknown
	 */
	override fun contentType(): MediaType? = mediaType

	/**
	 * Returns the length of the request body in bytes.
	 *
	 * @return The size of the file in bytes
	 */
	override fun contentLength(): Long = file.length()

	/**
	 * Writes the file content to the given sink, updating progress as bytes are written.
	 *
	 * This is where progress tracking happens. As chunks of the file are read and written
	 * to the network, the progressFlow is updated with the current percentage complete.
	 *
	 * @param sink The destination where file contents are written
	 */
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
