plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    compileOnly(project(":mat-auth-common-weblogic"))
    compileOnly("org.glassfish.soteria:jakarta.security.enterprise:3.0.3")
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
