plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    compileOnly(project(":common-weblogic"))
    compileOnly("org.glassfish.soteria:jakarta.security.enterprise:2.0.1")
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
