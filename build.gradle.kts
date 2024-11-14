import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.findKaptConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.noarg.gradle.NoArgExtension
import org.jetbrains.kotlin.util.prefixIfNot

plugins {
    id ("org.jetbrains.kotlin.jvm") apply(false)
    id ("org.jetbrains.kotlin.plugin.allopen") apply(false)
    id ("org.jetbrains.kotlin.plugin.noarg") apply(false)
}

allprojects {
    group = "no.statkart.matrikkel.auth"
    if (version == "unspecified") {
        val versionQualifier =
            (properties.getOrDefault("version.qualifier", "SNAPSHOT") as String?)
                ?.takeIf { it.isNotBlank() }
                ?.prefixIfNot("-")
                ?: ""
        version = "2.0.0$versionQualifier"
    }
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    plugins.withId("org.jetbrains.kotlin.jvm")  {
        plugins.apply("kotlin-noarg")
        plugins.apply("kotlin-allopen")

        configure<AllOpenExtension> {
            annotation("jakarta.enterprise.context.NormalScope")
            annotation("jakarta.enterprise.inject.Vetoed")
        }

        configure<NoArgExtension> {
            annotation("jakarta.enterprise.context.NormalScope")
            annotation("jakarta.enterprise.inject.Vetoed")
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    //
    // Legg til en compileOnlyApi configuration
    plugins.withType<JavaLibraryPlugin> {
        configure<JavaPluginExtension> {
            sourceSets.matching { it.name != TEST_SOURCE_SET_NAME }.all {
                val compileOnly = configurations[compileOnlyConfigurationName]
                val compileOnlyApi = configurations.maybeCreate(compileOnly.name + "Api").apply {
                    isCanBeConsumed = false
                    isCanBeResolved = false
                    compileOnly.extendsFrom(this)
                }
                configurations.maybeCreate(apiElementsConfigurationName).apply {
                    extendsFrom(compileOnlyApi)
                }
            }
        }
    }

    plugins.withType<JavaBasePlugin> {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
            afterEvaluate {
                sourceSets.filter { it.name != TEST_SOURCE_SET_NAME }.forEach { sourceSet ->

                    //
                    // Legg til en toppkonfigurasjon til hvert sourceSet, denne brukes for å ha version constraints
                    // på dependencies
                    val bottomConfigNames = listOf(
                        sourceSet.compileClasspathConfigurationName,
                        sourceSet.runtimeClasspathConfigurationName,
                        sourceSet.annotationProcessorConfigurationName
                    )
                    val bottomConfigs = bottomConfigNames.mapNotNull { configurations.findByName(it) }.toMutableList()
                    findKaptConfiguration(sourceSet.name)?.let { bottomConfigs.add(it) }

                    val topConfigs = bottomConfigs.flatMap { it.all }.distinct().filter { it.extendsFrom.isEmpty() }

                    val newTopConfig = configurations.create("${sourceSet.name}Top") {
                        isCanBeConsumed = false
                        isCanBeResolved = false
                    }

                    for (topConfig in topConfigs) {
                        topConfig.extendsFrom(newTopConfig)
                    }

                    dependencies {
                        add(newTopConfig.name, platform(project(":mat-auth-platform")))
                    }
                }
            }
        }
    }

}
