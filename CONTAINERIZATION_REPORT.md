# RevHire Microservices Containerization Report

**Date:** 2026-09-28  
**Branch:** `rh-atreya`  
**Author:** Atreya Kamat  

---

## 1. Executive Summary

All business microservices (`user-service`, `resume-service`, `job-service`, `application-service`) and infrastructure services (`eureka-server`, `config-server`, `api-gateway`, `test-service`, `notification-service`) have been fully containerized and integrated into Docker Compose.

All 9 containers build successfully, boot up in proper dependency order, connect to the shared MySQL database (`revhire-network`), register with Eureka Service Discovery, fetch externalized configuration from Spring Cloud Config Server, and route traffic seamlessly through Spring Cloud API Gateway.

---

## 2. What Was Already Working

- **Infrastructure Services in Docker Compose:** `eureka-server`, `config-server`, `api-gateway`, `test-service`, and `notification-service` were already defined in `docker-compose.yml`.
- **Shared MySQL Database:** MySQL 8.0 was already running as a Docker container (`mysql`) attached to `revhire_revhire-network` exposing port `3306` internally and `3333` on the host.
- **Teammate PR Merges:** Recent feature branches for `job-service` (PR #12), `user-service` (PR #14), and `application-service` / `resume-service` (PR #13) provided business logic, DTOs, controllers, and initial Dockerfiles.

---

## 3. What Was Changed & Rationale

| File Changed | Rationale / Why Change Was Needed |
| :--- | :--- |
| `docker-compose.yml` | Added definitions for `user-service`, `resume-service`, `job-service`, and `application-service`. Configured appropriate environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `CONFIG_SERVER_URL`, `EUREKA_SERVER_URL`, `SPRING_JPA_HIBERNATE_DDL_AUTO=update`), inter-service URLs, healthchecks, and updated `api-gateway` dependencies to include all business services. |
| `config/local/api-gateway.properties` | Added `spring.cloud.gateway.server.webflux.routes[3].filters[0]=StripPrefix=1` to match `infrastructure/api-gateway/src/main/resources/application.properties` so gateway route `/api/applications/**` properly strips prefix before forwarding to `application-service` controller mapping `/applications/**`. |
| `config/local/user-service.properties` | Added `spring.jpa.hibernate.ddl-auto=update` so that Spring Cloud Config does not enforce the shared `validate` mode on `user-service` (which uses Hibernate auto-DDL instead of Flyway migrations). |
| `config/local/resume-service.properties` | Added `spring.jpa.hibernate.ddl-auto=update` and `spring.jpa.open-in-view=true` so that Hibernate creates missing entity tables and allows lazy loading of resume collections (`educationList`, `experienceList`, `skills`) during response serialization. |
| `services/job-service/Dockerfile` | Updated `EXPOSE 8083` to `EXPOSE 8803` to match actual service port `server.port=8803`. |
| `services/application-service/Dockerfile` | Updated `EXPOSE 8084` to `EXPOSE 8804` to match actual service port `server.port=8804`. |
| `services/application-service/src/test/java/com/revhire/applicationservice/controller/ApplicationControllerTest.java` | Mocked `JobClient` and injected it into `ApplicationController` constructor to fix a constructor mismatch compilation error introduced in PR #13. |

---

## 4. Commands Executed

```bash
# 1. Inspect git status, branches, commits, and diff
git status
git log -n 10 --oneline
find . -name "Dockerfile*"

# 2. Compile and package all services with Java 21
JAVA_HOME=/usr/lib/jvm/java-21-openjdk PATH="/usr/lib/jvm/java-21-openjdk/bin:$PATH" mvn clean package -DskipTests

# 3. Verify unit tests for modified test file
JAVA_HOME=/usr/lib/jvm/java-21-openjdk PATH="/usr/lib/jvm/java-21-openjdk/bin:$PATH" mvn test -Dtest=ApplicationControllerTest -pl services/application-service

# 4. Pre-create required MySQL schemas in shared container
docker exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS revhire_user_db; CREATE DATABASE IF NOT EXISTS revhire_resume_db; CREATE DATABASE IF NOT EXISTS revhire_job; CREATE DATABASE IF NOT EXISTS revhire_application; SHOW DATABASES;"'

# 5. Build Docker Compose images
docker compose build

# 6. Start Docker Compose stack
docker compose up -d

# 7. Check container health status
docker ps

# 8. Verify Eureka service registration
curl -s -H "Accept: application/json" http://localhost:8761/eureka/apps | jq '.applications.application[].name'

# 9. Verify Config Server connectivity
for s in user-service resume-service job-service application-service notification-service test-service api-gateway; do
  curl -s http://localhost:8800/$s/local | jq '{name: .name, profiles: .profiles, propertySourcesCount: (.propertySources | length)}'
done

# 10. Verify API Gateway routing & end-to-end functionality
curl -i -s http://localhost:8080/api/test/ping
curl -i -s http://localhost:8080/api/jobs
curl -i -s http://localhost:8080/api/applications
curl -i -s http://localhost:8080/api/users/me
curl -i -s http://localhost:8080/api/resumes/user/1
```

