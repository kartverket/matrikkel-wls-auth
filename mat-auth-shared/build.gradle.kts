plugins {
    id("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi("jakarta.security.enterprise:jakarta.security.enterprise-api")
    compileOnly("jakarta.security.enterprise:jakarta.security.enterprise-api")
    compileOnlyApi("jakarta.json:jakarta.json-api")

    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")

    api(project(":mat-auth-api"))
    api ("org.bitbucket.b_c:jose4j")

    api ("io.arrow-kt:arrow-fx-coroutines")

    api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api")
    api("org.eclipse.microprofile.config:microprofile-config-api")

    compileOnly("org.glassfish.soteria:jakarta.security.enterprise:1.0.1")

    runtimeOnly("org.jboss.logging:jboss-logging:3.4.3.Final")
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
