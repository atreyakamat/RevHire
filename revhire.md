# RevHire Microservices

## Project Documentation

> **Project:** RevHire Microservices
> **Type:** Full-Stack Job Portal / Microservices Application
> **Architecture:** Domain-Driven Microservices
> **Backend:** Java 21, Spring Boot 4, Spring Cloud
> **Frontend:** Angular
> **Database:** MySQL
> **Containerization:** Docker
> **Orchestration:** Kubernetes
> **CI/CD:** Jenkins
> **Build Tool:** Maven
> **Version Control:** Git

---

## 1. Project Overview

**RevHire** is a full-stack job portal that connects **job seekers** with **employers**. The platform allows job seekers to create professional profiles, build and upload resumes, search for jobs, apply for positions, track applications, save favorite jobs, and receive notifications.

Employers can create and manage job postings, search and review applicants, evaluate resumes and cover letters, shortlist or reject candidates in bulk, and monitor recruitment statistics through dashboards.

The original RevHire application was implemented as a **monolithic Spring Boot application with MySQL**. This project focuses on modernizing that application into a **secure, scalable, maintainable, and production-ready microservices architecture**.

The application is divided into independent business domains, with each domain represented by its own microservice.

---

# 2. Objectives

The primary objectives of the RevHire Microservices project are:

* Transform the existing monolithic application into microservices.
* Identify appropriate business domains and bounded contexts.
* Build independently deployable services.
* Implement secure authentication and role-based authorization.
* Provide reliable communication between microservices.
* Introduce service discovery and centralized configuration.
* Implement API Gateway-based request routing.
* Add resilience and fault-tolerance mechanisms.
* Containerize services using Docker.
* Orchestrate containers using Kubernetes.
* Establish CI/CD pipelines using Jenkins.
* Maintain independent database schemas for each service.
* Improve scalability, maintainability, and deployment flexibility.

---

# 3. Existing Monolithic Application

The original RevHire application contains the major job portal functionalities in a single application.

### Existing Architecture

```text
                 +----------------------+
                 |      Angular UI      |
                 +----------+-----------+
                            |
                            v
                 +----------------------+
                 |   Spring Boot App    |
                 |                      |
                 | Authentication       |
                 | User Management      |
                 | Resume Management    |
                 | Job Management       |
                 | Applications         |
                 | Notifications        |
                 +----------+-----------+
                            |
                            v
                 +----------------------+
                 |        MySQL         |
                 +----------------------+
```

### Problems with the Monolith

The monolithic architecture can introduce several challenges as the application grows:

* Tight coupling between business modules.
* Difficult independent deployment.
* Scaling the entire application even when only one feature requires additional capacity.
* Changes in one module can affect other modules.
* Larger deployment units.
* Difficult ownership and maintenance of individual business domains.
* Limited fault isolation.
* Increasing complexity as more features are added.

---

# 4. Proposed Microservices Architecture

The RevHire application is divided into the following core microservices:

1. **User Service**
2. **Resume Service**
3. **Job Service**
4. **Application Service**
5. **Notification Service**

Infrastructure components are used to manage communication, configuration, service discovery, routing, and resilience.

```text
                         +----------------+
                         |    Angular     |
                         |   Frontend     |
                         +-------+--------+
                                 |
                                 v
                         +----------------+
                         |   API Gateway  |
                         +-------+--------+
                                 |
             +-------------------+-------------------+
             |          |          |          |       |
             v          v          v          v       v
       +---------+ +---------+ +---------+ +---------+ +--------------+
       |  User   | | Resume  | |   Job   | |Application| | Notification |
       | Service | | Service | | Service | |  Service  | |   Service    |
       +----+----+ +----+----+ +----+----+ +-----+-----+ +------+-------+
            |           |           |            |              |
            v           v           v            v              v
       +---------+ +---------+ +---------+ +-----------+ +-------------+
       | User DB | |Resume DB| |  Job DB | |Application| |Notification |
       |         | |         | |         | |    DB     | |     DB      |
       +---------+ +---------+ +---------+ +-----------+ +-------------+

                         Infrastructure
              +--------------------------------------+
              | Eureka | Config Server | Kubernetes |
              | Docker | Jenkins | Resilience4j      |
              +--------------------------------------+
```

---

# 5. Microservices

## 5.1 User Service

The **User Service** manages user identity and account-related functionality.

### Responsibilities

* User registration.
* Login/authentication.
* Role-based access control.
* User profile management.
* Job seeker profile management.
* Employer profile management.
* Account management.
* Authorization-related functionality.

