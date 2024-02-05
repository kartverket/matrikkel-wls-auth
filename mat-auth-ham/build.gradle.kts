plugins {
    id ("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi(project(":mat-auth-api"))
    compileOnly(project(":mat-auth-common"))
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
