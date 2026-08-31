plugins {
    id("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi("jakarta.security.enterprise:jakarta.security.enterprise-api")
    compileOnlyApi("jakarta.json:jakarta.json-api")
    compileOnly("jakarta.inject:jakarta.inject-api")
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    compileOnly("org.glassfish.soteria:jakarta.security.enterprise:3.0.4")

    api(project(":mat-auth-api"))
    api ("org.bitbucket.b_c:jose4j")
    api ("io.arrow-kt:arrow-core")
    api ("io.arrow-kt:arrow-resilience")
    api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api")
    api("org.eclipse.microprofile.config:microprofile-config-api")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testApi("jakarta.security.enterprise:jakarta.security.enterprise-api")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.eclipse.parsson:parsson")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed")
        events("failed")
    }
}

publishing {
    publications {
        create<MavenPublication>("project") {
            from(components["java"])
        }
    }
}
