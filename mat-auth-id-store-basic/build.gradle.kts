plugins {
    id("java")
    id("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}
dependencies {
    compileOnlyApi(project(":mat-auth-api"))
    compileOnly(project(":mat-auth-shared"))
    compileOnly("jakarta.inject:jakarta.inject-api")
    compileOnly("jakarta.security.enterprise:jakarta.security.enterprise-api")
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation(platform(project(":mat-auth-platform")))
    testImplementation("io.arrow-kt:arrow-core")
    testImplementation("io.arrow-kt:arrow-resilience")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("project") {
            from(components["java"])
        }
    }
}
