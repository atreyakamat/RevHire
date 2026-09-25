pipeline {
    agent any

    environment {
        // Name of the SonarQube Server configured in Jenkins (Manage Jenkins -> System -> SonarQube servers)
        SONARQUBE_ENV = "${env.SONAR_ENV ?: 'SonarQube'}"
        // Database credentials injected via Jenkins environment / credentials if configured
        REVHIRE_DB_PASSWORD = "${env.REVHIRE_DB_PASSWORD ?: ''}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Tests') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        echo "Checking SonarQube environment..."

                        if [ -n "$SONAR_HOST_URL" ]; then
                            echo "SONAR_HOST_URL = SET"
                        else
                            echo "SONAR_HOST_URL = NOT SET"
                        fi

                        if [ -n "$SONAR_AUTH_TOKEN" ]; then
                            echo "SONAR_AUTH_TOKEN = SET"
                        else
                            echo "SONAR_AUTH_TOKEN = NOT SET"
                        fi

                        if [ -n "$SONAR_TOKEN" ]; then
                            echo "SONAR_TOKEN = SET"
                        else
                            echo "SONAR_TOKEN = NOT SET"
                        fi
                    '''
                }
            }
        }


        stage('Docker Build') {
            steps {
                sh 'docker compose build eureka-server config-server test-service api-gateway'
            }
        }

        stage('Docker Compose Integration Test') {
            steps {
                sh '''
                    # Start the infrastructure and integration test services
                    docker compose up -d eureka-server config-server test-service api-gateway

                    # Poll for healthy status on dependencies and running status on api-gateway (up to 60s)
                    echo "Waiting for services to become healthy..."
                    READY=false
                    for i in $(seq 1 30); do
                        EUREKA_STATUS=$(docker inspect --format '{{.State.Health.Status}}' eureka-server 2>/dev/null || echo "starting")
                        CONFIG_STATUS=$(docker inspect --format '{{.State.Health.Status}}' config-server 2>/dev/null || echo "starting")
                        TEST_STATUS=$(docker inspect --format '{{.State.Health.Status}}' test-service 2>/dev/null || echo "starting")
                        GATEWAY_STATUS=$(docker inspect --format '{{.State.Health.Status}}' api-gateway 2>/dev/null || echo "starting")

                        if [ "$EUREKA_STATUS" = "healthy" ] && [ "$CONFIG_STATUS" = "healthy" ] && [ "$TEST_STATUS" = "healthy" ] && [ "$GATEWAY_STATUS" = "healthy" ]; then
                            echo "All required services are healthy and running."
                            READY=true
                            break
                        fi
                        sleep 2
                    done

                    if [ "$READY" != "true" ]; then
                        echo "Timed out waiting for services to become healthy."
                        docker compose ps
                        exit 1
                    fi

                    # Verify TEST-SERVICE registration in Eureka
                    echo "Verifying TEST-SERVICE registration in Eureka..."
                    curl -f -s http://eureka-server:8761/eureka/apps/TEST-SERVICE -H "Accept: application/json" 2>/dev/null | grep -q "TEST-SERVICE" || \
                    curl -f -s http://localhost:8761/eureka/apps/TEST-SERVICE -H "Accept: application/json" | grep -q "TEST-SERVICE"

                    # Verify end-to-end routing through API Gateway
                    echo "Verifying end-to-end request through API Gateway..."
                    RESPONSE=""
                    for j in $(seq 1 15); do
                        RESPONSE=$(curl -f -s http://api-gateway:8080/api/test/ping 2>/dev/null || curl -f -s http://localhost:8080/api/test/ping 2>/dev/null || echo "")
                        if echo "$RESPONSE" | grep -q '"service":"test-service"'; then
                            break
                        fi
                        sleep 2
                    done
                    echo "Gateway Response: $RESPONSE"
                    echo "$RESPONSE" | grep -q '"service":"test-service"'
                '''
            }
        }
    }

    post {
        always {
            sh 'docker compose rm -f -s || true'
        }
    }
}
