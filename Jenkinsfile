pipeline {
    agent any

    tools {
        jdk 'Java25'
    }

    environment {
        IMAGE_NAME = 'resumatch'
        SONARQUBE_SERVER = 'SonarQube'
    }

    stages {
        stage('Secret scan') {
            steps {
                sh 'gitleaks detect --source . --redact --exit-code 1'
            }
        }

        stage('Build and test') {
            steps {
                sh 'mvn -B clean verify'
            }
        }

        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_SERVER}") {
                    sh 'mvn -B sonar:sonar -Dsonar.projectKey=resumatch'
                }
            }
        }

        stage('Build and load K3s image') {
            steps {
                sh '''
                    set -eu
                    docker build -t "$IMAGE_NAME:$BUILD_NUMBER" .
                    image_archive="$(mktemp)"
                    trap 'rm -f "$image_archive"' EXIT
                    docker save --output "$image_archive" "$IMAGE_NAME:$BUILD_NUMBER"
                    sudo k3s ctr images import "$image_archive"
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    kubectl apply -f k8s/
                    kubectl set image deployment/resumatch \
                      resumatch="$IMAGE_NAME:$BUILD_NUMBER"
                    kubectl rollout status deployment/resumatch --timeout=180s
                '''
            }
        }
    }
}
