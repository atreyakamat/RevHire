# Kubernetes Base Manifests

This directory contains baseline Kubernetes resource definitions for all services, networking, and platform components.

## Subdirectories
- `namespace/`: Namespace declarations (`revhire-dev`, `revhire-prod`).
- `config/`: Central ConfigMaps across services.
- `secrets/`: Secret templates (actual secret values must NEVER be committed).
- `gateway/`: Deployment, Service, and Ingress manifests for API Gateway.
- `eureka/`: Deployment and Service manifests for Eureka Server.
- `config-server/`: Deployment and Service manifests for Config Server.
- `user-service/`: Deployment and Service manifests for User Service.
- `resume-service/`: Deployment and Service manifests for Resume Service.
- `job-service/`: Deployment and Service manifests for Job Service.
- `application-service/`: Deployment and Service manifests for Application Service.
- `notification-service/`: Deployment and Service manifests for Notification Service.
- `frontend/`: Deployment and Service manifests for Angular Frontend.
