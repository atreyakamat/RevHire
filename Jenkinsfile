pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        COMPOSE_PROJECT_NAME = 'revhire'
        SONARQUBE_ENV = "${env.SONAR_ENV ?: 'SonarQube'}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh '''
                    set -eu
                    echo "Branch: ${BRANCH_NAME:-rh-atreya}"
                    echo "Commit: $(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
                    test -f pom.xml
                    test -f docker-compose.yml
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
                    sh 'mvn -B sonar:sonar -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml'
                }
            }
        }

        stage('SonarQube Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate abortPipeline: false
                        echo "SonarQube Quality Gate: ${qg.status}"
                        if (qg.status != 'OK') {
                            echo "SonarQube Quality Gate did not pass (status: ${qg.status})."
                        } else {
                            echo "SonarQube Quality Gate passed successfully."
                        }
                    }
                }
            }
        }

        stage('Dependency Analysis') {
            steps {
                echo 'Running Maven dependency analysis...'
                sh 'mvn -B dependency:analyze -DignoreNonCompile=true'
                echo 'Note: dependency:analyze is not a dedicated SCA vulnerability scan.'
            }
        }

        stage('Build Docker Images') {
            steps {
                withCredentials([
                    string(credentialsId: 'revhire-internal-service-secret', variable: 'INTERNAL_SERVICE_SECRET'),
                    string(credentialsId: 'revhire-db-password', variable: 'REVHIRE_DB_PASSWORD')
                ]) {
                    sh '''
                        set -eu
                        if [ -z "${INTERNAL_SERVICE_SECRET:-}" ]; then
                            echo "ERROR: INTERNAL_SERVICE_SECRET must be supplied via credentials."
                            exit 1
                        fi
                        docker compose -p "$COMPOSE_PROJECT_NAME" config --quiet
                        docker compose -p "$COMPOSE_PROJECT_NAME" build
                    '''
                }
            }
        }

        stage('Docker Compose Integration Tests') {
            steps {
                withCredentials([
                    string(credentialsId: 'revhire-internal-service-secret', variable: 'INTERNAL_SERVICE_SECRET'),
                    string(credentialsId: 'revhire-db-password', variable: 'REVHIRE_DB_PASSWORD')
                ]) {
                    sh '''
                        set -eu
                        set +x

                        if [ -z "${INTERNAL_SERVICE_SECRET:-}" ]; then
                            echo "ERROR: INTERNAL_SERVICE_SECRET must be supplied via credentials."
                            exit 1
                        fi

                        echo "Starting RevHire microservice stack..."
                        docker compose -p "$COMPOSE_PROJECT_NAME" up -d

                        echo "Waiting for services to become healthy..."
                        SERVICES="eureka-server config-server test-service user-service resume-service job-service application-service notification-service api-gateway"
                        READY=false
                        for i in $(seq 1 75); do
                            ALL_HEALTHY=true
                            for svc in $SERVICES; do
                                STATUS=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else if .State.Running}}healthy{{else}}failed{{end}}' "$svc" 2>/dev/null || \
                                        docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else if .State.Running}}healthy{{else}}failed{{end}}' "${COMPOSE_PROJECT_NAME}-${svc}-1" 2>/dev/null || echo "starting")
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
                            docker compose -p "$COMPOSE_PROJECT_NAME" ps
                            docker logs --tail=100 api-gateway 2>/dev/null || true
                            exit 1
                        fi

                        # Discover gateway network dynamically
                        GATEWAY_ID=$(docker compose -p "$COMPOSE_PROJECT_NAME" ps -q api-gateway)
                        NETWORK=$(docker inspect -f '{{range $name, $conf := .NetworkSettings.Networks}}{{printf "%s" $name}}{{end}}' "$GATEWAY_ID" 2>/dev/null || echo "${COMPOSE_PROJECT_NAME}_revhire-network")

                        echo "Running integration tests on network: $NETWORK"

                        # Run integration test container inside the Compose network
                        CID=$(cat /etc/hostname 2>/dev/null || hostname)
                        if docker inspect "$CID" >/dev/null 2>&1; then
                            MOUNT_OPT="--volumes-from ${CID}:ro -w ${WORKSPACE}"
                        else
                            MOUNT_OPT="-v ${WORKSPACE}:/workspace:ro -w /workspace"
                        fi

                        docker run --rm \
                            --network "$NETWORK" \
                            $MOUNT_OPT \
                            alpine:3.20 \
                            sh -ec '
                                apk add --no-cache curl jq
                                sh ci/integration-test.sh
                            '

                        echo "All Docker Compose integration tests completed successfully!"
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar, **/target/site/jacoco/**',
                             allowEmptyArchive: true
        }

        failure {
            sh '''
                set +e
                echo "=== Docker Compose Service Status on Failure ==="
                docker compose -p "$COMPOSE_PROJECT_NAME" ps

                echo "=== Capturing Container Logs ==="
                docker compose -p "$COMPOSE_PROJECT_NAME" logs --no-color --tail=150 > compose-logs.txt 2>&1 || true
            '''
            archiveArtifacts artifacts: 'compose-logs.txt',
                             allowEmptyArchive: true
        }

        cleanup {
            sh '''
                set +e
                echo "Stopping RevHire containers (named volumes are preserved)..."
                docker compose -p "$COMPOSE_PROJECT_NAME" down
            '''
        }
    }
}
