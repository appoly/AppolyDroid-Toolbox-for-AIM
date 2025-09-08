package uk.co.appoly.droid.data.remote.model.response

interface RootJson {
	val status: ResponseStatus
	val messages: List<String>?
	val errors: List<String>?

	val success: Boolean
		get() = status == ResponseStatus.Success
}