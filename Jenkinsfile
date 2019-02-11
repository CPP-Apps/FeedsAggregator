pipeline {
    agent {
        docker { image 'openjdk:8-jdk-slim' }
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew assemble'
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