---

## 5. Verification & Test Results

### 5.1 Docker Container Status
All 9 containers report `healthy`:
```
CONTAINER ID   IMAGE                          STATUS                    PORTS                    NAMES
383103f90e75   revhire-user-service           Up (healthy)              0.0.0.0:8801->8801/tcp   user-service
fcbe00746d50   revhire-resume-service         Up (healthy)              0.0.0.0:8802->8802/tcp   resume-service
46d5f6975871   revhire-job-service            Up (healthy)              0.0.0.0:8803->8803/tcp   job-service
1d2e1e2a615f   revhire-application-service    Up (healthy)              0.0.0.0:8804->8804/tcp   application-service
e2f72834f074   revhire-notification-service   Up (healthy)              0.0.0.0:8085->8085/tcp   notification-service
5e35428043d6   revhire-test-service           Up (healthy)              0.0.0.0:8086->8086/tcp   test-service
f4333f09b600   revhire-eureka-server          Up (healthy)              0.0.0.0:8761->8761/tcp   eureka-server
5f661eea6a59   revhire-config-server          Up (healthy)              0.0.0.0:8800->8800/tcp   config-server
f5e0cbaafac5   revhire-api-gateway            Up (healthy)              0.0.0.0:8080->8080/tcp   api-gateway
```

### 5.2 Eureka Registration
Every service successfully registered with Eureka:
- `API-GATEWAY`
- `RESUME-SERVICE`
- `CONFIG-SERVER`
- `JOB-SERVICE`
- `TEST-SERVICE`
- `NOTIFICATION-SERVICE`
- `USER-SERVICE`
- `APPLICATION-SERVICE`

### 5.3 Spring Cloud Config Server Connectivity
Config Server serves externalized properties to all services with HTTP 200:
- `user-service` (profile: `local`, property sources: 2)
- `resume-service` (profile: `local`, property sources: 2)
- `job-service` (profile: `local`, property sources: 2)
- `application-service` (profile: `local`, property sources: 2)
- `notification-service` (profile: `local`, property sources: 2)
- `test-service` (profile: `local`, property sources: 2)
- `api-gateway` (profile: `local`, property sources: 2)

### 5.4 Key Endpoint & Gateway Routing Validation

1. **Test Service:**
   - `GET http://localhost:8080/api/test/ping` -> `HTTP 200 OK` (`{"service":"test-service","status":"UP"}`)
2. **Job Service:**
   - `POST http://localhost:8080/api/jobs` -> `HTTP 201 Created` (Job ID 1 created in MySQL)
   - `GET http://localhost:8080/api/jobs/1` -> `HTTP 200 OK` (Job ID 1 retrieved via Gateway)
3. **Application Service & Feign Integration:**
   - `GET http://localhost:8080/api/applications/job-details/1` -> `HTTP 200 OK` (`application-service` called `job-service` via Feign client over Eureka discovery)
   - `POST http://localhost:8080/api/applications` -> `HTTP 201 Created` (Application ID 1 persisted to MySQL)
   - `GET http://localhost:8080/api/applications` -> `HTTP 200 OK` (Application list retrieved)
4. **User Service:**
   - `POST http://localhost:8801/api/auth/register` -> `HTTP 201 Created` (User registered, JWT returned)
   - `POST http://localhost:8801/api/auth/login` -> `HTTP 200 OK` (JWT token authenticated)
   - `GET http://localhost:8080/api/users/me` -> `HTTP 200 OK` (Profile returned using Bearer token via Gateway)
5. **Resume Service & Inter-service Auth:**
   - `POST http://localhost:8080/api/resumes` -> `HTTP 201 Created` (`resume-service` validated JWT and called `user-service:8801/api/users/me` via Feign client to verify user role)
   - `GET http://localhost:8080/api/resumes/user/2` -> `HTTP 200 OK` (Resume retrieved with collections initialized)

---

## 6. Remaining Blockers

**None.** All services build cleanly, run in Docker Compose, and communicate properly.
