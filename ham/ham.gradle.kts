plugins {
    id ("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi(project(":api"))
    compileOnly(project(":common"))
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