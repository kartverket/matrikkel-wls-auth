package no.statkart.matrikkel.auth.common

import no.statkart.matrikkel.auth.credential.AuthConfigKeys
import no.statkart.matrikkel.auth.util.jaxrs.logger
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import java.net.URI
import java.util.*
import javax.enterprise.event.Observes
import javax.enterprise.inject.spi.*

class OIDCDiscoveryExtension : Extension {
    private val visitedClassLoaders : MutableSet<ClassLoader> = Collections.newSetFromMap(IdentityHashMap())

    fun processBean(@Observes pb: ProcessBean<*>) {
        visitedClassLoaders.add(pb.bean.beanClass.classLoader)
    }

    fun afterBeanDiscovery(@Observes abd: AfterBeanDiscovery) {
        val configResolver = ConfigProviderResolver.instance()
        val allClassLoaders = visitedClassLoaders.flatMap { cl ->
            generateSequence(cl) { it.parent }.mapNotNull {
                val ccl = Thread.currentThread().contextClassLoader
                try {
                    Thread.currentThread().contextClassLoader = cl
                    val uri = configResolver
                        .getConfig(cl)
                        .getOptionalValue(AuthConfigKeys.DISCOVERY_URL, URI::class.java)
                        .orElse(null)
                    uri?.let { cl to uri }
                } finally {
                    Thread.currentThread().contextClassLoader = ccl
                }
            }
        }.distinct().toMap()

        allClassLoaders.filter { (cl, uri) -> val parent = allClassLoaders.getOrDefault(cl.parent, null); parent == null || uri != parent }.forEach { (cl, uri) ->
            val config = configResolver.getConfig(cl)
            val newConfig = configResolver
                .builder
                .forClassLoader(cl)
                .addDiscoveredConverters()
                .withSources(*config.configSources.toList().toTypedArray())
                .withSources(OIDCDiscovery(uri))
                .build()
            logger.info("Registering OpenID Connect metadata config source {} for {}", uri, cl)
            configResolver.registerConfig(newConfig, cl)
        }
    }

    fun afterDeploymentValidation(@Observes adv: AfterDeploymentValidation) {
        visitedClassLoaders.clear()
    }
}