#!groovy
@Library(['common']) _

pipeline {
    agent {
        node {
            label 'matrikkelen'
        }
    }

    tools {
        jdk 'Java 8 Latest'
    }

    environment {
        GRADLE_USER_HOME = "$WORKSPACE/.gradle"
        GRADLE_OPTS = '-Dorg.gradle.daemon=false ' +
            '-Dmaven.repo.local=$WORKSPACE/.m2/repository ' +
            '-Dorg.gradle.internal.publish.checksums.insecure=true ' +
            '-Dorg.gradle.console=plain'
//         GRADLE_ARGS = "-Pversion.qualifier=RC-build-$BUILD_NUMBER --stacktrace"
        GRADLE_ARGS = "-Pversion.qualifier=RC-build-test --stacktrace"

        MAVEN_PUBLISH = credentials('MAVEN_DEPLOY_RELEASE_CANDIDATE')
    }

    stages {
        stage('Build') {
            steps {
                withGradle {
                    sh './gradlew $GRADLE_ARGS clean assemble'
                }
            }
        }
        stage('Publish and Tag') {
            when {
                branch '*'
            }
            stages {
                stage('Publish') {
                    steps {
                        withGradle {
                            sh './gradlew publish $GRADLE_ARGS --init-script gradle/mavenPublish.gradle'
                        }
                    }
                }
                stage('Tag') {
                    when {
                        expression { fileExists('build/published.version') }
                    }
                    steps {
                        withCredentials([usernamePassword(credentialsId: 'Github-app-matrikkel',
                                                          usernameVariable: 'GITHUB_APP',
                                                          passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
                            script {
                                def publishedVersion = readFile(file: 'build/published.version', encoding: 'UTF-8')
                                if (publishedVersion != null && !publishedVersion.empty) {
                                     tag(publishedVersion)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
