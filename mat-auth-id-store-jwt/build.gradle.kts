plugins {
    id("java")
    id("java-library")
    id ("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnlyApi(project(":mat-auth-api"))
    compileOnly(project(":mat-auth-shared"))
    compileOnly("org.bitbucket.b_c:jose4j")
    compileOnly("org.eclipse.microprofile.config:microprofile-config-api")
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
