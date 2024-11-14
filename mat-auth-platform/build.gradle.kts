plugins {
    id("java-platform")
    id("maven-publish")
}
dependencies {
    constraints {
        api("org.bitbucket.b_c:jose4j:0.9.6") //Json Web Token (JWT)
        api("io.arrow-kt:arrow-core:0.11.0")
        api("io.arrow-kt:arrow-fx-coroutines:0.11.0")
        api("io.arrow-kt:arrow-optics:0.11.0")
        api("io.arrow-kt:arrow-meta:0.11.0")
        api("org.slf4j:slf4j-api:2.0.9")
        api("org.eclipse.microprofile.config:microprofile-config-api:3.1")
        api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api:2.1")
        api("jakarta.security.enterprise:jakarta.security.enterprise-api:2.0.0")
        api("jakarta.ws.rs:jakarta.ws.rs-api:3.0.0")
        api("jakarta.enterprise:jakarta.enterprise.cdi-api:3.0.1")
        api("jakarta.servlet:jakarta.servlet-api:5.0.0")
        api("jakarta.annotation:jakarta.annotation-api:2.0.0")
        api("jakarta.ejb:jakarta.ejb-api:4.0.1")
        api("org.jboss.weld:weld-api")
        api("com.google.guava:guava:32.1.2-jre")
        api("jakarta.security.jacc:jakarta.security.jacc-api:1.6.1")
        api("jakarta.security.auth.message:jakarta.security.auth.message-api:2.0.0-RC1")
        api("jakarta.json:jakarta.json-api:2.0.2")
    }
}
publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
