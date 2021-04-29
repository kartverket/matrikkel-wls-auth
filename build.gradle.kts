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
        version = "0.1.0$versionQualifier"
    }
}

subprojects {
    repositories {
        mavenLocal()
        maven {
            setUrl("https://nexus.statkart.no/repository/public/")
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm")  {
        plugins.apply("kotlin-noarg")
        plugins.apply("kotlin-allopen")

        configure<AllOpenExtension> {
            annotation("javax.enterprise.context.NormalScope")
            annotation("javax.enterprise.inject.Vetoed")
        }

        configure<NoArgExtension> {
            annotation("javax.enterprise.context.NormalScope")
            annotation("javax.enterprise.inject.Vetoed")
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_1_8.toString()
            }
        }
    }

    //
    // Legg til en compileOnlyApi configuration
    plugins.withType<JavaLibraryPlugin> {
        configure<JavaPluginConvention> {
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
        configure<JavaPluginConvention> {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
            afterEvaluate {
                sourceSets.filter { it.name != TEST_SOURCE_SET_NAME }.forEach { sourceSet ->
                    //
                    // Rename jar filene slik at de begynner på "mat-auth-"
                    tasks.findByName(sourceSet.jarTaskName)?.let { it as? Jar }?.apply {
                        if (archiveBaseName.getOrElse(project.name) == project.name) {
                            archiveBaseName.set("mat-auth-" + project.name)
                        }
                    }

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
                        add(newTopConfig.name, platform(project(":platform")))
                    }
                }
            }
        }
    }

    afterEvaluate {
        //
        // Skift artifact id på maven publications slik at de begynner på "mat-auth-"
        extensions.findByType<PublishingExtension>()?.apply {
            publications.withType<MavenPublication>().all {
                if (groupId == project.group && artifactId == project.name && version == project.version) {
                    artifactId = "mat-auth-" + project.name
                }
            }
        }
    }
}