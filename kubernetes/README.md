# Kubernetes Manifests

This directory contains the Kubernetes orchestration manifests for RevHire Microservices following the Kustomize base/overlay pattern.

## Directory Structure
- `base/`: Common manifests and resource specifications across all environments.
- `dev/`: Development environment overlays, configurations, and replica counts.
- `production/`: Production environment overlays, resource limits, and high-availability specifications.
