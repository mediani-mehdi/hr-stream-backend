# HR Stream - Application Tracking System

An end-to-end ATS (Application Tracking System) built with Spring Boot, PostgreSQL, MinIO/S3, Redis, and AI-powered job description generation.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Docker Deployment](#docker-deployment)
- [API Documentation](#api-documentation)
- [Services](#services)
- [Troubleshooting](#troubleshooting)
- [API Endpoints](#api-endpoints)

---

## ✨ Features

### Core Capabilities

- **User Authentication**
  - Role-based access control (Admin, HR, Candidate)
  - JWT token authentication
  - Password reset with secure tokens
  - Candidate-specific authentication flow

- **Job Management**
  - Full CRUD operations for job postings
  - Job status management (DRAFT, OPEN, CLOSED, ARCHIVED)
  - **AI-Powered Job Description Generation** using Google Gemini API
  - Automatic application link generation with secure tokens
  - Job listing with pagination and filtering

- **Candidate Management**
  - Candidate profile creation and management
  - CV/Resume upload with MinIO/S3 storage
  - Pre-signed URL generation for secure file access
  - Candidate authentication and session management

- **Job Applications**
  - Token-based application submission
  - Application tracking with status updates
  - Application history and management
  - Status workflow (PENDING, UNDER_REVIEW, INTERVIEWING, REJECTED, ACCEPTED)

- **File Storage**
  - MinIO/S3-compatible object storage
  - Secure CV storage with encryption
  - File metadata management (size, content type, SHA256)

### Technology Stack

- **Backend**: Spring Boot 3.5.9, Java 17
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Object Storage**: MinIO (S3-compatible)
- **Monitoring**: Prometheus, Grafana
- **AI**: Google Gemini API
- **Security**: JWT, Spring Security
- **Documentation**: Swagger/OpenAPI

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Client App                          │
│                 (Admin Panel, Recruiter, Candidate)          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                    │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────────┐  │
│  │  Auth        │ │  Jobs        │ │  Candidates          │  │
│  │  Module      │ │  Module      │ │  Module              │  │
│  └──────────────┘ └──────────────┘ └─────────────────────┘  │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────────────────┐  │
│  │ Applications │ │  AI (Gemini) │ │  File Storage        │  │
│  │  Module      │ │              │ │  (MinIO)             │  │
│  └──────────────┘ └──────────────┘ └─────────────────────┘  │
└────────────┬──────────────────────────────────────┬────────┘
             │                                      │
             ▼                                      ▼
    ┌──────────────────┐                  ┌──────────────────┐
    │    PostgreSQL    │                  │     MinIO/S3      │
    │    (PostgreSQL)  │                  │    (File Storage) │
    └──────────────────┘                  └──────────────────┘
             │
             ▼
    ┌──────────────────┐
    │      Redis       │
    │   (Caching)      │
    └──────────────────┘
```

---

## 📦 Prerequisites

- **Docker** (v24.0+)
- **Docker Compose** (v2.20+)
- **Java 17** (if running locally without Docker)
- **Maven** (v3.8+)
- **Google Gemini API Key** [Get one here](https://makersuite.google.com/app/apikey)

---

## ⚙️ Environment Setup

### 1. Copy Environment File

```bash
cp .env.example .env
```

### 2. Configure Environment Variables

Edit `.env` file with your settings:

```env
# PostgreSQL Configuration
DB_NAME=hr_stream
DB_USER=postgres
DB_PASSWORD=your_secure_password_here

# MinIO Configuration
MINIO_ROOT_USER=minio_admin
MINIO_ROOT_PASSWORD=minio_admin_password_123
MINIO_BUCKET_NAME=ats-resumes
MINIO_APP_USER=appuser
MINIO_APP_PASSWORD=appuser_secure_password_123

# Redis Configuration
REDIS_PASSWORD=redis_secure_password_123

# Application Configuration
JWT_SECRET=your_random_secret_key_at_least_32_chars
APP_PORT=8090
SPRING_PROFILE=dev

# Gemini Configuration
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.0-flash

# Grafana
GRAFANA_PASSWORD=admin_secure_password
```

**Important Notes:**

- **JWT_SECRET**: Generate a random string (32+ characters)
- **DB_PASSWORD**: Must be secure, different from MinIO and Redis passwords
- **GEMINI_API_KEY**: Required for AI job description generation
- **All passwords**: Should be different and secure

### 3. Verify Configuration

```bash
docker compose config
```

This will render the final configuration and show any errors.

---

## 🐳 Docker Deployment

### Quick Start

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down

# Stop and remove volumes (resets database)
docker compose down -v
```

### Service Details

#### 1. PostgreSQL

- **Container**: `ats_postgres`
- **Port**: 5432
- **Database**: `hr_stream`
- **User**: `postgres`
- **Volume**: `postgres_data`
- **Health Check**: Auto-restarts if unhealthy

**Access Database:**
```bash
docker compose exec postgres psql -U postgres -d hr_stream
```

#### 2. MinIO (Object Storage)

- **Container**: `ats_minio`
- **API Port**: 9000
- **Console Port**: 9001
- **Default User**: `admin`
- **Default Password**: `admin123`
- **Bucket**: `ats-resumes`
- **Volume**: `minio_data`

**Access MinIO Console:**
- URL: `http://localhost:9001`
- Login with `admin` / `admin123`

**Bucket Permissions:**
- **Public Access**: None (bucket is private)
- **Application User**: `appuser` with policy for `ats-resumes` bucket only

#### 3. Redis

- **Container**: `ats_redis`
- **Port**: 6379
- **Password**: `redis123`
- **Volume**: `redis_data`
- **Mode**: AOF persistence enabled

**Test Redis Connection:**
```bash
docker compose exec redis redis-cli -a redis123 ping
```

#### 4. Application

- **Container**: `hr_stream_app`
- **Port**: 8090
- **Build**: Maven
- **Auto-restart**: Enabled
- **Volume**: `./logs:/app/logs` (for application logs)

**Application Environment:**
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/hr_stream
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=123654
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=appuser
MINIO_SECRET_KEY=appuser123
MINIO_BUCKET_NAME=ats-resumes
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=redis123
SERVER_PORT=8090
SPRING_PROFILES_ACTIVE=docker
SPRING_FLYWAY_ENABLED=false
GEMINI_API_KEY=<from .env>
```

#### 5. MinIO Setup Container

- **Container**: `ats_minio_setup`
- **Purpose**: Creates bucket, sets up users and policies
- **Runs Once**: On first startup
- **Automation**: Automatically sets up `ats-resumes` bucket with private policy and creates `appuser`

#### 6. Prometheus

- **Container**: `prometheus`
- **Port**: 9090
- **Configuration**: `prometheus.yml`

**Access Prometheus:**
- URL: `http://localhost:9090`

#### 7. Grafana

- **Container**: `grafana`
- **Port**: 3000
- **Default User**: `admin`
- **Default Password**: `admin` (first login required to change)

**Access Grafana:**
- URL: `http://localhost:3000`
- Login: `admin` / `admin`

**Import Dashboard:**
1. Go to Dashboards → Import
2. Enter dashboard ID: 1860 (or import from Prometheus exporter)

---

## 📚 API Documentation

Once the application is running, access Swagger UI:

- **URL**: `http://localhost:8090/swagger-ui.html`
- **API Specification**: `http://localhost:8090/v3/api-docs`
- **OpenAPI JSON**: `http://localhost:8090/v3/api-docs/swagger-config`

### Authentication

Most endpoints require JWT authentication. Get a token:

**1. Login as Admin/HR:**
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "admin@hrstream.local",
  "password": "Admin#1234"
}
```

**2. Login as Candidate:**
```bash
POST /candidate/auth/login
Content-Type: application/json

{
  "email": "candidate@example.com",
  "password": "Candidate#1234"
}
```

**3. Register as Candidate:**
```bash
POST /candidate/auth/register
Content-Type: application/json

{
  "email": "newcandidate@example.com",
  "password": "Candidate#1234",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "niveauEtude": "Master",
  "domaineExpertise": "Software Engineering",
  "experienceProfessionnelle": "5 years"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "candidate@example.com",
  "role": "CANDIDATE"
}
```

**Use the token in headers:**
```
Authorization: Bearer <your-jwt-token>
```

---

## 🔧 Services

### Application Services

#### Admin/HR Services (`/auth`)

- `POST /auth/login` - Login
- `POST /auth/register` - Register
- `POST /auth/forgot-password` - Request password reset
- `POST /auth/reset-password` - Reset password with token

#### Job Management (`/jobs`)

- `POST /jobs/save` - Create/Save job
- `GET /jobs/{jobId}` - Get job by ID
- `PUT /jobs/{jobId}` - Update job
- `DELETE /jobs/{jobId}` - Delete job (soft delete)
- `PATCH /jobs/{jobId}/status` - Update job status
- `POST /jobs/{jobId}/publish` - Publish job
- `GET /jobs` - List jobs (paginated)
- `POST /jobs/generate-description` - Generate job description with AI

#### Candidate Management (`/candidates`)

- `GET /candidates` - List candidates (paginated)
- `GET /candidates/{id}` - Get candidate by ID
- `POST /candidates` - Create candidate
- `PUT /candidates/{id}` - Update candidate
- `DELETE /candidates/{id}` - Delete candidate

**CV/Resume Operations:**
- `POST /candidates/{candidateId}/cv` - Upload CV
- `GET /candidates/{candidateId}/cv` - Get CV presigned URL
- `DELETE /candidates/{candidateId}/cv` - Delete CV

**Job Application:**
- `POST /candidates/apply/{token}` - Apply to job (JSON only)
- `POST /candidates/apply/{token}/with-resume` - Apply with resume (multipart)

#### Job Applications (`/applications`)

- `GET /applications` - List applications (paginated)
- `GET /applications/{id}` - Get application by ID
- `DELETE /applications/{id}` - Delete application
- `PATCH /applications/{id}/status` - Update application status

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Database Not Created

**Problem**: Tables don't exist after starting containers

**Solution**: Flyway migrations are disabled in Docker. You need to initialize the database:

```bash
# Run the init script
docker compose exec postgres psql -U postgres -d hr_stream -f /docker-entrypoint-initdb.d/01-init.sql

# Or reset volumes entirely
docker compose down -v
docker compose up -d
```

#### 2. MinIO Bucket Not Found

**Problem**: `ats-resumes` bucket doesn't exist

**Solution**: The `minio-setup` container should run automatically. Check logs:

```bash
docker compose logs minio-setup
```

If it failed, run it again:

```bash
docker compose run --rm minio-setup
```

#### 3. Flyway Migrations Not Running

**Problem**: Application starts but tables are missing

**Solution**: Enable Flyway in Docker:

```bash
docker compose stop app
docker compose run --rm app mvn flyway:migrate
docker compose start app
```

#### 4. Connection Issues

**Problem**: Services can't connect

**Solution**: Check network connectivity:

```bash
docker compose ps
docker compose network ls
```

Test connectivity:

```bash
docker compose exec app ping postgres
docker compose exec app ping minio
docker compose exec app ping redis
```

#### 5. JWT Token Invalid

**Problem**: 401 Unauthorized on protected endpoints

**Solution**:
- Regenerate a new token by logging in again
- Check JWT expiration time (default: 24 hours)
- Verify JWT_SECRET is the same across instances

#### 6. MinIO Credentials Wrong

**Problem**: 403 Forbidden on file upload

**Solution**:
- Verify `MINIO_ACCESS_KEY` and `MINIO_SECRET_KEY` match the appuser created by MinIO setup
- Check MinIO console: http://localhost:9001 → Access Keys → appuser

#### 7. Gemini API Key Not Working

**Problem**: AI description generation fails

**Solution**:
- Verify API key is valid and has credits
- Check API quotas and rate limits
- Ensure `GEMINI_API_KEY` is set in `.env` and mounted correctly

#### 8. Port Already in Use

**Problem**: Error starting containers

**Solution**: Change ports in `docker-compose.yml`:

```yaml
services:
  app:
    ports:
      - "8091:8090"  # Change 8090 to an available port
```

### Debug Mode

Enable debug logging:

```bash
docker compose logs -f --tail=100 app
```

Check application properties:

```bash
docker compose exec app env | grep SPRING_
```

View database logs:

```bash
docker compose logs -f postgres
```

View MinIO logs:

```bash
docker compose logs -f minio
```

View Redis logs:

```bash
docker compose logs -f redis
```

### Health Checks

Check service health:

```bash
docker compose ps
```

Or access Actuator endpoints:

- `http://localhost:8090/actuator/health`
- `http://localhost:8090/actuator/info`
- `http://localhost:8090/actuator/prometheus`

---

## 🔄 Maintenance

### Backup Database

```bash
docker compose exec postgres pg_dump -U postgres hr_stream > backup.sql
```

### Restore Database

```bash
docker compose exec -T postgres psql -U postgres hr_stream < backup.sql
```

### Backup MinIO Data

```bash
docker compose run --rm minio mc mirror /data /backup
```

### Clear Redis Cache

```bash
docker compose exec redis redis-cli -a redis123 FLUSHDB
```

### Check Disk Usage

```bash
docker system df
```

---

## 📊 Monitoring

### Prometheus Metrics

Access at: `http://localhost:9090`

Key metrics:
- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_threads_live_threads` - Active threads
- `http_server_requests_seconds_count` - Request count
- `http_server_requests_seconds_sum` - Request duration

### Grafana Dashboards

Access at: `http://localhost:3000`

- Default admin credentials: `admin` / `admin`
- Change password on first login
- Import dashboard ID: `1860` (Prometheus JVM 8.0)

---

## 🎯 Default Credentials

| Service | Username | Password | URL |
|---------|----------|----------|-----|
| MinIO Console | `admin` | `admin123` | http://localhost:9001 |
| Grafana | `admin` | `admin` | http://localhost:3000 |
| PostgreSQL | `postgres` | `123654` | localhost:5432 |
| Redis | - | `redis123` | localhost:6379 |
| Application Admin | `admin@hrstream.local` | `Admin#1234` | See Swagger UI |

**⚠️ WARNING**: Change default passwords immediately in production!

---

## 📝 Development

### Run Locally (Without Docker)

```bash
# Build the application
mvn clean package

# Run with local Postgres
mvn spring-boot:run

# Run with tests
mvn test
```

### Database Migrations

Create a new migration file in `src/main/resources/db/migration/`:

```bash
# V{version}__{description}.sql
# Example: V2__create_candidates_table.sql
```

Flyway will automatically run migrations on startup.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is proprietary software. All rights reserved.

---

## 🆘 Support

For issues and questions:
- Open an issue on GitHub
- Check troubleshooting section above
- Review application logs: `docker compose logs -f app`

---

## 🔄 Version History

### Version 1.0.0 (Current)
- ✅ JWT-based authentication (Admin, HR, Candidate)
- ✅ Job management with AI description generation
- ✅ Candidate profiles and CV upload
- ✅ Job application system
- ✅ MinIO/S3 integration
- ✅ PostgreSQL + Redis
- ✅ Monitoring with Prometheus + Grafana
- ✅ Swagger API documentation

---

**Happy Recruiting! 🚀**
