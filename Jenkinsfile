pipeline {
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-8'
        }
    }

    stages {

        stage('Build') {
            steps {
                checkout scm
                sh 'mvn clean package'
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar'
            }
        }
    }
}