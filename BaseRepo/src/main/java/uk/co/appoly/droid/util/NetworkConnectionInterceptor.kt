package uk.co.appoly.droid.util

import com.duck.flexilogger.FlexiLog
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import uk.co.appoly.droid.BaseRepoLogger
import java.io.IOException

class NetworkConnectionInterceptor(
	private val isInternetAvailable: () -> Boolean,
	private val logger: FlexiLog = BaseRepoLogger
) : Interceptor {

	@Throws(IOException::class)
	override fun intercept(chain: Interceptor.Chain): Response {
		if (!isInternetAvailable()) {
			logger.v(this, "Interceptor check says: No internet connection!")
			throw NoConnectivityException()
		}
		logger.v(this, "Interceptor check says: Has internet connection")
		val builder: Request.Builder = chain.request().newBuilder()
		return chain.proceed(builder.build())
	}
}

/**
 * No connectivity exception
 *
 * Thrown by [NetworkConnectionInterceptor] when there is no internet connection
 */
class NoConnectivityException : IOException {
	constructor() : super()
	constructor(cause: Throwable) : super(cause)

	override val message: String
		get() = "No Internet Connection"
}

fun Throwable.asNoConnectivityException(): NoConnectivityException {
	return this as? NoConnectivityException ?: NoConnectivityException(this)
}