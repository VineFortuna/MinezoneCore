pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install local libs') {
            steps {
                sh 'mvn install:install-file -Dfile=lib/npclib-api.jar -DgroupId=net.jitse -DartifactId=npclib-api -Dversion=2.7-SNAPSHOT -Dpackaging=jar'
            }
        }

        stage('Build') {
            steps {
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