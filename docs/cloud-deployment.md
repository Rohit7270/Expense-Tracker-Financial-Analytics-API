# Cloud Deployment Guide

## 1. Provider-Neutral Deployment Flow

```text
Source Code (Git)
       ↓
CI/CD Pipeline (GitHub Actions / GitLab CI)
       ↓ [Build & Test: `mvn clean package`]
Docker Image Build (`docker build -t expense-tracker-api:latest .`)
       ↓
Container Registry (AWS ECR / GCP Artifact Registry / Docker Hub)
       ↓
Cloud Container Platform (AWS ECS / AWS App Runner / GCP Cloud Run / Azure Container Apps)
       ↓
Managed Cloud PostgreSQL (AWS RDS PostgreSQL / GCP Cloud SQL)
```

---

## 2. Production Environment Variables Matrix

| Variable Name | Description | Example Production Setting |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC connection URL to Cloud DB | `jdbc:postgresql://rds-instance.aws.com:5432/expense_tracker` |
| `SPRING_DATASOURCE_USERNAME` | Managed DB admin username | `prod_db_user` |
| `SPRING_DATASOURCE_PASSWORD` | Managed DB admin password | `[SecretsManager / Env Vault]` |
| `JWT_SECRET` | 256-bit Base64 signing secret | `[AWS SecretsManager Secret]` |
| `SPRING_AI_ENABLED` | Toggle Spring AI integration | `false` |
