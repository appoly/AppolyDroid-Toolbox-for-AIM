package uk.co.appoly.droid.data.remote

import com.duck.flexilogger.FlexiLog
import kotlinx.serialization.json.Json
import uk.co.appoly.droid.BaseRepoLogger
import kotlin.reflect.KClass

/**
 * Manages API service instances using the Singleton pattern.
 *
 * This class provides a centralized way to create and access API service instances.
 * It caches created services and provides methods to reset clients when needed, for
 * example, when the Auth token changes.
 *
 * @property getRetrofitClient Lambda that provides a [BaseRetrofitClient] instance
 * @property getLogger Lambda that provides a [FlexiLog] instance for logging
 */
class ServiceManager private constructor(
	val getRetrofitClient: () -> BaseRetrofitClient,
	val getLogger: () -> FlexiLog
) {
	val services = mutableMapOf<KClass<*>, BaseService<*>>()

	/**
	 * Resets all managed service clients.
	 *
	 * This forces all services to recreate their API clients on next access.
	 */
	fun resetClients() {
		synchronized(services) {
			services.values.forEach {
				it.resetClient()
			}
		}
	}

	/**
	 * Gets or creates a service of the specified API type.
	 *
	 * @param T The API interface type to get a service for
	 * @return A [BaseService] instance for the requested API type
	 */
	@Suppress("UNCHECKED_CAST")
	inline fun <reified T : BaseService.API> getService(): BaseService<T> {
		return (services[T::class] ?: synchronized(services) {
			services[T::class] ?: makeBaseService<T>()
				.also {
					services[T::class] = it
				}
		}) as BaseService<T>
	}

	/**
	 * Creates a new [BaseService] instance for the specified API type.
	 *
	 * @param C The API interface type to create a service for
	 * @return A new [BaseService] instance for the requested API type
	 */
	inline fun <reified C : BaseService.API> makeBaseService(): BaseService<C> =
		object : BaseService<C>(C::class) {
			override val retrofitClient: BaseRetrofitClient
				get() = getRetrofitClient()
		}

	companion object {
		@Volatile
		private var instance: ServiceManager? = null

		/**
		 * Gets or creates the singleton [ServiceManager] instance.
		 *
		 * @param getRetrofitClient Lambda that provides a [BaseRetrofitClient] instance
		 * @param getLogger Lambda that provides a [FlexiLog] instance for logging
		 * @return The singleton [ServiceManager] instance
		 */
		fun getInstance(
			getRetrofitClient: () -> BaseRetrofitClient,
			getLogger: () -> FlexiLog
		): ServiceManager {
			return instance ?: synchronized(this) {
				instance ?: ServiceManager(
					getRetrofitClient = getRetrofitClient,
					getLogger = getLogger
				).also {
					instance = it
				}
			}
		}

		/**
		 * Resets all service clients in the singleton instance.
		 */
		fun resetClients() {
			instance?.resetClients()
		}

		/**
		 * Gets the logger from the singleton instance or returns the default [BaseRepoLogger].
		 *
		 * @return The configured [FlexiLog] instance or [BaseRepoLogger] if no instance exists
		 */
		fun getLogger(): FlexiLog {
			return instance?.getLogger() ?: BaseRepoLogger
		}
	}
}

/**
 * Interface for Retrofit client implementations.
 *
 * Provides methods to create service instances and access the [Kotlinx Serialization Json][Json] configuration.
 */
interface BaseRetrofitClient {
	/**
	 * The [Kotlinx Serialization Json][Json] configuration used for serialization/deserialization.
	 */
	val json: Json

	/**
	 * Creates a service instance for the specified service class.
	 *
	 * @param serviceClass The class of the service interface to create
	 * @return A new instance of the requested service interface
	 */
	fun <T> createService(serviceClass: Class<T>): T
}

/**
 * Abstract base class for API service wrappers.
 *
 * Provides caching of API client instances and methods to reset them when needed.
 *
 * @param API The API interface type this service wraps
 * @param kClass The Kotlin class of the API interface
 */
abstract class BaseService<API : BaseService.API>(
	private val kClass: KClass<API>
) {
	/**
	 * Empty interface to be implemented by all API interfaces
	 * The purpose is to act as a base type constraint for the [BaseService] class
	 */
	interface API

	@Volatile
	private var client: API? = null

	/**
	 * The API client instance. Creates a new instance if none exists.
	 */
	val api: API
		get() = getClient()

	/**
	 * The Retrofit client to use for creating API instances.
	 */
	protected abstract val retrofitClient: BaseRetrofitClient

	/**
	 * Gets the existing client instance or creates a new one if needed.
	 *
	 * @return The API client instance
	 */
	private fun getClient(): API {
		return client ?: synchronized(this) {
			client ?: makeClient().also {
				client = it
			}
		}
	}

	/**
	 * Creates a new API client instance.
	 *
	 * @return A new instance of the API interface
	 */
	protected open fun makeClient(): API =
		retrofitClient.createService(kClass.java)

	/**
	 * Resets the cached client instance.
	 *
	 * Forces a new client to be created on the next access to [api].
	 */
	fun resetClient() {
		client = null
	}
}
