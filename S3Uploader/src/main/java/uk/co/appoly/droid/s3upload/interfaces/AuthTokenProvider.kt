package uk.co.appoly.droid.s3upload.interfaces

fun interface AuthTokenProvider {
	fun provideToken(): String?
}