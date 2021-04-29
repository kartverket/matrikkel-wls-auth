plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    compileOnlyApi ("javax.enterprise:cdi-api")
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