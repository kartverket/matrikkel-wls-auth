package no.statkart.matrikkel.auth.shared

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.spi.CreationalContext
import jakarta.enterprise.inject.spi.Bean
import jakarta.enterprise.inject.spi.BeanManager
import jakarta.inject.Inject
import jakarta.security.enterprise.credential.Credential
import jakarta.security.enterprise.identitystore.CredentialValidationResult
import jakarta.security.enterprise.identitystore.IdentityStore
import jakarta.security.enterprise.identitystore.IdentityStoreHandler
import java.util.IdentityHashMap

abstract class AbstractIdentityStoreHandler protected constructor() : IdentityStoreHandler {
    private lateinit var authenticationIdentityStores: List<IdentityStore>
    private lateinit var authorizationIdentityStores: List<IdentityStore>

    @field:Inject
    private lateinit var bm: BeanManager
    private val identityStoreInstances =
        IdentityHashMap<IdentityStore, Pair<Bean<IdentityStore>, CreationalContext<IdentityStore>>>()

    override fun validate(credential: Credential?): CredentialValidationResult {
        val validation = authenticationIdentityStores.validate(credential)
        return when (validation) {
            is IdentityStoreValidationResult.Invalid -> validation.status
            is IdentityStoreValidationResult.Valid -> {
                val groups: Set<String> = authorizationIdentityStores.extractProvidedGroups(validation)
                CredentialValidationResult(
                    validation.status.identityStoreId,
                    validation.status.callerPrincipal,
                    validation.status.callerDn,
                    validation.status.callerUniqueId,
                    groups
                )
            }
        }
    }

    @PostConstruct
    open fun initializeInternal() {
        // Bruker BeanManager direkte, siden Instance<IdentityStore> ikke plukket opp alle, bug i Weld?
        @Suppress("UNCHECKED_CAST")
        val identityStoreBeans = bm.getBeans(IdentityStore::class.java).map { it as Bean<IdentityStore> }.toList()
        val authenticators: MutableList<IdentityStore> = ArrayList()
        val authorizers: MutableList<IdentityStore> = ArrayList()
        for (bean in identityStoreBeans) {
            val ctx = bm.createCreationalContext(bean)
            val identityStore = bm.getReference(bean, IdentityStore::class.java, ctx) as IdentityStore
            val validationTypes = identityStore.validationTypes()
            if (validationTypes != null && validationTypes.contains(IdentityStore.ValidationType.VALIDATE)) {
                authenticators.add(identityStore)
                identityStoreInstances[identityStore] = bean to ctx
            } else if (validationTypes != null && validationTypes.contains(IdentityStore.ValidationType.PROVIDE_GROUPS)) {
                authorizers.add(identityStore)
                identityStoreInstances[identityStore] = bean to ctx
            } else {
                bean.destroy(identityStore, ctx)
            }
        }
        authenticators.sortWith(Comparator.comparing { obj: IdentityStore -> obj.priority() })
        authenticationIdentityStores = authenticators
        check(authenticationIdentityStores.isNotEmpty()) {
            "No authenticating identity stores found"
        }
        authorizers.sortWith(Comparator.comparing { obj: IdentityStore -> obj.priority() })
        authorizationIdentityStores = authorizers
    }

    @PreDestroy
    open fun cleanUpInternal() {
        val instances = identityStoreInstances.iterator()
        while (instances.hasNext()) {
            val (identityStore, reference) = instances.next()
            val (bean, ctx) = reference
            bean.destroy(identityStore, ctx)
            instances.remove()
        }
    }

    companion object {
        fun List<IdentityStore>.validate(credential: Credential?): IdentityStoreValidationResult {
            var currentResult: IdentityStoreValidationResult = IdentityStoreValidationResult.NOT_VALIDATED
            for (identityStore in this) {
                val newResult = identityStore.validate(credential) ?: continue

                if (newResult.status == CredentialValidationResult.Status.INVALID) {
                    return IdentityStoreValidationResult.INVALID
                }

                if (currentResult is IdentityStoreValidationResult.Valid) {
                    continue
                }

                currentResult = IdentityStoreValidationResult.of(identityStore, newResult)
            }

            return currentResult
        }

        fun List<IdentityStore>.extractProvidedGroups(validation: IdentityStoreValidationResult.Valid): Set<String> {
            val groups = this.mapNotNull { it.getCallerGroups(validation.status) }
                .flatten()
                .toSet()

            val provideExtraGroups = validation.identityStore
                .validationTypes()
                ?.contains(IdentityStore.ValidationType.PROVIDE_GROUPS)
                ?: false

            if (provideExtraGroups) {
                val extraGroups = validation.identityStore.getCallerGroups(validation.status)
                return groups.union(extraGroups)
            }

            return groups
        }
    }
}
