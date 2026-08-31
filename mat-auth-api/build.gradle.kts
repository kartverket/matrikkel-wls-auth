plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi("jakarta.servlet:jakarta.servlet-api")
    compileOnlyApi("jakarta.annotation:jakarta.annotation-api")
    compileOnlyApi("jakarta.enterprise:jakarta.enterprise.cdi-api")
    compileOnlyApi("jakarta.interceptor:jakarta.interceptor-api")
    compileOnlyApi("jakarta.authentication:jakarta.authentication-api")
    compileOnlyApi("jakarta.security.enterprise:jakarta.security.enterprise-api")
    compileOnlyApi("org.eclipse.microprofile.config:microprofile-config-api")
    compileOnlyApi("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api")
    compileOnlyApi("org.slf4j:slf4j-api")
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
