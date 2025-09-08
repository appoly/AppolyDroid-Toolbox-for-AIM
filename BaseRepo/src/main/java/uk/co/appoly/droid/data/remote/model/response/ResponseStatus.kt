package uk.co.appoly.droid.data.remote.model.response

import kotlinx.serialization.Serializable
import uk.co.appoly.droid.util.EnumAsStringSerializer

@Serializable(with = ResponseStatus.Serializer::class)
enum class ResponseStatus(val status: String) {
	Success("success"),
	Error("error"),
	;

	companion object {
		fun fromString(value: String): ResponseStatus {
			return entries.firstOrNull { it.status.equals(value, ignoreCase = true) }
				?: throw IllegalArgumentException("Unknown ResponseStatus: $value")
		}
	}

	object Serializer : EnumAsStringSerializer<ResponseStatus>(
		"ResponseStatus",
		ResponseStatus::status,
		ResponseStatus::fromString
	)
}