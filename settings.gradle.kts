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
    ":annotation",
    ":api",
    ":common",
    ":common-weblogic",
    ":ham",
    ":initializer-weblogic",
    ":id-store-basic",
    ":id-store-jwt",
    ":platform",
    ":example:shared",
    ":example:ear",
    ":example:war",
    ":example:war2"
)

fun renameProject(prefix: String, pd: ProjectDescriptor) {
    pd.buildFileName = "$prefix${pd.name}.gradle.kts"
    if (!File(pd.projectDir, pd.buildFileName).isFile) {
        val buildFileName = "$prefix${pd.name}.gradle"
        if (File(pd.projectDir, buildFileName).isFile) {
            pd.buildFileName = buildFileName
        }
    }
    if (pd.children.isEmpty()) {
        if (!pd.buildFile.isFile) {
            logger.log(LogLevel.WARN, "Build file missing: {}", pd.buildFile.relativeTo(rootProject.projectDir))
        } else if (logger.isInfoEnabled) {
            logger.log(
                LogLevel.INFO,
                "Build file detected: {} ({})",
                pd.buildFile.relativeTo(rootProject.projectDir),
                pd.path
            )
        }
    } else {
        if (!pd.projectDir.isDirectory) {
            throw GradleException("Project directory missing: ${pd.buildFile}")
        }
        pd.children.forEach {renameProject(prefix + pd.projectDir.name + "-", it) }
    }
}
rootProject.children.forEach {renameProject("", it) }
