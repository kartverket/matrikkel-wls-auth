plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi("javax.servlet:javax.servlet-api")
    compileOnlyApi("javax.annotation:javax.annotation-api")
    compileOnlyApi("javax.enterprise:cdi-api")
    compileOnlyApi("javax.security.auth.message:javax.security.auth.message-api")

    compileOnlyApi("javax.security.enterprise:javax.security.enterprise-api")

    compileOnlyApi("org.eclipse.microprofile.config:microprofile-config-api")

    compileOnlyApi("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api")

    compileOnlyApi("org.apache.logging.log4j:log4j-api")

    api(project(":annotation"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("project") {
            from(components["java"])
        }
    }
}