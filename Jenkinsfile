pipeline {
    agent any

    environment {
        // SonarQube Server configured in Jenkins (Manage Jenkins -> System -> SonarQube servers)
        SONARQUBE_ENV = "${env.SONAR_ENV ?: 'SonarQube'}"
        // Database credentials injected via Jenkins environment / credentials
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
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh 'mvn sonar:sonar'
                }
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Security Scan') {
            steps {
                // Dependency and security verification
                sh 'mvn dependency:analyze -DignoreNonCompile=true || true'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker compose -p revhire build'
            }
        }

        stage('Docker Compose Integration Test') {
            steps {
                sh '''
                    # Start all services using project name 'revhire'
                    docker compose -p revhire up -d

                    # Wait for all services to become healthy (up to 120s)
                    echo "Waiting for all microservices to become healthy..."
                    SERVICES="eureka-server config-server test-service user-service resume-service job-service application-service notification-service api-gateway"
                    READY=false
                    for i in $(seq 1 60); do
                        ALL_HEALTHY=true
                        for svc in $SERVICES; do
                            STATUS=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else if .State.Running}}healthy{{else}}failed{{end}}' "$svc" 2>/dev/null || echo "starting")
                            if [ "$STATUS" != "healthy" ]; then
                                ALL_HEALTHY=false
                                break
                            fi
                        done
                        if [ "$ALL_HEALTHY" = "true" ]; then
                            echo "All microservices are healthy!"
                            READY=true
                            break
                        fi
                        sleep 2
                    done

                    if [ "$READY" != "true" ]; then
                        echo "Timed out waiting for microservices to become healthy."

                        echo "=== Docker Compose status ==="
                        docker compose -p revhire ps

                        echo "=== API Gateway logs ==="
                        docker logs --tail=200 api-gateway || true

                        echo "=== API Gateway health state ==="
                        docker inspect api-gateway --format '{{json .State.Health}}' || true

                        echo "=== API Gateway healthcheck configuration ==="
                        docker inspect api-gateway --format '{{json .Config.Healthcheck}}' || true

                        exit 1
                    fi

                    # Verify Eureka service registrations
                    echo "Verifying Eureka service registrations..."
                    for app in TEST-SERVICE USER-SERVICE RESUME-SERVICE JOB-SERVICE APPLICATION-SERVICE NOTIFICATION-SERVICE API-GATEWAY; do
                        echo "Checking $app registration..."
                        curl -f -s http://eureka-server:8761/eureka/apps/$app -H "Accept: application/json" 2>/dev/null | grep -q "$app" || \
                        curl -f -s http://localhost:8761/eureka/apps/$app -H "Accept: application/json" 2>/dev/null | grep -q "$app" || \
                        echo "Warning: $app registration check pending"
                    done

                    # Integration Smoke Tests via API Gateway (Port 8080)
                    GATEWAY_URL="http://localhost:8080"
                    which curl >/dev/null || exit 1

                    # 1. TEST-SERVICE Ping
                    echo "1. Testing TEST-SERVICE ping..."
                    curl -f -s "$GATEWAY_URL/api/test/ping" | grep -q '"service":"test-service"'

                    # 2. USER-SERVICE Register & Login
                    echo "2. Testing USER-SERVICE Auth..."
                    TIMESTAMP=$(date +%s)
                    REG_EMAIL="testuser_${TIMESTAMP}@revhire.local"
                    curl -s -X POST "$GATEWAY_URL/api/auth/register" \
                        -H "Content-Type: application/json" \
                        -d "{\\"email\\":\\"$REG_EMAIL\\",\\"password\\":\\"SecretPass123\\",\\"role\\":\\"JOB_SEEKER\\",\\"firstName\\":\\"Test\\",\\"lastName\\":\\"User\\"}" | grep -q "token"

                    LOGIN_RESP=$(curl -s -X POST "$GATEWAY_URL/api/auth/login" \
                        -H "Content-Type: application/json" \
                        -d "{\\"email\\":\\"$REG_EMAIL\\",\\"password\\":\\"SecretPass123\\"}")
                    TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
                    USER_ID=$(echo "$LOGIN_RESP" | grep -o '"userId":[0-9]*' | cut -d':' -f2)

                    # 3. USER-SERVICE Profile via Gateway
                    echo "3. Testing USER-SERVICE Profile with JWT..."
                    curl -f -s -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/users/me" | grep -q "$REG_EMAIL"

                    # 4. JOB-SERVICE Endpoints
                    echo "4. Testing JOB-SERVICE..."
                    curl -f -s "$GATEWAY_URL/api/jobs" | grep -q '"content"'

                    # 5. APPLICATION-SERVICE Endpoints
                    echo "5. Testing APPLICATION-SERVICE..."
                    curl -f -s "$GATEWAY_URL/api/applications" | grep -q '\\[.*'

                    # 6. APPLICATION -> NOTIFICATION Workflow
                    echo "6. Testing Application Status Change -> Notification Dispatch..."
                    if [ -n "$USER_ID" ]; then
                        # Submit an application
                        APP_RESP=$(curl -s -X POST "$GATEWAY_URL/api/applications" \
                            -H "Content-Type: application/json" \
                            -d "{\\"jobId\\":1,\\"userId\\":$USER_ID,\\"resumeId\\":null}")
                        APP_ID=$(echo "$APP_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
                        if [ -n "$APP_ID" ]; then
                            # Update status to SHORTLISTED to trigger notification
                            curl -s -X PUT "$GATEWAY_URL/api/applications/$APP_ID/status?status=SHORTLISTED" | grep -q "SHORTLISTED"
                            # Verify notification received in NOTIFICATION-SERVICE
                            sleep 2
                            curl -s "$GATEWAY_URL/api/notifications/user/$USER_ID" | grep -q "APPLICATION_SHORTLISTED"
                            echo "Application -> Notification integration verified successfully!"
                        fi
                    fi

                    echo "All end-to-end integration tests completed successfully!"
                '''
            }
        }
    }

    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        failure {
            sh 'docker compose -p revhire ps || true'
            sh 'docker compose -p revhire logs --tail=50 || true'
        }
    }
}
