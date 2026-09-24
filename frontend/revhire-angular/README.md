# RevHire Angular Frontend

## Purpose
This directory contains the Angular single-page application (SPA) for RevHire. It serves as the primary user interface for job seekers and employers to interact with the platform.

## Technology Stack
- **Framework:** Angular 18+
- **Language:** TypeScript
- **HTTP Communication:** RESTful API client communicating with backend microservices via the API Gateway (`http://localhost:8080`)
- **Default Development Port:** `4200`

## Planned Directory Layout
```text
revhire-angular/
├── src/
│   ├── app/
│   │   ├── core/              # Singleton services, interceptors, auth guards
│   │   ├── shared/            # Reusable UI components, pipes, directives
│   │   ├── features/          # Domain feature modules (auth, jobs, applications, resumes)
│   │   └── models/            # TypeScript interfaces / DTO contracts
│   ├── assets/                # Static assets, styling, icons
│   └── environments/          # Environment configuration
├── package.json
├── angular.json
└── tsconfig.json
```

## Structural Initialization Notice
The Angular application setup is scheduled for the frontend initialization milestone. No frontend code or dependencies are committed during this architectural initialization phase.
