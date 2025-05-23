package uk.co.appoly.s3imageupload.interfaces

fun interface AuthTokenProvider {
	fun provideToken(): String?
}