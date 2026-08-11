package us.webmy.core.internal.di

import androidx.annotation.VisibleForTesting
import us.webmy.core.internal.InternalWebmyApi
import kotlin.reflect.KClass

/**
 * SDK-wide service locator shared by all WebMY artifacts.
 *
 * All services are lazy singletons: the factory runs on first [resolve] and the
 * result is cached. Registration and resolution are synchronized on the registry
 * itself; the lock is reentrant, so factories may resolve other services.
 */
@InternalWebmyApi
object ServiceRegistry {

    private class Entry(val factory: () -> Any) {
        var instance: Any? = null
    }

    private val services = HashMap<KClass<*>, Entry>()

    fun <T : Any> register(type: KClass<T>, eager: Boolean = false, factory: () -> T) {
        synchronized(this) {
            check(type !in services) {
                "WebMY: ${type.simpleName} is already registered. " +
                    "Did you call init/installUi/initBilling/initAds twice?"
            }
            services[type] = Entry(factory)
        }
        if (eager) resolve(type)
    }

    fun <T : Any> resolve(type: KClass<T>, missingMessage: String? = null): T =
        synchronized(this) {
            val entry = services[type] ?: error(
                missingMessage
                    ?: ("WebMY: ${type.simpleName} is not available. " +
                        "Make sure WebMY.init(...) was called first.")
            )
            @Suppress("UNCHECKED_CAST")
            (entry.instance ?: entry.factory().also { entry.instance = it }) as T
        }

    fun <T : Any> resolveOrNull(type: KClass<T>): T? = synchronized(this) {
        val entry = services[type] ?: return null
        @Suppress("UNCHECKED_CAST")
        (entry.instance ?: entry.factory().also { entry.instance = it }) as T
    }

    fun isRegistered(type: KClass<*>): Boolean = synchronized(this) { type in services }

    @VisibleForTesting
    fun clear() = synchronized(this) { services.clear() }

    inline fun <reified T : Any> register(eager: Boolean = false, noinline factory: () -> T) =
        register(T::class, eager, factory)

    inline fun <reified T : Any> resolve(missingMessage: String? = null): T =
        resolve(T::class, missingMessage)

    inline fun <reified T : Any> resolveOrNull(): T? = resolveOrNull(T::class)
}
