pipeline {
    agent any

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
}
