plugins {
    id("java")
    id("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}
dependencies {
    compileOnlyApi(project(":api"))
    compileOnly(project(":common"))
    compileOnly("javax.ws.rs:javax.ws.rs-api")
    compileOnly("com.google.guava:guava")
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