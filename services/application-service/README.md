# RevHire Application Service

## Owner
- **Primary Owner:** Shriya

## Target Responsibilities
The Application Service manages job applications and applicant review pipelines:
- Job Application Submission & One-Click Apply
- Candidate Application Status Tracking
- Application Withdrawal Workflow
- Candidate Shortlisting and Rejection
- Bulk Shortlist & Reject Processing
- Employer Review Notes & Candidate Evaluation

## Package Conventions
- `controller/`: REST API Controllers (DTO contracts only)
- `service/`: Domain business logic and transaction boundaries
- `repository/`: Spring Data JPA Repositories
- `entity/`: JPA entities and persistent models
- `dto/request/`: Inbound API request payloads
- `dto/response/`: Outbound API response payloads
- `mapper/`: Entity-to-DTO and DTO-to-Entity transformation mappers
- `exception/`: Domain-specific exceptions and global exception handlers
- `config/`: Spring bean and framework configurations
- `client/`: OpenFeign clients (User Service, Job Service, Notification Service)
- `security/`: Service-level token validation and security utilities
- `util/`: Application workflow helper utilities

## Structural Initialization Notice
No application processing, status state machines, or business logic is implemented during this repository initialization phase.
