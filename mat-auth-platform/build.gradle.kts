plugins {
    id("java-platform")
    id("maven-publish")
}
dependencies {
    constraints {
        api("org.bitbucket.b_c:jose4j:0.9.6") //Json Web Token (JWT)
        api("io.arrow-kt:arrow-core:0.11.0")
        api("io.arrow-kt:arrow-fx-coroutines:0.11.0")
        api("io.arrow-kt:arrow-optics:0.11.0")
        api("io.arrow-kt:arrow-meta:0.11.0")
        api("org.slf4j:slf4j-api:2.0.9")
        api("org.eclipse.microprofile.config:microprofile-config-api:1.4")
        api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api") {
//            because("CDI < 2.0")
            version {
//                strictly("[1.1.1, 2.0[")
                prefer("1.1.1")
            }
        }
        //
        // WebLogic 14 implementerer Java EE 8
        //
        api("jakarta.security.enterprise:jakarta.security.enterprise-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[1.0.1, 2.0[")
                prefer("2.0.0")
            }
        }
        api("jakarta.ws.rs:jakarta.ws.rs-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[2.1.5, 3.0[")
                prefer("3.0.0")
            }
        }
        api("jakarta.enterprise:jakarta.enterprise.cdi-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[2.0.1, 3.0[")
                prefer("3.0.1")
            }
        }
        api("jakarta.servlet:jakarta.servlet-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[4.0.2, 6.0[")
                prefer("5.0.0")
            }
        }
        api("jakarta.annotation:jakarta.annotation-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[1.3.4, 2.0[")
                prefer("2.0.0")
            }
        }
        api("jakarta.ejb:jakarta.ejb-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[3.2.5, 4.0[")
                prefer("4.0.1")
            }
        }
        api("org.jboss.weld:weld-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("3.1.Final")
            }
        }
        api("org.jboss.weld:weld-spi") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("3.1.Final")
            }
        }
        api("com.google.guava:guava:32.1.2-jre")
        api("jakarta.security.jacc:jakarta.security.jacc-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[1.6.1, 2.0[")
                prefer("1.6.1")
            }
        }
        api("jakarta.security.auth.message:jakarta.security.auth.message-api") {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[1.1.2, 2.0[")
                prefer("2.0.0-RC1")
            }
        }
        api("jakarta.json:jakarta.json-api")  {
//            because("WebLogic 14.1.1.0")
            version {
//                strictly("[1.1.5, 2.0[")
                prefer("2.0.2")
            }
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])
        }
    }
}
