package uk.co.appoly.droid.s3upload.interfaces

/**
 * Functional interface for providing authentication tokens.
 *
 * This interface abstracts the authentication token retrieval mechanism,
 * allowing the S3Uploader to access API endpoints that require authentication
 * without being tied to any specific authentication implementation.
 *
 * Implementation example:
 * ```kotlin
 * val tokenProvider = AuthTokenProvider {
 *     // Get token from your auth system, e.g.:
 *     authRepository.getCurrentToken()
 * }
 * ```
 */
fun interface AuthTokenProvider {
	/**
	 * Provides an authentication token for API requests.
	 *
	 * @return The authentication token as a string, or null if no token is available
	 */
	fun provideToken(): String?
}
