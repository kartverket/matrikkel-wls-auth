@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.gradle.kotlin.dsl.support.serviceOf
import kotlin.streams.toList
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer as ShadowTransformer

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") apply(false)
}

val ConfigurationContainer.weblogicEmbedImplementation by configurations.register("weblogicEmbedImplementation") {
    isCanBeConsumed = false
    isCanBeResolved = false
}

val TaskContainer.shadowJar by tasks.creating(ShadowJar::class) {
    from(sourceSets.main.get().output)
    configurations = mutableListOf(project.configurations.create("weblogicEmbedElements") {
        isCanBeConsumed = false
        extendsFrom(project.configurations.weblogicEmbedImplementation)
        setTransitive(false)
    })
    archiveBaseName.set("mat-auth-${project.name}")
    archiveClassifier.set("")
    // shadowJar tasken lager en jar med embedded dependencies fra
    // weblogicEmbed konfigurasjonen. Det er noen ServiceLoader tjenester
    // vi ikke vil ha med, siden de ikke helt virker med WLS, så
    // vi filtrerer ut disse her.
    transformers.add(FilteringServiceFileTransformer(listOf(
        "io.smallrye.config.inject.ConfigExtension")))
}

configurations {
    apiElements { isCanBeConsumed = false }
    runtimeElements { isCanBeConsumed = false }
    create("weblogicApiElements") {
        isCanBeResolved = false
        extendsFrom(api.get(), compileOnlyApi.get())
        configureAs(apiElements.get())
    }
    create("weblogicRuntimeElements") {
        isCanBeResolved = false
        extendsFrom(implementation.get(), runtimeOnly.get())
        configureAs(runtimeElements.get())
    }
    "compileOnlyApi" {
        extendsFrom(configurations.weblogicEmbedImplementation)
    }
}

gradle.serviceOf<SoftwareComponentFactory>().adhoc("weblogicComponent").apply {
    addVariantsFromConfiguration(configurations["weblogicRuntimeElements"]) {
        val attrs = configurationVariant.attributes
        val isJar = attrs.getAttribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE)?.name == LibraryElements.JAR
        val isExternal = attrs.getAttribute(Bundling.BUNDLING_ATTRIBUTE)?.name == Bundling.EXTERNAL
        if (isJar && isExternal) {
            when (attrs.getAttribute(Usage.USAGE_ATTRIBUTE)?.name) {
                Usage.JAVA_RUNTIME -> mapToMavenScope("runtime")
                else -> skip()
            }
        } else {
            skip()
        }
    }
    components.add(this)
}

dependencies {
    compileOnlyApi("jakarta.security.enterprise:jakarta.security.enterprise-api")
    api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api")
    api("org.eclipse.microprofile.config:microprofile-config-api")

    api(project(":mat-auth-api"))
    api(project(":mat-auth-common"))

    compileOnly("org.glassfish.soteria:jakarta.security.enterprise:2.0.0")

    val weblogicEmbed = configurations.weblogicEmbedImplementation

    weblogicEmbed("io.smallrye.config:smallrye-config:3.10.0")
    runtimeOnly("org.jboss.logging:jboss-logging:3.4.3.Final")
}

tasks {
    named<Jar>("jar") {
        enabled = false
        archiveClassifier.set("broken-dont-use")
        doFirst {
            throw GradleException("Jar task is disabled")
        }
    }
    named<Task>("assemble") {
        dependsOn(shadowJar)
    }
}

artifacts {
    add("weblogicRuntimeElements", tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("project") {
            from(components["weblogicComponent"])
        }
    }
}

/**
 * ShadowJar transformer som kan flytte og filtrere bort ServiceLoader tjenester.
 *
 * @see java.util.ServiceLoader
 * @see ServiceFileTransformer
 */
class FilteringServiceFileTransformer(
    private val ignored: List<String>,
    private val transformer: ServiceFileTransformer = ServiceFileTransformer()
) : ShadowTransformer by transformer, PatternFilterable by transformer {

    @Input
    override fun getIncludes(): MutableSet<String> = transformer.includes

    @Input
    override fun getExcludes(): MutableSet<String> = transformer.excludes

    override fun transform(context: TransformerContext) {
        val lines = context.`is`.bufferedReader()
            .lines().toList()
            .filter { it.isNotBlank() && !ignored.contains(it) }

        if (lines.isEmpty()) {
            return
        }

        transformer.transform(TransformerContext(
            context.path,
            lines.joinToString("\n", postfix = "\n").byteInputStream(),
            context.relocators,
            context.stats
        ))
    }
}

fun Configuration.configureAs(other: Configuration) {
    attributes {
        copyAttributes(this, other.attributes)
    }
    outgoing {
        //artifact(tasks.shadowJar)
        for (capability in other.outgoing.capabilities) {
            capability(capability)
        }
        variants {
            other.outgoing.variants.forEach { otherVariant ->
                create(otherVariant.name) {
                    copyAttributes(attributes, otherVariant.attributes)
                    otherVariant.artifacts.all { artifact(this) }
                }

            }
        }
    }
}

fun copyAttributes(target: AttributeContainer, source: AttributeContainer) {
    source.attributes.keySet().forEach { attribute -> copyAttribute(target, source, attribute) }
}

fun <T> copyAttribute(target: AttributeContainer, source: AttributeContainer, attribute: Attribute<T>) {
    target.attribute(attribute, source.getAttribute(attribute)!!)
}
