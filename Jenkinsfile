pipeline {
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-8'
        }
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Plugin') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Archive Build') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar'
            }
        }
    }
}