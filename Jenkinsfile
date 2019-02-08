pipeline {
    agent any

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
