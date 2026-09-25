# RevHire Microservices – Infrastructure & Developer Context

This document provides developers with the architectural context, port assignments, infrastructure integration rules, and operational guidelines needed to build, configure, and run microservices in the **RevHire** platform.

---

## 1. System Architecture & Topology

```
                         [ Frontend (Angular: 4200) ]
                                      │
                                      ▼
                        [ API Gateway (Port: 8080) ]
                                      │
            ┌─────────────────────────┴────────────────────────┐
            ▼                                                  ▼
[ Eureka Service Discovery ]                      [ Spring Cloud Config Server ]
       (Port: 8761)                                       (Port: 8800)
            │                                                  │
            ├───────────────────────┬──────────────────────────┼───────────────────────┐
            ▼                       ▼                          ▼                       ▼
    [ User Service ]        [ Resume Service ]          [ Job Service ]     [ Application Service ]
      (Port: 8801)            (Port: 8802)               (Port: 8803)            (Port: 8804)
                                                                                       │
                                                                                       ▼
                                                                            [ Notification Service ]
                                                                                  (Port: 8805)
```

---

## 2. Port Assignment Master Reference

To eliminate port contention across developer workstations, Docker, and CI/CD tools, all services strictly follow this port convention:

| Category | Service / Tool | Host Port | Container Port | Protocol / Path | Purpose |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Infrastructure** | **API Gateway** | `8080` | `8080` | HTTP | Single entry point for all frontend/client requests |
| **Infrastructure** | **Eureka Server** | `8761` | `8761` | HTTP (`/eureka`) | Dynamic service registry and health tracking |
| **Infrastructure** | **Config Server** | `8800` | `8800` | HTTP | Centralized external configuration repository |
| **Business Service**| **User Service** | `8801` | `8801` | HTTP | User accounts, authentication & profiles |
| **Business Service**| **Resume Service** | `8802` | `8802` | HTTP | Candidate resumes, skills & parse data |
| **Business Service**| **Job Service** | `8803` | `8803` | HTTP | Employer job listings, categories & searches |
| **Business Service**| **Application Service**| `8804`| `8804` | HTTP | Job applications, candidate submissions & workflow |
| **Business Service**| **Notification Service**| `8805`| `8805` | HTTP | Alerts, emails, and event notifications |
| **DevOps / CI/CD** | **Jenkins** | `8888` | `8080` | HTTP | Automated CI/CD pipeline automation |
| **Code Quality** | **SonarQube** | `9191` | `9000` | HTTP | Static code analysis & Quality Gates |
| **Database** | **MySQL** | `3333` | `3306` | TCP | Relational database engine |

---

## 3. Infrastructure Component Guides

### A. Config Server (`Port: 8800`)
The Config Server serves externalized property files located under the `config/` directory.

* **Endpoints:**
  * Pattern: `http://localhost:8800/{service-name}/{profile}`
  * Example: `http://localhost:8800/user-service/local`
* **Configuration Storage:**
  * Common configs: `config/{profile}/application.properties`
  * Service-specific configs: `config/{profile}/{service-name}.properties`
* **How Microservices Must Import Config:**
  In each microservice's `src/main/resources/application.properties`, define:
  ```properties
  spring.application.name=<service-name>
  spring.config.import=optional:configserver:${CONFIG_SERVER_URL:http://config-server:8800}
  ```
  > **Note:** Always include `optional:` so local unit tests can run offline without requiring the Config Server container to be active.

---

### B. Service Discovery / Eureka (`Port: 8761`)
All microservices dynamically register with Eureka so the API Gateway and OpenFeign clients can discover them without hardcoded IP addresses.

* **Eureka Dashboard:** `http://localhost:8761/`
* **Eureka API:** `http://localhost:8761/eureka/apps`
* **Microservice Eureka Configuration:**
  ```properties
  eureka.client.service-url.defaultZone=${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
  eureka.instance.prefer-ip-address=true
  ```
  > **Crucial:** Always set `eureka.instance.prefer-ip-address=true` so services register their IP rather than container hostnames that may not resolve across container boundaries.

---

### C. API Gateway (`Port: 8080`)
All external traffic reaches business microservices through the Gateway. Direct client-to-service calls are discouraged.

* **Explicit Routing Table:**
  | Route Predicate | Destination Service | Eureka Service URI |
  | :--- | :--- | :--- |
  | `/api/users/**` | User Service | `lb://user-service` |
  | `/api/resumes/**` | Resume Service | `lb://resume-service` |
  | `/api/jobs/**` | Job Service | `lb://job-service` |
  | `/api/applications/**` | Application Service | `lb://application-service` |
  | `/api/notifications/**`| Notification Service | `lb://notification-service` |

* **Path Preservation:**
  The prefix `/api/<domain>` is **preserved** by default when forwarded to downstream services. Downstream controllers must mount their endpoints under the matching `/api/<domain>` prefix.
  * *Example:* A call to `http://localhost:8080/api/users/profile` forwards directly to `http://user-service:8801/api/users/profile`.

---

## 4. Docker & Startup Sequencing

When launching via Docker Compose, services must follow a strict healthcheck-driven dependency chain:

$$\text{Eureka Server (8761)} \xrightarrow{\text{healthy}} \text{Config Server (8800)} \xrightarrow{\text{healthy}} \text{API Gateway (8080) / Business Services}$$

### Docker Network
* Network Name: `revhire-network` (bridge)
* Docker Hostnames & Aliases:
  * Eureka: `eureka-server` (alias: `eurekaserver`)
  * Config Server: `config-server` (alias: `configserver`)
  * Gateway: `api-gateway` (alias: `apigateway`)

### Health Check Commands
Minimal Alpine images lack `curl`. Container health checks must use `wget`:
* Eureka: `wget -q --spider http://localhost:8761/ || exit 1`
* Config Server: `wget -q --spider http://localhost:8800/user-service/local || exit 1`

---

## 5. Developer Rules & Common Pitfalls

1. **Avoid Hostname Mismatches:**
   Always use `http://config-server:8800` (hyphenated). Do not switch between `configserver` and `config-server` without ensuring both are declared as aliases.
2. **Do Not Rely Solely on Docker's `depends_on`:**
   Docker's `depends_on` only verifies that the container has started, not that Spring Boot has booted and opened its port. Always use health checks and `condition: service_healthy`.
3. **Matching Service Names in Eureka and Gateway:**
   The `spring.application.name` declared in your service (e.g., `user-service`) must strictly match the `lb://<service-name>` declared in API Gateway routes.
4. **Local JPA Scaffolding:**
   If a service includes JPA dependencies (`spring-boot-starter-data-jpa`) but does not yet have a running database or datasource properties configured, exclude the auto-configuration temporarily during bootstrap testing:
   ```properties
   spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
   ```
5. **Quality & SonarQube:**
   Before pushing or integrating, ensure your code passes local unit tests and SonarQube analysis:
   ```bash
   mvn clean test
   mvn sonar:sonar -Dsonar.host.url=http://localhost:9191
   ```
   Quality gate requires zero bugs, zero security vulnerabilities, and adherence to clean architecture principles.
