# RevHire Resume Service

## Owner
- **Primary Owner:** Anant

## Target Responsibilities
The Resume Service manages job seeker resumes and qualifications:
- Interactive Resume Builder
- Full Resume CRUD Lifecycle
- PDF and DOCX Document Uploads
- Resume Parsing and Keyword Extraction
- Resume Retrieval and Viewing for Employers

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
- `client/`: OpenFeign clients for inter-service communication (e.g., User Service)
- `security/`: Service-level token validation and security utilities
- `util/`: Document processing and file-handling utilities

## Structural Initialization Notice
No resume parsing, document storage, or business logic is implemented during this repository initialization phase.
