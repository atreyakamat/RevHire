
pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    environment {
        COMPOSE_PROJECT_NAME = 'revhire'
        SONARQUBE_ENV = 'SonarQube'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh '''
                    set -eu
                    echo "Branch: ${BRANCH_NAME:-unknown}"
                    echo "Commit: $(git rev-parse --short HEAD)"
                    test -f pom.xml
                    test -f docker-compose.yml || test -f compose.yml
                    test -f ci/integration-test.sh
                '''
            }
        }

        stage('Build and Unit Tests') {
            steps {
                sh 'mvn -B clean test'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml',
                          allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh 'mvn -B sonar:sonar'
                }
            }
        }

        stage('SonarQube Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qualityGate = waitForQualityGate()
                        echo "SonarQube Quality Gate: ${qualityGate.status}"

                        if (qualityGate.status != 'OK') {
                            error("SonarQube Quality Gate failed: ${qualityGate.status}")
                        }
                    }
                }
            }
        }

        stage('Dependency Analysis') {
            steps {
                sh 'mvn -B dependency:analyze -DignoreNonCompile=true'
                echo 'Note: dependency:analyze is not a vulnerability scan.'
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                    set -eu
                    docker compose -p "$COMPOSE_PROJECT_NAME" config --quiet
                    docker compose -p "$COMPOSE_PROJECT_NAME" build
                '''
            }
        }

        stage('Docker Compose Integration Tests') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'revhire-internal-service-secret',
                        variable: 'INTERNAL_SERVICE_SECRET'
                    ),
                    string(
                        credentialsId: 'revhire-db-password',
                        variable: 'REVHIRE_DB_PASSWORD'
                    )
                ]) {
                    sh '''
                        set -eu
                        set +x

                        echo "Starting RevHire Compose stack..."
                        docker compose -p "$COMPOSE_PROJECT_NAME" up \
                            -d --wait --wait-timeout 240

                        echo "Compose service status:"
                        docker compose -p "$COMPOSE_PROJECT_NAME" ps

                        echo "Running integration tests inside the Compose network..."

                        # The default network created by Compose is
                        # <project-name>_default.
                        # The temporary test container can resolve Compose
                        # service names such as api-gateway and eureka-server.
                        docker run --rm \
                            --network "${COMPOSE_PROJECT_NAME}_default" \
                            -v "$WORKSPACE:/workspace:ro" \
                            -w /workspace \
                            alpine:3.20 \
                            sh -ec '
                                apk add --no-cache curl jq
                                sh ci/integration-test.sh
                            '
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar',
                             allowEmptyArchive: true
        }

        failure {
            script {
                sh '''
                    set +e
                    docker compose -p "$COMPOSE_PROJECT_NAME" ps
                    docker compose -p "$COMPOSE_PROJECT_NAME" logs \
                        --no-color --tail=200 > compose-logs.txt
                '''
                archiveArtifacts artifacts: 'compose-logs.txt',
                                 allowEmptyArchive: true
            }
        }

        cleanup {
            sh '''
                set +e
                echo "Stopping RevHire containers (named volumes are preserved)."
                docker compose -p "$COMPOSE_PROJECT_NAME" down
            '''
        }
    }
}
