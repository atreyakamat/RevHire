# Externalized Configuration Repository

This directory contains environment-specific configuration files served via Spring Cloud Config Server.

> **CRITICAL SECURITY RULE:**
> Do NOT commit credentials, API keys, private certificates, or database passwords to this directory. Use environment variable substitutions or external secret managers (e.g. HashiCorp Vault, Kubernetes Secrets).
