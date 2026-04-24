plugins {
    id("java-platform")
    id("maven-publish")
}
dependencies {
    constraints {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        api("org.bitbucket.b_c:jose4j:0.9.6") //Json Web Token (JWT)
        api("io.arrow-kt:arrow-core:2.2.2.1")
        api("io.arrow-kt:arrow-resilience:2.1.2")
        api("org.slf4j:slf4j-api:2.0.17")
        api("org.eclipse.microprofile.config:microprofile-config-api:3.1.1")
        api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api:2.1")
        api("jakarta.security.enterprise:jakarta.security.enterprise-api:2.0.0")
        api("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
        api("jakarta.enterprise:jakarta.enterprise.cdi-api:3.0.1")
        api("jakarta.servlet:jakarta.servlet-api:5.0.0")
        api("jakarta.annotation:jakarta.annotation-api:2.0.0")
        api("jakarta.ejb:jakarta.ejb-api:4.0.1")
        api("jakarta.security.jacc:jakarta.security.jacc-api:1.6.1")
        api("jakarta.security.auth.message:jakarta.security.auth.message-api:2.0.0-RC1")
        api("jakarta.json:jakarta.json-api:2.0.2")

        api("org.junit.jupiter:junit-jupiter:5.13.4")
        api("org.eclipse.parsson:parsson:1.1.7")
    }
}
publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
