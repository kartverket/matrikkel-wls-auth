pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }

    plugins {
        val kotlinVersion = "1.4.21"
        id ("org.jetbrains.kotlin.jvm") version kotlinVersion
        id ("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id ("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
        id ("org.jetbrains.kotlin.kapt") version kotlinVersion
        id ("com.github.johnrengelman.shadow") version "5.2.0"
    }
}

rootProject.name = "mat-auth"

include(
    ":mat-auth-annotation",
    ":mat-auth-api",
    ":mat-auth-common",
    ":mat-auth-common-weblogic",
    ":ham",
    ":initializer-weblogic",
    ":id-store-basic",
    ":id-store-jwt",
    ":mat-auth-platform",
    ":example:shared",
    ":example:ear",
    ":example:war",
    ":example:war2"
)
