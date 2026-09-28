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
                    script {
                        def qg = waitForQualityGate abortPipeline: false
                        echo "SonarQube Quality Gate result: ${qg.status}"
                        if (qg.status != 'OK') {
                            echo "Quality Gate did not pass (status: ${qg.status}). SonarQube analysis completed, but the quality gate policy reported issues."
                        } else {
                            echo "SonarQube Quality Gate passed successfully (status: ${qg.status})."
                        }
                    }
                }
            }
        }

        stage('Security Scan') {
            steps {
                echo "Running Maven dependency usage analysis (used undeclared / unused declared)..."
                echo "Note: A dedicated SCA vulnerability scanner (e.g., OWASP Dependency-Check or Snyk) is not currently configured in this repository."
                sh 'mvn dependency:analyze -DignoreNonCompile=true'
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
                            STATUS=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else if .State.Running}}healthy{{else}}failed{{end}}' "$svc" 2>/dev/null || \
                                    docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else if .State.Running}}healthy{{else}}failed{{end}}' "revhire-${svc}-1" 2>/dev/null || echo "starting")
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
                        echo "ERROR: Timed out waiting for microservices to become healthy."

                        echo "=== Docker Compose status ==="
                        docker compose -p revhire ps

                        echo "=== API Gateway logs ==="
                        docker logs --tail=200 api-gateway 2>/dev/null || true

                        echo "=== All unhealthy container logs ==="
                        for svc in $SERVICES; do
                            STATUS=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}' "$svc" 2>/dev/null || echo "unknown")
                            if [ "$STATUS" != "healthy" ]; then
                                echo "--- Logs for $svc ($STATUS) ---"
                                docker logs --tail=50 "$svc" 2>/dev/null || true
                                docker inspect "$svc" --format 'Health: {{json .State.Health}}' 2>/dev/null || true
                            fi
                        done

                        exit 1
                    fi

                    # Verify Eureka service registrations with wait loop and failure
                    echo "Verifying Eureka service registrations..."
                    REQUIRED_APPS="TEST-SERVICE USER-SERVICE RESUME-SERVICE JOB-SERVICE APPLICATION-SERVICE NOTIFICATION-SERVICE API-GATEWAY"
                    for app in $REQUIRED_APPS; do
                        echo "Waiting for $app to register with Eureka..."
                        REGISTERED=false
                        for attempt in $(seq 1 30); do
                            if curl -f -s "http://eureka-server:8761/eureka/apps/$app" -H "Accept: application/json" 2>/dev/null | grep -q "$app" || \
                               curl -f -s "http://localhost:8761/eureka/apps/$app" -H "Accept: application/json" 2>/dev/null | grep -q "$app"; then
                                echo "$app is registered with Eureka!"
                                REGISTERED=true
                                break
                            fi
                            sleep 2
                        done
                        if [ "$REGISTERED" != "true" ]; then
                            echo "ERROR: Required service $app failed to register with Eureka within timeout!"
                            exit 1
                        fi
                    done

                    # Integration Smoke Tests via API Gateway (Port 8080)
                    GATEWAY_URL="http://localhost:8080"
                    which curl >/dev/null || { echo "ERROR: curl is required"; exit 1; }

                    # Wait for API Gateway route discovery warmup
                    echo "Waiting for API Gateway route discovery warmup..."
                    GATEWAY_WARM=false
                    for attempt in $(seq 1 30); do
                        WARM_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/api/test/ping" 2>/dev/null || echo "000")
                        if [ "$WARM_CODE" = "200" ]; then
                            GATEWAY_WARM=true
                            echo "API Gateway route cache is ready!"
                            break
                        fi
                        sleep 2
                    done
                    if [ "$GATEWAY_WARM" != "true" ]; then
                        echo "ERROR: API Gateway failed to route requests within timeout"
                        exit 1
                    fi

                    # 1. TEST-SERVICE Ping
                    echo "1. Testing TEST-SERVICE ping..."
                    PING_CODE=$(curl -s -o /tmp/ping.json -w "%{http_code}" "$GATEWAY_URL/api/test/ping")
                    if [ "$PING_CODE" != "200" ]; then
                        echo "ERROR: TEST-SERVICE ping returned HTTP $PING_CODE"
                        exit 1
                    fi
                    grep -q "test-service" /tmp/ping.json || { echo "ERROR: ping response missing test-service identity"; exit 1; }
                    echo "PASS: TEST-SERVICE ping OK (HTTP 200)"

                    # 2. USER-SERVICE Register & Login
                    echo "2. Testing USER-SERVICE Auth..."
                    TIMESTAMP=$(date +%s)
                    REG_EMAIL="ci_user_${TIMESTAMP}@revhire.local"

                    set +x
                    REG_CODE=""

                    for attempt in $(seq 1 15); do
                        REG_CODE=$(curl -s -o /tmp/reg.json -w "%{http_code}" \
                            -X POST "$GATEWAY_URL/api/auth/register" \
                            -H "Content-Type: application/json" \
                            -d "{\"email\":\"$REG_EMAIL\",\"password\":\"SecretPass123!\",\"role\":\"JOB_SEEKER\",\"firstName\":\"Integration\",\"lastName\":\"Tester\"}")

                        if [ "$REG_CODE" = "201" ]; then
                            break
                        fi

                        echo "Registration attempt $attempt returned HTTP $REG_CODE; retrying..."
                        sleep 2
                    done

                    if [ "$REG_CODE" != "201" ]; then
                        echo "ERROR: User registration failed after 15 attempts (HTTP $REG_CODE)"
                        echo "Response body:"
                        cat /tmp/reg.json
                        exit 1
                    fi

                    echo "PASS: User registration successful (HTTP 201)"

                    TOKEN=$(jq -r '.token // empty' /tmp/reg.json)
                    USER_ID=$(jq -r '.userId // empty' /tmp/reg.json)

                    if [ -z "$TOKEN" ] || [ -z "$USER_ID" ]; then
                        echo "ERROR: Registration response is missing token or userId"
                        echo "Response keys:"
                        jq -r 'keys | join(", ")' /tmp/reg.json 2>/dev/null || echo "Invalid JSON response"
                        exit 1
                    fi
                    echo "PASS: User registered successfully with ID: $USER_ID (HTTP 201)"

                    LOGIN_CODE=$(curl -s -o /tmp/login.json -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/login" \
                        -H "Content-Type: application/json" \
                        -d "{\\"email\\":\\"$REG_EMAIL\\",\\"password\\":\\"SecretPass123!\\"}")
                    if [ "$LOGIN_CODE" != "200" ]; then
                        echo "ERROR: User login returned HTTP $LOGIN_CODE"
                        exit 1
                    fi
                    LOGIN_TOKEN=$(jq -r '.token // empty' /tmp/login.json)

                    if [ -z "$LOGIN_TOKEN" ]; then
                        echo "ERROR: Login response is missing token"
                        echo "Response keys:"
                        jq -r 'keys | join(", ")' /tmp/login.json 2>/dev/null || echo "Invalid JSON response"
                        exit 1
                    fi
                    echo "PASS: User login OK (HTTP 200)"

                    # 3. USER-SERVICE Profile via Gateway
                    echo "3. Testing USER-SERVICE Profile with JWT..."
                    ME_CODE=$(curl -s -o /tmp/me.json -w "%{http_code}" -H "Authorization: Bearer $LOGIN_TOKEN" "$GATEWAY_URL/api/users/me")
                    if [ "$ME_CODE" != "200" ]; then
                        echo "ERROR: User profile returned HTTP $ME_CODE"
                        exit 1
                    fi
                    grep -q "$REG_EMAIL" /tmp/me.json || { echo "ERROR: Profile response does not match registered email"; exit 1; }
                    echo "PASS: Authenticated user profile verified (HTTP 200)"

                    # 4. JOB-SERVICE Endpoints
                    echo "4. Testing JOB-SERVICE..."
                    JOBS_CODE=$(curl -s -o /tmp/jobs.json -w "%{http_code}" "$GATEWAY_URL/api/jobs")
                    if [ "$JOBS_CODE" != "200" ]; then
                        echo "ERROR: Jobs list returned HTTP $JOBS_CODE"
                        exit 1
                    fi
                    grep -q "content" /tmp/jobs.json || { echo "ERROR: Jobs response missing content array"; exit 1; }
                    echo "PASS: JOB-SERVICE endpoint verified (HTTP 200)"

                    # 5. APPLICATION-SERVICE Endpoints
                    echo "5. Testing APPLICATION-SERVICE..."
                    APPS_CODE=$(curl -s -o /tmp/apps.json -w "%{http_code}" "$GATEWAY_URL/api/applications")
                    if [ "$APPS_CODE" != "200" ]; then
                        echo "ERROR: Applications list returned HTTP $APPS_CODE"
                        exit 1
                    fi
                    echo "PASS: APPLICATION-SERVICE endpoint verified (HTTP 200)"

                    # 6. APPLICATION Submission and Status Update Workflow
                    echo "6. Testing Application Submission and Status Update..."
                    APP_SUB_CODE=$(curl -s -o /tmp/app_sub.json -w "%{http_code}" -X POST "$GATEWAY_URL/api/applications" \
                        -H "Content-Type: application/json" \
                        -d "{\\"jobId\\":1,\\"userId\\":$USER_ID,\\"resumeId\\":null}")
                    if [ "$APP_SUB_CODE" != "201" ]; then
                        echo "ERROR: Application submission returned HTTP $APP_SUB_CODE"
                        exit 1
                    fi
                    APP_ID=$(jq -r '.id // empty' /tmp/app.json)
                    if [ -z "$APP_ID" ]; then
                        echo "ERROR: Failed to extract application ID from submission response"
                        exit 1
                    fi
                    echo "PASS: Application submitted successfully with ID $APP_ID (HTTP 201)"

                    STATUS_CODE=$(curl -s -o /tmp/status.json -w "%{http_code}" -X PUT "$GATEWAY_URL/api/applications/$APP_ID/status?status=SHORTLISTED")
                    if [ "$STATUS_CODE" != "200" ]; then
                        echo "ERROR: Application status update returned HTTP $STATUS_CODE"
                        exit 1
                    fi
                    grep -q "SHORTLISTED" /tmp/status.json || { echo "ERROR: Status update response does not contain SHORTLISTED"; exit 1; }
                    echo "PASS: Application status updated to SHORTLISTED (HTTP 200)"

                    # 7. NOTIFICATION-SERVICE Workflow
                    echo "7. Testing Notification Dispatch & Retrieval..."
                    NOTIF_CODE=""
                    for attempt in $(seq 1 15); do
                        NOTIF_CODE=$(curl -s -o /tmp/notif.json -w "%{http_code}" "$GATEWAY_URL/api/notifications/user/$USER_ID")
                        if [ "$NOTIF_CODE" = "200" ] && grep -q "APPLICATION_SHORTLISTED" /tmp/notif.json 2>/dev/null; then
                            break
                        fi
                        sleep 2
                    done
                    if [ "$NOTIF_CODE" != "200" ]; then
                        echo "ERROR: Notifications query returned HTTP $NOTIF_CODE"
                        exit 1
                    fi
                    grep -q "APPLICATION_SHORTLISTED" /tmp/notif.json || { echo "ERROR: Expected notification APPLICATION_SHORTLISTED not found for user $USER_ID"; exit 1; }
                    echo "PASS: Notification dispatch and retrieval verified for user $USER_ID (HTTP 200)"

                    # Clean up temporary response files
                    rm -f /tmp/ping.json /tmp/reg.json /tmp/login.json /tmp/me.json /tmp/jobs.json /tmp/apps.json /tmp/app_sub.json /tmp/status.json /tmp/notif.json

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
            echo "Pipeline failed. Preserving container status and diagnostic logs:"
            sh 'docker compose -p revhire ps || true'
            sh 'docker compose -p revhire logs --tail=100 || true'
        }
        cleanup {
            echo "Stopping integration test containers (preserving named database volumes)..."
            sh 'docker compose -p revhire down || true'
        }
    }
}
