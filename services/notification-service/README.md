# RevHire Notification Service

## Owner
- **Primary Owner:** Yuvraj

## Target Responsibilities
The Notification Service handles communication and alert delivery across the platform:
- In-App Notification Feed & Unread Badges
- Email Dispatch (Application status changes, interview invites, registrations)
- Job Recommendation & Alert Notifications
- Notification Delivery Tracking

## Package Conventions
- `controller/`: REST API Controllers (DTO contracts only)
- `service/`: Domain business logic and transaction boundaries
- `repository/`: Spring Data JPA Repositories
- `entity/`: JPA entities and persistent models
- `dto/request/`: Inbound API request payloads
- `dto/response/`: Outbound API response payloads
- `mapper/`: Entity-to-DTO and DTO-to-Entity transformation mappers
- `exception/`: Domain-specific exceptions and global exception handlers
- `config/`: Spring bean, JavaMailSender, and framework configurations
- `client/`: External / Feign HTTP clients
- `security/`: Service-level token validation and security utilities
- `util/`: Email templating and string formatting utilities

## Structural Initialization Notice
No email sending, template rendering, or notification dispatch logic is implemented during this repository initialization phase.
