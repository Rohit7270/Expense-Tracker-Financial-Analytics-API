# Docker and Docker Compose

## 1. Containerization Concepts
* **Docker Image**: Read-only template containing application code, JDK runtime, and dependencies.
* **Docker Container**: Runnable isolated instance of a Docker image.
* **Dockerfile**: Blueprint text file specifying steps to build a Docker image.
* **Docker Compose**: Tool for defining and running multi-container Docker applications (API + PostgreSQL).

---

## 2. Multi-Container Topology

```text
Docker Compose Environment
│
├── Container 1: `expense-tracker-api` (Port 8080)
│     └── Depends on `postgres-db`
│
└── Container 2: `postgres-db` (Port 5432)
      └── Named volume: `postgres_data`
```

---

## 3. Docker Compose Setup & Execution

Configuration file: [`docker-compose.yml`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/docker-compose.yml)

```bash
# Build and start all containers in detached mode
docker-compose up -d --build

# View container status
docker-compose ps

# View application logs
docker-compose logs -f expense-tracker-api

# Stop and remove containers
docker-compose down
```
