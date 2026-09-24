# RevHire Job Service

## Owner
- **Primary Owner:** Shriya

## Target Responsibilities
The Job Service manages job vacancy lifecycle and search operations:
- Job Creation, Modification, & Deletion
- Job Status Management (Draft, Active, Closed)
- Multi-criteria Job Search and Filtering (Location, Skills, Salary, Job Type)
- Paginated Job Listings
- Employer Job Dashboards & Analytics Statistics

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
- `client/`: OpenFeign clients for inter-service communication
- `security/`: Service-level token validation and security utilities
- `util/`: Search and pagination helpers

## Structural Initialization Notice
No job querying, indexing, or business logic is implemented during this repository initialization phase.