### User Roles

```text
USER
 |
 +-- JOB_SEEKER
 |
 +-- EMPLOYER
 |
 +-- ADMIN
```

### Example Operations

```http
POST   /users/register
POST   /users/login
GET    /users/{id}
PUT    /users/{id}
DELETE /users/{id}
```

---

# 6. Resume Service

The **Resume Service** manages job seeker resumes and resume-related documents.

### Responsibilities

* Resume creation.
* Textual resume builder.
* Resume updates.
* PDF upload.
* DOCX upload.
* Resume parsing.
* Resume retrieval.
* Resume management.

### Example Operations

```http
POST   /resumes
GET    /resumes/{id}
PUT    /resumes/{id}
DELETE /resumes/{id}

POST   /resumes/upload
GET    /resumes/{id}/download
```

### Resume Flow

```text
Job Seeker
    |
    v
Angular UI
    |
    v
API Gateway
    |
    v
Resume Service
    |
    +----> Validate File
    |
    +----> Store/Process Resume
    |
    +----> Parse Resume
    |
    v
Resume Database
```

---

# 7. Job Service

The **Job Service** manages job postings and job search functionality.

### Responsibilities

* Create job postings.
* Update job postings.
* Delete/manage job postings.
* Publish jobs.
* Search jobs.
* Advanced job filtering.
* Employer job management.
* Employer statistics.

### Job Search Filters

The system can support filters such as:

* Job title.
* Skills.
* Location.
* Experience.
* Employment type.
* Salary range.
* Job category.
* Company.
* Posting status.

### Example APIs

```http
POST   /jobs
GET    /jobs
GET    /jobs/{id}
PUT    /jobs/{id}
DELETE /jobs/{id}

GET    /jobs/search
GET    /jobs/employer/{employerId}
```

---

# 8. Application Service

The **Application Service** manages the complete job application lifecycle.

### Responsibilities

* One-click job application.
* Application creation.
* Application status tracking.
* Application withdrawal.
* Employer applicant management.
* Candidate shortlisting.
* Candidate rejection.
* Bulk shortlist/reject operations.
* Internal employer notes.

### Application Status

A typical application lifecycle can be represented as:

```text
APPLIED
   |
   v
UNDER_REVIEW
   |
   +---------> REJECTED
   |
   v
SHORTLISTED
   |
   v
INTERVIEW
   |
   v
SELECTED
```

The exact status flow can be configured according to business requirements.

### Example APIs

```http
POST   /applications
GET    /applications/{id}
GET    /applications/seeker/{seekerId}
GET    /applications/job/{jobId}

PUT    /applications/{id}/status
POST   /applications/{id}/withdraw

POST   /applications/bulk/shortlist
POST   /applications/bulk/reject
```

---

# 9. Notification Service

The **Notification Service** handles user notifications.

### Responsibilities

* In-app notifications.
* Email notifications.
* Application status notifications.
* Job recommendation notifications.
* Notification management.
* Notification status tracking.

### Notification Examples

A job seeker can receive notifications when:

* An application is submitted.
* An application status changes.
* An employer shortlists the candidate.
* An employer rejects an application.
* A relevant job recommendation becomes available.

### Notification Flow

```text
Application Service
        |
        | Application Status Changed
        v
Notification Service
        |
        +---------> In-App Notification
        |
        +---------> Email Notification
```

---

# 10. Bounded Contexts

The system follows domain-driven design principles by dividing the application into bounded contexts.

| Bounded Context            | Microservice         |
| -------------------------- | -------------------- |
| Identity & Accounts        | User Service         |
| Resume Management          | Resume Service       |
| Jobs & Search              | Job Service          |
| Recruitment & Applications | Application Service  |
| Notifications              | Notification Service |

Each service owns its business logic and data.

---

# 11. Database Architecture

A key principle of the architecture is **database-per-service**.

```text
User Service
     |
     v
User Database

Resume Service
     |
     v
Resume Database

Job Service
     |
     v
Job Database

Application Service
     |
     v
Application Database

Notification Service
     |
     v
Notification Database
```

Each microservice should primarily access its own database rather than directly accessing another service's database.

### Benefits

* Data ownership is clearly defined.
* Services remain loosely coupled.
* Individual databases can be scaled independently.
* Database changes can be isolated.
* Services can evolve independently.

---

# 12. Inter-Service Communication

Microservices need to communicate with each other to complete business operations.

