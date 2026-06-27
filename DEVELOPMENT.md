# HR Stream - Developer Documentation

Complete developer guide for contributing to and maintaining the HR Stream Application Tracking System.

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Project Structure](#4-project-structure)
5. [API Reference](#5-api-reference)
6. [Database Schema](#6-database-schema)
7. [Security](#7-security)
8. [Configuration](#8-configuration)
9. [Development Setup](#9-development-setup)
10. [Testing](#10-testing)
11. [Deployment](#11-deployment)
12. [Troubleshooting](#12-troubleshooting)

---

## 1. Project Overview

**HR Stream** is an end-to-end Application Tracking System (ATS) built with Spring Boot that provides:
- User authentication with role-based access control (Admin, HR, Candidate)
- Job posting management with AI-powered description generation
- Candidate profiles with CV/Resume upload
- Job application tracking with scoring pipeline
- Secure file storage via MinIO/S3

**Current Version**: 0.0.1-SNAPSHOT  
**Spring Boot Version**: 3.5.9  
**Java Version**: 17

---

## 2. Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Client Applications                            │
│                 (Admin Panel, Candidate Portal)                      │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  Spring Boot Application (Port 8090)                 │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐  │
│  │   Auth      │ │   Jobs     │ │   Candidates       │  │
│  │   Module   │ │   Module  │ │   Module          │  │
│  └──────────────┘ └──────────────┘ └──────────────────────┘  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐  │
│  │Applications│ │   AI       │ │   File Storage    │  │
│  │   Module  │ │   Gateway  │ │   (MinIO)       │  │
│  └──────────────┘ └──────────────┘ └──────────────────────┘  │
└──────┬──────────────────────────┬───────────────────────────┬───┘
       │                          │                        │
       ▼                          ▼                        ▼
┌──────────────┐            ┌──────────────┐          ┌──────────────┐
│ PostgreSQL  │            │    Redis     │          │   MinIO     │
│  (Port 5432)│            │  (Port 6379) │          │ (Port 9000) │
└──────────────┘            └──────────────┘          └──────────────┘
```

### 2.2 Module Responsibilities

| Module | Package | Responsibility |
|--------|--------|-------------|
| Auth | `auth` | Admin/HR authentication, JWT token management, password reset |
| User | `user` | User entity, Role enum, repository |
| Candidate | `candidate` | Candidate CRUD, CV upload, authentication |
| Job | `job` | Job CRUD, status management, AI generation |
| JobApplication | `jobapplication` | Application lifecycle, scoring pipeline |
| File | `file` | MinIO integration for resume storage |
| Security | `security` | JWT service, Spring Security config |
| Config | `config` | Application configuration beans |
| Gemini | `Gemini` | AI job description generation |
| Health | `health` | Custom health indicators |

---

## 3. Technology Stack

### 3.1 Core Dependencies

| Category | Technology | Version |
|----------|-----------|---------|
| Framework | Spring Boot | 3.5.9 |
| Language | Java | 17 |
| Build Tool | Maven | 3.8+ |
| Database | PostgreSQL | 16+ |
| Cache | Redis | 7 |
| Object Storage | MinIO | Latest |
| Security | Spring Security + JWT | - |
| API Documentation | SpringDoc OpenAPI | 2.8.5 |
| ORM | Spring Data JPA + Hibernate | - |

### 3.2 AI & ML

| Service | Library | Purpose |
|--------|---------|---------|
| Google Gemini | google-genai 1.0.0 | Job description generation |
| Spring AI | 1.1.2 | Multi-provider AI gateway |
| Anthropic Claude | spring-ai-starter-model-anthropic | Alternative AI scorer |
| OpenRouter | - | AI provider aggregation |

### 3.3 Supporting Libraries

| Library | Purpose |
|---------|---------|
| Lombok | Code generation |
| PDFBox 3.0.3 | CV text extraction |
| Resilience4j 2.2.0 | Circuit breakers |
| ShedLock 5.16.0 | Distributed scheduling |
| Testcontainers | Integration testing |
| AWS SDK v2 | S3-compatible storage |

---

## 4. Project Structure

```
hr-stream/
├── src/
│   ├── main/
│   │   ├── java/com/medev/hrstream/
│   │   │   ├── HrStreamApplication.java     # Main entry point
│   │   │   ├── auth/                     # Auth endpoints
│   │   │   ├── candidate/               # Candidate management
│   │   │   ├── config/                 # Configuration
│   │   │   ├── file/                  # MinIO storage
│   │   │   ├── Gemini/                 # AI generation
│   │   │   ├── job/                   # Job management
│   │   │   ├── jobapplication/          # Applications
│   │   │   ├── user/                  # User entity
│   │   │   ├── security/               # Security config
│   │   │   └── common/                # Shared utilities
│   │   └── resources/
│   │       ├── application.yml          # Main config
│   │       ├── application-dev.yml     # Dev overrides
│   │       ├── application-prod.yml    # Prod overrides
│   └── test/
│       └── java/...                    # Unit & integration tests
├── pom.xml
├── docker-compose.infra.yml            # Local infrastructure
├── docker-compose.yml               # Full deployment
├── Dockerfile
└── README.md
```

---

## 5. API Reference

### 5.1 Base URL

```
http://localhost:8090
```

**Note**: No global `/api/v1` prefix - endpoints are mounted directly.

### 5.2 Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|--------------|
| POST | /auth/login | Admin/HR login | No |
| POST | /auth/register | Register new admin | No |
| POST | /auth/forgot-password | Request password reset | No |
| POST | /auth/reset-password | Reset password | No |
| GET | /auth/welcome | Welcome message | No |

### 5.3 Candidate Auth Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|--------------|
| POST | /candidate/auth/register | Register candidate | No |
| POST | /candidate/auth/login | Candidate login | No |

### 5.4 Job Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|--------------|
| GET | /jobs | List jobs (paginated) | Yes |
| POST | /jobs/save | Create job | Yes |
| GET | /jobs/{jobId} | Get job by ID | Yes |
| PUT | /jobs/{jobId} | Update job | Yes |
| DELETE | /jobs/{jobId} | Delete job | Yes |
| PATCH | /jobs/{jobId}/status | Update status | Yes |
| POST | /jobs/generate-description | AI generate description | Yes |
| GET | /public/jobs/{slug} | Public job details | No |

### 5.5 Candidate Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|--------------|
| GET | /candidates | List candidates | Yes |
| POST | /candidates | Create candidate | Yes |
| GET | /candidates/{id} | Get candidate | Yes |
| PUT | /candidates/{id} | Update candidate | Yes |
| DELETE | /candidates/{id} | Delete candidate | Yes |
| POST | /candidates/{candidateId}/cv | Upload CV | Yes |
| GET | /candidates/{candidateId}/cv | Get CV URL | Yes |
| DELETE | /candidates/{candidateId}/cv | Delete CV | Yes |

### 5.6 Candidate Profile Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|--------------|
| GET | /candidate/profile | Get profile | Candidate |
| PUT | /candidate/profile/basic-info | Update basic info | Candidate |
| POST/PUT/DELETE | /candidate/profile/education/* | Manage education | Candidate |
| POST/PUT/DELETE | /candidate/profile/experience/* | Manage experience | Candidate |
| POST/PUT/DELETE | /candidate/profile/skills/* | Manage skills | Candidate |
| POST/PUT/DELETE | /candidate/profile/languages/* | Manage languages | Candidate |
| GET | /candidate/profile/completeness | Profile completeness | Candidate |

### 5.7 Application Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|--------------|
| GET | /applications | List applications | Yes |
| POST | /jobs/{slug}/apply | Apply for job | Candidate |
| GET | /applications/{id} | Get application | Yes |
| DELETE | /applications/{id} | Delete application | Yes |
| PATCH | /applications/{id}/status | Update status | Yes |

### 5.8 Admin Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|--------------|
| GET | /admin/dashboard/stats | Dashboard stats | ADMIN |

### 5.9 Public Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|--------------|
| GET | /public/jobs/{slug} | Public job | No |
| POST | /candidates/apply/{token} | Apply via token | No |
| POST | /candidates/apply/{token}/with-resume | Apply with resume | No |

---

## 6. Database Schema

### 6.1 Core Tables

#### Users Table (`users`)
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| firstname | VARCHAR(255) | First name |
| lastname | VARCHAR(255) | Last name |
| email | VARCHAR(255) | Unique email |
| password | VARCHAR(255) | BCrypt hashed |
| role | VARCHAR(20) | ADMIN, HR |
| phoneNumber | VARCHAR(20) | Phone number |
| isActive | BOOLEAN | Account status |
| isDeleted | BOOLEAN | Soft delete flag |
| createdAt | TIMESTAMP | Creation time |
| updatedAt | TIMESTAMP | Last update |
| passwordResetTokenHash | VARCHAR(255) | Reset token |
| passwordResetExpiresAt | TIMESTAMP | Token expiry |

#### Jobs Table (`job`)
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| title | VARCHAR(255) | Job title |
| description | TEXT | Job description |
| location | VARCHAR(255) | Job location |
| experienceLevel | VARCHAR(50) | Experience required |
| contractType | VARCHAR(20) | FULL_TIME, PART_TIME, etc. |
| companyDetails | TEXT | Company info |
| additionalInfo |TEXT | Additional details |
| requiredSkills | TEXT[] | Array of skills |
| niceToHaveSkills | TEXT[] | Nice to have skills |
| dateLimte | TIMESTAMP | Application deadline |
| applyLink | VARCHAR(500) | Public apply URL |
| applicationToken | VARCHAR(64) | Unique token |
| status | VARCHAR(20) | DRAFT, OPEN, CLOSED |
| deleted | BOOLEAN | Soft delete |
| createdDate | TIMESTAMP | Creation time |
| updatedDate | TIMESTAMP | Last update |
| closedAt | TIMESTAMP | When closed |
| closedReason | VARCHAR(32) | Close reason |

#### Candidates Table (`candidate`)
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| firstName | VARCHAR(255) | First name |
| lastName | VARCHAR(255) | Last name |
| email | VARCHAR(255) | Unique email |
| password | VARCHAR(255) | BCrypt hashed |
| phone | VARCHAR(20) | Phone number |
| niveauEtude | VARCHAR(100) | Education level |
| domaineExpertise | VARCHAR(100) | Expertise domain |
| experienceProfessionnelle | VARCHAR(100) | Experience years |
| headline | VARCHAR(255) | Profile headline |
| summary | TEXT | Profile summary |
| location | VARCHAR(255) | Location |
| linkedinUrl | VARCHAR(255) | LinkedIn URL |
| resumeObjectKey | TEXT | MinIO object key |
| resumeUrl | TEXT | Pre-signed URL |
| resumeOriginalName | VARCHAR(255) | Original filename |
| resumeContentType | VARCHAR(100) | MIME type |
| resumeSizeBytes | BIGINT | File size |

#### Job Applications Table (`job_application`)
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| job_id | UUID | Foreign to job |
| candidate_id | UUID | Foreign to candidate |
| applicationDate | TIMESTAMP | When applied |
| status | VARCHAR(30) | Application status |
| pipeline_status | VARCHAR(30) | QUEUED, PROCESSING, etc. |
| cvBlobKey | VARCHAR(512) | CV storage key |
| cvExtractedChars | INTEGER | Extracted text length |
| ruleScore | INTEGER | Rule-based score |
| ruleScoreDetails | JSONB | Rule scoring details |
| aiScore | INTEGER | AI score |
| aiReasoning | TEXT | AI reasoning |
| aiProvider | VARCHAR(32) | AI provider used |
| processingErrorCode | VARCHAR(64) | Error code |
| processingErrorMessage | TEXT | Error message |
| processedAt | TIMESTAMP | When processed |
| pipelineAttemptCount | INTEGER | Retry attempts |
| pipelineLastAttemptAt | TIMESTAMP | Last retry time |

### 6.2 Schema Management

The application currently relies on Spring Data JPA and Hibernate `ddl-auto=update` to align the PostgreSQL schema during startup. Entity changes should be tested against a local PostgreSQL database before deployment.

---

## 7. Security

### 7.1 Authentication

- **Method**: JWT (JSON Web Tokens)
- **Library**: jjwt 0.11.5
- **Token Expiration**: 24 hours (configurable)

### 7.2 JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET:your-32-char-secret-key}
```

Generate a secure secret:
```bash
openssl rand -base64 32
```

### 7.3 Roles

| Role | Description |
|------|-------------|
| ADMIN | Full system access |
| HR | HR operations |
| CANDIDATE | Candidate portal access |

### 7.4 Password Requirements

- Minimum 8 characters
- At least one uppercase
- At least one lowercase
- At least one digit
- At least one special character (@#$%^&+=)

### 7.5 Public Routes

The following routes don't require authentication:
```
/auth/welcome
/auth/login
/auth/register
/auth/forgot-password
/auth/reset-password
/candidate/auth/*
/public/jobs/*
/candidates/apply/*
```

---

## 8. Configuration

### 8.1 Application Properties

Location: `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hr_stream
    username: postgres
    password: 123654
  jpa:
    hibernate:
      ddl-auto: update
server:
  port: 8090

app:
  seed:
    admin:
      enabled: true
      email: admin@hrstream.local
      password: Admin@1234
  scoring:
    rule-min-score: 40
    ai-min-score: 60

minio:
  endpoint: http://localhost:9000
  accessKey: admin
  secretKey: admin123
  bucket: hr-stream

jwt:
  secret: your-secret-key
```

### 8.2 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| SPRING_PROFILES_ACTIVE | Active profile | dev |
| SPRING_DATASOURCE_URL | DB URL | jdbc:postgresql://localhost:5432/hr_stream |
| SPRING_DATASOURCE_USERNAME | DB user | postgres |
| SPRING_DATASOURCE_PASSWORD | DB password | 123654 |
| SPRING_REDIS_HOST | Redis host | localhost |
| SPRING_REDIS_PORT | Redis port | 6379 |
| SPRING_REDIS_PASSWORD | Redis password | redis123 |
| MINIO_ENDPOINT | MinIO URL | http://localhost:9000 |
| MINIO_ACCESS_KEY | MinIO access | admin |
| MINIO_SECRET_KEY | MinIO secret | admin123 |
| JWT_SECRET | JWT signing secret | (must be set) |
| GEMINI_API_KEY | Gemini API key | - |
| SEED_ADMIN_ENABLED | Create default admin | true |
| SEED_ADMIN_EMAIL | Default admin email | admin@hrstream.local |
| SEED_ADMIN_PASSWORD | Default admin password | Admin@1234 |

### 8.3 AI Provider Configuration

```yaml
ai:
  providers:
    order: [openrouter, gemini, claude, glm]
    openrouter:
      enabled: false
      api-key: ${OPENROUTER_API_KEY:}
      model: openai/gpt-4o-mini
    gemini:
      enabled: true
      api-key: ${GEMINI_API_KEY:}
      model: gemini-2.0-flash
    claude:
      enabled: false
      api-key: ${CLAUDE_API_KEY:}
      model: claude-3-5-haiku-latest
```

### 8.4 Scoring Pipeline Configuration

```yaml
app:
  scoring:
    rule-min-score: 40       # Minimum rule-based score
    ai-min-score: 60       # Minimum AI score
    max-applications: 100  # Max applications per job
    required-skill-weight: 60
    nice-skill-weight: 25
    experience-keyword-weight: 15
    prompt-char-budget: 20000
    min-extracted-chars: 50
    stuck-after-minutes: 15
```

---

## 9. Development Setup

### 9.1 Prerequisites

- Java 17
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL client (optional)

### 9.2 Local Development

1. **Start infrastructure**:
```powershell
docker compose -f docker-compose.infra.yml up -d
```

2. **Configure IDE**:
- Import as Maven project
- Set Java 17 as target
- Configure run configuration with:
  - `SPRING_PROFILES_ACTIVE=dev`
  - Working directory: project root

3. **Run application**:
```bash
mvn spring-boot:run
```
Or:
```bash
./mvnw spring-boot:run
```

4. **Access**:
- API: http://localhost:8090
- Swagger UI: http://localhost:8090/swagger-ui.html
- Actuator: http://localhost:8090/actuator/health

### 9.3 Building

```bash
mvn clean package
```

Output: `target/hr-stream-0.0.1-SNAPSHOT.jar`

### 9.4 Running Tests

```bash
mvn test              # Unit tests
mvn verify            # All tests including integration
```

### 9.5 Database Reset

```bash
# Drop and recreate database
docker compose down -v
docker compose -f docker-compose.infra.yml up -d
```

---

## 10. Testing

### 10.1 Test Structure

```
src/test/java/com/medev/hrstream/
├── security/
│   └── JwtServiceTest.java
├── jobapplication/
│   └── scoring/
│       ├── scorer/
│       │   └── AiDeepScorerTest.java
│       └── extractor/
│           └── PdfBoxCvTextExtractorTest.java
```

### 10.2 Test Dependencies

- Spring Boot Test
- Testcontainers (PostgreSQL, MinIO)
- JUnit 5
- MockMvc for controller tests

### 10.3 Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=JwtServiceTest

# With coverage
mvn test jacoco:report
```

---

## 11. Deployment

### 11.1 Docker Deployment

```bash
# Build
docker build -t hr-stream:latest .

# Run full stack
docker compose up -d

# Check logs
docker compose logs -f app

# Stop
docker compose down
```

### 11.2 Production Checklist

1. Change default passwords in `.env`
2. Set `JWT_SECRET` to secure random value
3. Configure `GEMINI_API_KEY`
4. Enable HTTPS/TLS
5. Set `SPRING_PROFILES_ACTIVE=prod`
6. Configure production database
7. Review CORS settings
8. Set up monitoring

### 11.3 Health Checks

| Endpoint | Purpose |
|----------|---------|
| /actuator/health | Overall health |
| /actuator/info | Application info |
| /actuator/prometheus | Metrics |

---

## 12. Troubleshooting

### 12.1 Common Issues

| Issue | Solution |
|-------|---------|
| 401 Unauthorized | Check JWT token validity |
| 403 Forbidden | Verify role permissions |
| Database connection | Check PostgreSQL is running |
| MinIO errors | Verify credentials |
| AI generation fails | Verify API key |

### 12.2 Debug Logging

```yaml
# application.yml
logging:
  level:
    com.medev.hrstream: DEBUG
    org.springframework.security: DEBUG
```

### 12.3 Development Tips

1. **Enable H2 for faster tests**: Add H2 dependency for unit tests
2. **Hot reload**: Add Spring Boot DevTools
3. **Profile-specific configs**: Use `application-{profile}.yml`

---

## Quick Reference

### Build Command
```bash
mvn clean package -DskipTests
```

### Run Command
```bash
mvn spring-boot:run
```

### Docker Infrastructure
```bash
docker compose -f docker-compose.infra.yml up -d
```

### Default Credentials
| Service | Username | Password |
|---------|----------|----------|
| Admin | admin@hrstream.local | Admin@1234 |
| PostgreSQL | postgres | 123654 |
| Redis | - | redis123 |
| MinIO | admin | admin123 |

### Key Ports
| Service | Port |
|--------|------|
| Application | 8090 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| MinIO API | 9000 |
| MinIO Console | 9001 |
| Prometheus | 9090 |
| Grafana | 3000 |

---

*Last Updated: May 2026*
