plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    compileOnly(project(":common-weblogic"))
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