The project uses **OpenFeign clients** for service-to-service communication.

### Example

When an application is submitted:

```text
Application Service
       |
       | OpenFeign
       v
User Service
       |
       v
Validate Job Seeker

Application Service
       |
       | OpenFeign
       v
Job Service
       |
       v
Validate Job
```

After successful processing:

```text
Application Service
       |
       v
Notification Service
       |
       v
Send Notification
```

---

# 13. Service Discovery

The system uses **Eureka Server** for service discovery.

Instead of hardcoding service URLs, microservices register themselves with Eureka.

```text
             +----------------+
             | Eureka Server  |
             +-------+--------+
                     |
       +-------------+-------------+
       |             |             |
       v             v             v
 User Service   Job Service   Application Service
```

### Service Registration

Each service registers with Eureka when it starts.

The service can then discover other services using their registered service names.

Example:

```text
USER-SERVICE
JOB-SERVICE
RESUME-SERVICE
APPLICATION-SERVICE
NOTIFICATION-SERVICE
```

---

# 14. API Gateway

The **API Gateway** provides a single entry point for frontend clients.

```text
Angular
   |
   v
API Gateway
   |
   +----> User Service
   |
   +----> Resume Service
   |
   +----> Job Service
   |
   +----> Application Service
   |
   +----> Notification Service
```

### API Gateway Responsibilities

* Request routing.
* Authentication integration.
* Rate limiting.
* Request filtering.
* Cross-cutting concerns.
* Service discovery integration.
* Resilience handling.

Example routes:

```text
/api/users/**          -> USER-SERVICE
/api/resumes/**        -> RESUME-SERVICE
/api/jobs/**           -> JOB-SERVICE
/api/applications/**   -> APPLICATION-SERVICE
/api/notifications/**  -> NOTIFICATION-SERVICE
```

---

# 15. Resilience and Fault Tolerance

The project uses **Resilience4j** for handling failures between services.

Important resilience patterns include:

* Circuit breaker.
* Retry.
* Rate limiter.
* Time limiter.
* Fallback handling.

### Circuit Breaker Example

```text
Application Service
       |
       v
Notification Service
       |
       X
    Failure
       |
       v
Circuit Breaker
       |
       v
Fallback Response
```

The circuit breaker helps prevent repeated requests to an unhealthy downstream service.

---

# 16. Security

Security is an important part of the RevHire platform.

### Security Requirements

* User authentication.
* Role-based access control.
* Secure API access.
* Protected employer operations.
* Protected job seeker operations.
* Validation of incoming requests.
* Secure document handling.
* Authorization at service/API level.

### Role-Based Access

| Functionality       | Job Seeker |  Employer  | Admin |
| ------------------- | :--------: | :--------: | :---: |
| Register/Login      |     Yes    |     Yes    |  Yes  |
| Manage Own Profile  |     Yes    |     Yes    |  Yes  |
| Create Job          |     No     |     Yes    |  Yes  |
| Search Jobs         |     Yes    |     Yes    |  Yes  |
| Apply for Job       |     Yes    |     No     |   No  |
| Manage Applications |     Own    | Candidates |  Yes  |
| Upload Resume       |     Yes    |     No     |  Yes  |
| Manage Users        |     No     |     No     |  Yes  |

---

# 17. Angular Frontend

The frontend is developed using **Angular**.

### Job Seeker Features

```text
Registration/Login
       |
       v
Job Seeker Dashboard
       |
       +-- Profile
       +-- Resume
       +-- Search Jobs
       +-- Job Details
       +-- Apply
       +-- Saved Jobs
       +-- Applications
       +-- Notifications
```

### Employer Features

```text
Registration/Login
       |
       v
Employer Dashboard
       |
       +-- Company Profile
       +-- Post Job
       +-- Manage Jobs
       +-- Applicants
       +-- Shortlist Candidates
       +-- Reject Candidates
       +-- Statistics
       +-- Notifications
```

---

# 18. Docker Containerization

Each application component can be packaged as an independent Docker image.

Example:

```text
revhire-user-service
revhire-resume-service
revhire-job-service
revhire-application-service
revhire-notification-service
revhire-api-gateway
revhire-eureka-server
revhire-config-server
revhire-frontend
```

### Example Docker Architecture

```text
                    Docker Environment
                           |
        +------------------+------------------+
        |                  |                  |
        v                  v                  v
   API Gateway        User Service       Job Service
        |                  |                  |
        v                  v                  v
   Application        User DB             Job DB
   Service
        |
        v
 Application DB
```

