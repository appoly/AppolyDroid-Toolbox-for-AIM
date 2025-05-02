package uk.co.appoly.droid.data.remote

import com.duck.flexilogger.FlexiLog
import kotlinx.serialization.json.Json
import uk.co.appoly.droid.BaseRepoLogger
import kotlin.reflect.KClass

class ServiceManager private constructor(
	val getRetrofitClient: () -> BaseRetrofitClient,
	val getLogger: () -> FlexiLog
) {
	val services = mutableMapOf<KClass<*>, BaseService<*>>()

	fun resetClients() {
		synchronized(services) {
			services.values.forEach {
				it.resetClient()
			}
		}
	}

	@Suppress("UNCHECKED_CAST")
	inline fun <reified T : BaseService.API> getService(): BaseService<T> {
		return (services[T::class] ?: synchronized(services) {
			services[T::class] ?: makeBaseService<T>()
				.also {
					services[T::class] = it
				}
		}) as BaseService<T>
	}

	inline fun <reified C : BaseService.API> makeBaseService(): BaseService<C> =
		object : BaseService<C>(C::class) {
			override val retrofitClient: BaseRetrofitClient
				get() = getRetrofitClient()
		}

	companion object {
		@Volatile
		private var instance: ServiceManager? = null

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

		fun resetClients() {
			instance?.resetClients()
		}

		fun getLogger(): FlexiLog {
			return instance?.getLogger() ?: BaseRepoLogger
		}
	}
}

interface BaseRetrofitClient {
	val json: Json
	fun <T> createService(serviceClass: Class<T>): T
}

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

	val api: API
		get() = getClient()

	protected abstract val retrofitClient: BaseRetrofitClient

	private fun getClient(): API {
		return client ?: synchronized(this) {
			client ?: makeClient().also {
				client = it
			}
		}
	}

	protected open fun makeClient(): API =
		retrofitClient.createService(kClass.java)

	fun resetClient() {
		client = null
	}
}