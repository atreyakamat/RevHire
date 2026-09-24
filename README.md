# RevHire Microservices

## Project Purpose
RevHire Microservices is an enterprise-grade job portal platform connecting job seekers with employers. This project modernizes the legacy monolithic RevHire application into a scalable, resilient, domain-driven microservices architecture.

## Architecture Summary
The system is built on Spring Boot, Spring Cloud, and Java 21, adhering to domain-driven design and microservice architectural patterns:
- **Database-per-service**: Each business microservice owns its schema and datastore independently. Direct cross-database access is prohibited.
- **API Gateway**: Serves as the single unified entry point for all external client requests.
- **Service Discovery**: Eureka Server facilitates dynamic client-side service registration and discovery.
- **Centralized Configuration**: Spring Cloud Config Server provides externalized configuration management across environments.
- **Resilience**: Fault tolerance, circuit breaking, and rate limiting via Resilience4j.
- **Containerization & Orchestration**: Containerized with Docker and orchestrated with Kubernetes.
- **Continuous Integration/Continuous Deployment**: Automated Jenkins pipelines enforcing SonarQube quality gates and SCA vulnerability scanning.

## Service List
The platform consists of five core business microservices:
1. **User Service** (`services/user-service`): Authentication, authorization, RBAC, user accounts, and seeker/employer profiles.
2. **Resume Service** (`services/resume-service`): Resume creation, storage, parsing, and management.
3. **Job Service** (`services/job-service`): Job postings, lifecycle management, searching, and filtering.
4. **Application Service** (`services/application-service`): Job applications, status workflows, and employer review processes.
5. **Notification Service** (`services/notification-service`): In-app alerts, email notifications, and communication dispatch.

## Infrastructure List
Core infrastructure and governance components:
1. **API Gateway** (`infrastructure/api-gateway`): Request routing, centralized authentication filters, rate limiting.
2. **Eureka Server** (`infrastructure/eureka-server`): Service registry and discovery.
3. **Config Server** (`infrastructure/config-server`): Centralized environment configuration management.

## Team Ownership
- **Atreya**:
  - Git repository manager & repository governance
  - API Gateway
  - Eureka Server
  - Config Server
  - Docker & Kubernetes orchestration
  - Jenkins CI/CD pipelines
  - SonarQube & SCA/security scanning
  - Release & integration management
- **Anant**:
  - User Service
  - Resume Service
- **Shriya**:
  - Job Service
  - Application Service
- **Yuvraj**:
  - Notification Service
  - Integration testing & E2E testing coordination

## Branch Strategy
The repository strictly adheres to a trunk-based integration workflow:

```text
main (Protected - Production Releases)
  ^
  | (Release Pull Request after integration & QA)
dev (Protected - Integration Branch)
  ^
  | (Pull Request per feature)
  +-- rh-atreya
  +-- rh-anant
  +-- rh-shriya
  +-- rh-yuvraj
```

- Developers commit and push exclusively to their designated `rh-<name>` developer branch.
- Feature integration into `dev` requires an approved Pull Request following the repository PR template.
- Integration and testing take place on `dev`.
- Deployments to `main` occur via Release Pull Requests from `dev`.
- Generic `develop` or unassigned `feature/*` branches are prohibited.

## Repository Structure
```text
revhire-microservices/
├── services/                     # Business microservices
│   ├── user-service/
│   ├── resume-service/
│   ├── job-service/
│   ├── application-service/
│   └── notification-service/
├── infrastructure/               # Platform infrastructure services
│   ├── api-gateway/
│   ├── eureka-server/
│   └── config-server/
├── frontend/                     # Client web applications
│   └── revhire-angular/
├── kubernetes/                   # Kubernetes deployment manifests
│   ├── base/
│   ├── dev/
│   └── production/
├── docker/                       # Docker compose and utility scripts
│   ├── scripts/
│   └── compose/
├── config/                       # Externalized environment configuration
│   ├── local/
│   ├── dev/
│   └── production/
├── docs/                         # System architecture and specifications
│   ├── architecture/
│   ├── api/
│   ├── development/
│   ├── deployment/
│   └── testing/
├── scripts/                      # Build, test, and deployment helper scripts
│   ├── build/
│   ├── test/
│   └── deployment/
├── .github/                      # GitHub issue and PR templates
│   └── pull_request_template.md
├── .gitignore                    # Git ignore definitions
├── README.md                     # Root project documentation
├── pom.xml                       # Root Maven aggregator & parent POM
├── Jenkinsfile                   # CI/CD pipeline definition
└── docker-compose.yml            # Local development orchestration
```

## Structural Initialization Notice
> **IMPORTANT ARCHITECTURAL RULE:**
> This repository is currently in the **structural initialization phase**. No business logic, mock REST controllers, domain entities, database tables, or dummy implementations are part of this initialization. All modules and packages establish the clean architectural boundaries required for development teams to commence feature implementations independently.