---

# 19. Kubernetes Deployment

Kubernetes is used to orchestrate the containerized services.

### Kubernetes Responsibilities

* Container orchestration.
* Service discovery within the cluster.
* Scaling.
* Self-healing.
* Rolling deployments.
* Load balancing.
* Configuration management.
* Secret management.

### Conceptual Kubernetes Architecture

```text
                    Kubernetes Cluster
                           |
                    +------+------+
                    |             |
                API Gateway    Frontend
                    |
        +-----------+-----------+
        |           |           |
        v           v           v
      User        Resume       Job
     Service      Service     Service
        |           |           |
        +-----------+-----------+
                    |
              Application
                 Service
                    |
              Notification
                 Service
```

---

# 20. CI/CD Pipeline

Jenkins is used to automate the build and deployment process.

### CI/CD Flow

```text
Developer
    |
    v
Git Repository
    |
    v
Jenkins
    |
    +----> Checkout Code
    |
    +----> Maven Build
    |
    +----> Run Tests
    |
    +----> Build Docker Image
    |
    +----> Push Image to Docker Hub
    |
    +----> Deploy to Kubernetes
    |
    v
Running Application
```

### Pipeline Stages

```text
Checkout
   ↓
Compile
   ↓
Unit Tests
   ↓
Package
   ↓
Docker Build
   ↓
Docker Push
   ↓
Kubernetes Deployment
```

---

# 21. Git Workflow

Git is used for source-code management.

A possible branching strategy is:

```text
main
 |
 +---- develop
        |
        +---- feature/user-service
        |
        +---- feature/job-service
        |
        +---- feature/resume-service
        |
        +---- feature/application-service
        |
        +---- feature/notification-service
```

Feature branches can be merged into the development branch after implementation and testing.

---

# 22. Suggested Project Structure

A possible repository structure is:

```text
revhire-microservices/
│
├── frontend/
│   └── revhire-angular/
│
├── services/
│   │
│   ├── user-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   │
│   ├── resume-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   │
│   ├── job-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   │
│   ├── application-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   │
│   └── notification-service/
│       ├── src/
│       ├── pom.xml
│       └── Dockerfile
│
├── infrastructure/
│   ├── api-gateway/
│   ├── eureka-server/
│   └── config-server/
│
├── kubernetes/
│   ├── user-service.yaml
│   ├── resume-service.yaml
│   ├── job-service.yaml
│   ├── application-service.yaml
│   ├── notification-service.yaml
│   └── gateway.yaml
│
├── Jenkinsfile
├── docker-compose.yml
└── README.md
```

---

# 23. Typical Application Flow

## Job Seeker Registration

```text
Job Seeker
    |
    v
Angular
    |
    v
API Gateway
    |
    v
User Service
    |
    v
User Database
```

---

## Job Posting

```text
Employer
    |
    v
Angular
    |
    v
API Gateway
    |
    v
Job Service
    |
    v
Job Database
```

---

## Applying for a Job

```text
Job Seeker
    |
    v
Angular
    |
    v
API Gateway
    |
    v
Application Service
    |
    +----> User Service
    |
    +----> Job Service
    |
    v
Application Database
    |
    v
Notification Service
    |
    +----> In-App Notification
    |
    +----> Email Notification
```

---

# 24. Employer Applicant Management

An employer can review candidates who have applied to a job.

```text
Employer
   |
   v
Application Service
   |
   +----> Retrieve Applications
   |
   +----> Retrieve Candidate Information
   |
   +----> Retrieve Resume
   |
   v
Applicant List
   |
   +----> Shortlist
   |
   +----> Reject
   |
   +----> Add Internal Note
```

Bulk operations can be supported for handling multiple applicants simultaneously.

---

# 25. Job Search

The job seeker can search for jobs using multiple filters.

```text
Job Seeker
    |
    v
Search Criteria
    |
    +-- Keyword
    +-- Location
    +-- Experience
    +-- Salary
    +-- Employment Type
    +-- Skills
    |
    v
API Gateway
    |
    v
Job Service
    |
    v
Job Database
    |
    v
Filtered Job Results
```

---

# 26. Notification Flow

```text
Business Event
     |
     v
Relevant Microservice
     |
     v
Notification Service
     |
     +----------------+
     |                |
     v                v
In-App             Email
Notification       Notification
```

Example:

