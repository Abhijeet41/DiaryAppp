pipeline {
    agent any

    tools {
        jdk 'OracleJDK11'
    }

    stages {
        stage('Check ANDROID_HOME') {
            steps {
                sh 'echo "ANDROID_HOME is set to: $ANDROID_HOME"'
                sh 'ls $ANDROID_HOME'
            }
        }

        stage('Set Permissions') {
            steps {
                sh 'chmod +x ./gradlew'
            }
        }

        stage('Set up SDK path') {
            steps {
                sh 'echo "sdk.dir=$ANDROID_HOME" > local.properties'
            }
        }

        /* stage('Clean Gradle') {
            steps {
                sh './gradlew clean --no-daemon'
            }
        } */

        stage('Build') {
            steps {
                sh './gradlew assembleRelease'
            }
        }
        stage('SonarQube Analysis') {
               environment {
                  SONAR_TOKEN = credentials('sonartoken')  // Reference the credential ID created in Jenkins
                  SONARQUBE_URL = 'http://192.168.56.82:9000'
               }
                steps {
                    withSonarQubeEnv('sonarserver') {  // Replace 'sonarserver' with the name of your SonarQube server in Jenkins
                         sh '''
                         sonar-scanner \
                         -Dsonar.projectKey=DiaryApp \
                         -Dsonar.sources=. \
                         -Dsonar.host.url=$SONARQUBE_URL \
                         -Dsonar.login=$SONAR_TOKEN'''
                        }
                    }
                }
    }

    post {
        success {
            archiveArtifacts artifacts: 'app/build/outputs/apk/release/*.apk', allowEmptyArchive: true
            echo 'Build completed successfully!'
        }
        failure {
            echo 'Build failed. Please check the logs for errors.'
        }
    }
}