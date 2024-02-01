plugins {
    id("java")
    id("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}
dependencies {
    compileOnlyApi(project(":mat-auth-api"))
    compileOnly(project(":mat-auth-common"))
    compileOnly("jakarta.security.enterprise:jakarta.security.enterprise-api")
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
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