```text
Employer Shortlists Candidate
              |
              v
     Application Service
              |
              v
     Notification Service
              |
       +------+------+
       |             |
       v             v
    In-App          Email
```

---

# 27. Production Readiness

The target architecture focuses on production-oriented concepts including:

* Independent service deployment.
* Centralized configuration.
* Service discovery.
* API Gateway.
* Rate limiting.
* Circuit breakers.
* Containerization.
* Kubernetes orchestration.
* CI/CD automation.
* Independent database ownership.
* Authentication and authorization.
* Logging and monitoring readiness.
* Fault isolation.

---

# 28. Technology Stack

| Technology           | Purpose                      |
| -------------------- | ---------------------------- |
| Java 21              | Backend programming language |
| Spring Boot 4        | Microservice development     |
| Spring Cloud         | Cloud-native infrastructure  |
| Spring Cloud Gateway | API Gateway                  |
| Eureka               | Service discovery            |
| Spring Cloud Config  | Centralized configuration    |
| OpenFeign            | Inter-service communication  |
| Resilience4j         | Fault tolerance              |
| Angular              | Frontend                     |
| MySQL                | Relational database          |
| Maven                | Build automation             |
| Docker               | Containerization             |
| Kubernetes           | Container orchestration      |
| Jenkins              | CI/CD                        |
| Git                  | Version control              |
| Docker Hub           | Container image registry     |

---

# 29. Non-Functional Requirements

## Scalability

Services should be independently scalable based on their workload.

For example:

```text
Job Service
    |
    +---- Instance 1
    +---- Instance 2
    +---- Instance 3
```

while another service may continue running with a single instance.

## Availability

Failure of one service should have limited impact on unrelated functionality. Resilience mechanisms such as circuit breakers and fallbacks help manage downstream failures.

## Maintainability

Each service contains a focused business responsibility, making the codebase easier to understand and evolve.

## Security

Authentication, authorization, API protection, and secure communication should be considered throughout the system.

## Performance

API Gateway routing, independent scaling, appropriate database queries, and resilience mechanisms can help maintain predictable application performance.

---

# 30. Advantages of the Proposed Architecture

The RevHire microservices architecture provides:

* Clear separation of business responsibilities.
* Independent service deployment.
* Independent database ownership.
* Better fault isolation.
* Independent scaling.
* Easier maintenance of individual domains.
* Flexible technology and implementation choices per service.
* Automated containerized deployment.
* CI/CD integration.
* Centralized configuration.
* Service discovery.
* Resilience mechanisms.

---

# 31. Future Enhancements

Possible future improvements include:

* Distributed tracing.
* Centralized log aggregation.
* Metrics and monitoring dashboards.
* Event-driven communication using a message broker.
* Advanced job recommendation algorithms.
* Full-text search using a dedicated search engine.
* Resume skill extraction.
* Automated candidate-job matching.
* Object storage for uploaded documents.
* Kubernetes autoscaling.
* Automated API documentation.
* Integration and contract testing.
* Cloud deployment.

---

# 32. Conclusion

**RevHire Microservices** transforms a traditional monolithic job portal into a modular, cloud-native architecture.

The application separates its core business domains into **User, Resume, Job, Application, and Notification services**. Supporting infrastructure such as **API Gateway, Eureka Server, Config Server, OpenFeign, and Resilience4j** enables communication, discovery, configuration, and resilience.

Docker and Kubernetes provide containerization and orchestration, while Jenkins enables an automated CI/CD workflow.

The resulting architecture provides a foundation for independently deployable services and supports the scalability, maintainability, security, and operational requirements of a modern job portal.

---

## 33. Quick Architecture Summary

```text
                         REVHIRE
                            |
                  +---------+---------+
                  |                   |
              Angular             API Gateway
                                      |
              +-----------------------+-----------------------+
              |           |            |          |            |
              v           v            v          v            v
           User       Resume          Job    Application   Notification
          Service     Service       Service     Service       Service
              |           |            |          |            |
              v           v            v          v            v
           User DB     Resume DB     Job DB   Application DB Notification DB
              |           |            |          |            |
              +-----------+------------+----------+------------+
                                      |
                              Service Infrastructure
                                      |
                 +--------------------+--------------------+
                 |                    |                    |
              Eureka            Config Server        Resilience4j
                 |
             Kubernetes
                 |
               Docker
                 |
              Jenkins
                 |
             Docker Hub
```

**RevHire = Job Portal + Domain-Driven Microservices + Spring Cloud + Docker + Kubernetes + CI/CD**
