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
        GRADLE_OPTS = '-Dorg.gradle.daemon=false ' +
            '-Dorg.gradle.internal.publish.checksums.insecure=true ' +
            '-Dorg.gradle.console=plain'
        GRADLE_ARGS = "-Pversion.qualifier=RC-build-$BUILD_NUMBER --stacktrace"

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
                branch 'master'
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
                        withCredentials([gitUsernamePassword(credentialsId: 'Github-app-matrikkel')]) {
                            script {
                                def publishedVersion = readFile(file: 'build/published.version', encoding: 'UTF-8')
                                if (publishedVersion != null && !publishedVersion.empty) {
                                    sh "git tag -a ${publishedVersion} -m \"Tagged ${publishedVersion}\""
                                    sh "git push origin ${publishedVersion}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
