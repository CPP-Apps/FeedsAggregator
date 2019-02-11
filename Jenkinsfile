pipeline {
    agent {
        docker {
            label 'docker-agent'
            image 'openjdk:8-jdk-slim'
        }
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew --no-daemon assemble'
            }
        }
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts 'build/libs/*.war'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}