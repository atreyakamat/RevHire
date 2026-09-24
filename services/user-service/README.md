# RevHire User Service

## Owner
- **Primary Owner:** Anant

## Target Responsibilities
The User Service owns user identity and profile domains across RevHire:
- Authentication & Authorization
- JWT Generation, Validation, & Token Refresh
- Role-Based Access Control (RBAC: Job Seeker, Employer, Admin)
- User Registration & Account Management
- Job Seeker Profiles
- Employer Company Profiles

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
- `client/`: External / Feign HTTP clients
- `security/`: JWT filters, password encoders, security config
- `util/`: Service-specific helper utilities

## Structural Initialization Notice
No business logic, database tables, or auth filters are implemented during this repository initialization phase.
