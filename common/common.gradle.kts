plugins {
    id("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi("javax.security.enterprise:javax.security.enterprise-api")
    compileOnlyApi("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api")

    compileOnly("javax.ws.rs:javax.ws.rs-api")
    compileOnlyApi("javax.json:javax.json-api")

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