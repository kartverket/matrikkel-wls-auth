plugins {
    id("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi("jakarta.security.enterprise:jakarta.security.enterprise-api")
    compileOnlyApi("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api")

    compileOnly("jakarta.security.enterprise:jakarta.security.enterprise-api")
    compileOnlyApi("jakarta.json:jakarta.json-api")

    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")

    api(project(":api"))
    api ("org.bitbucket.b_c:jose4j")

    api ("io.arrow-kt:arrow-fx-coroutines")
    api ("io.arrow-kt:arrow-syntax")
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