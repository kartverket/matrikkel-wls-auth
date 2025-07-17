pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }

    plugins {
        val kotlinVersion = "2.2.0"
        id ("org.jetbrains.kotlin.jvm") version kotlinVersion
        id ("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id ("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
    }
}

rootProject.name = "mat-auth"

include(
    ":mat-auth-api",
    ":mat-auth-shared",
    ":mat-auth-ham",
    ":mat-auth-id-store-basic",
    ":mat-auth-id-store-jwt",
    ":mat-auth-platform",
    ":example:shared",
    ":example:ear",
    ":example:war",
    ":example:war2"
)
