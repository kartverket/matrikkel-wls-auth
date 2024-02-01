package no.statkart.matrikkel.auth.weblogic.soteria

import java.security.AccessController
import java.security.PrivilegedAction
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.spi.CreationalContext
import javax.enterprise.inject.spi.Bean
import javax.enterprise.inject.spi.BeanManager
import javax.inject.Inject
import javax.security.enterprise.credential.Credential
import javax.security.enterprise.identitystore.CredentialValidationResult
import javax.security.enterprise.identitystore.IdentityStore
import javax.security.enterprise.identitystore.IdentityStoreHandler

abstract class AbstractSoteriaIdentityStoreHandler protected constructor() : IdentityStoreHandler {

    private lateinit var authenticationIdentityStores: List<IdentityStore>
    private lateinit var authorizationIdentityStores: List<IdentityStore>
    @field:Inject
    private lateinit var bm: BeanManager
    private val identityStoreInstances = IdentityHashMap<IdentityStore, Pair<Bean<IdentityStore>, CreationalContext<IdentityStore>>>()

    override fun validate(credential: Credential?): CredentialValidationResult {
        val (identityStore, validation) = authenticationIdentityStores
            .fold(null as IdentityStore? to CredentialValidationResult.NOT_VALIDATED_RESULT) { acc, identityStore ->
                val (_, validation) = acc
                when (validation.status) {
                    CredentialValidationResult.Status.NOT_VALIDATED, null -> identityStore.validate(credential)?.let { identityStore to it } ?: acc
                    CredentialValidationResult.Status.INVALID -> acc
                    CredentialValidationResult.Status.VALID -> identityStore.validate(credential)
                        ?.takeIf { it.status == CredentialValidationResult.Status.INVALID }
                        ?.let { identityStore to it }
                        ?: acc
                }
            }

        if (validation.status != CredentialValidationResult.Status.VALID) {
            return validation
        }

        val groups : Set<String> = AccessController.doPrivileged(PrivilegedAction<Set<String>> {
            authorizationIdentityStores
                .mapNotNull { it.getCallerGroups(validation) }.flatten().toSet()
                .let { groups ->
                    if (identityStore!!.validationTypes().contains(IdentityStore.ValidationType.PROVIDE_GROUPS)) {
                        identityStore.getCallerGroups(validation)?.let { groups.union(it) }
                    } else {
                        groups
                    }
                }
        })

        return CredentialValidationResult(
            validation.identityStoreId,
            validation.callerPrincipal,
            validation.callerDn,
            validation.callerUniqueId,
            groups
        )
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
}