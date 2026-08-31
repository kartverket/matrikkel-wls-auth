plugins {
    id("java-platform")
    id("maven-publish")
}
javaPlatform {
    allowDependencies()
}
dependencies {
    api(platform("jakarta.platform:jakarta.jakartaee-bom:10.0.0"))

    constraints {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
        api("org.bitbucket.b_c:jose4j:0.9.6") //Json Web Token (JWT)
        api("io.arrow-kt:arrow-core:2.2.3")
        api("io.arrow-kt:arrow-resilience:2.2.3")
        api("org.slf4j:slf4j-api:2.0.18")
        api("org.eclipse.microprofile.config:microprofile-config-api:3.1.1")
        api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api:2.2")

        api("org.junit.jupiter:junit-jupiter:6.1.3")
        api("org.eclipse.parsson:parsson:1.1.9")
    }
}
publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
