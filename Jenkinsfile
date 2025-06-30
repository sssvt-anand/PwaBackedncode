pipeline {
    agent any

    tools {
        maven 'Maven 3.8.1' // Name configured in Jenkins > Global Tools Configuration
        jdk 'Java 17'       // Or your installed JDK version in Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/sssvt-anand/PwaBackedncode.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
    }
}
