# HR Stream - API Schema Documentation

This document outlines the JSON payloads for the primary REST API endpoints available in the HR Stream backend, designed for the frontend team to build the user interface.

## 1. Authentication (Admins / HR)

### 1.1 Login
- **Endpoint**: `POST /auth/login`
- **Description**: Authenticate an admin/HR user.
- **Request (JSON)**:
  ```json
  {
    "email": "user@domain.com",
    "password": "securepassword123"
  }
  ```
- **Response (JSON)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR...",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "firstName": "John",
      "lastName": "Doe",
      "email": "user@domain.com",
      "role": "ADMIN"
    }
  }
  ```

### 1.2 Register
- **Endpoint**: `POST /auth/register`
- **Request (JSON)**:
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@domain.com",
    "password": "securepassword123"
  }
  ```
- **Response (JSON)**: Same as Login response.

## 2. Candidate Authentication

### 2.1 Candidate Login
- **Endpoint**: `POST /candidate/auth/login`
- **Description**: Authenticate a candidate applying for jobs.
- **Request (JSON)**:
  ```json
  {
    "email": "candidate@domain.com",
    "password": "securepassword123"
  }
  ```
- **Response (JSON)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR...",
    "candidate": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "candidate@domain.com"
    }
  }
  ```

### 2.2 Candidate Register
- **Endpoint**: `POST /candidate/auth/register`
- **Request (JSON)**:
  ```json
  {
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "candidate@domain.com",
    "password": "securepassword123"
  }
  ```
- **Response (JSON)**: Same as Candidate Login response.

## 3. Jobs Management

### 3.1 Get All Jobs
- **Endpoint**: `GET /jobs`
- **Query Parameters**: `page` (default 0), `size` (default 10), `sortBy` (default "createdDate"), `direction` (default "desc")
- **Response (JSON)**:
  ```json
  {
    "content": [
      {
        "id": "job-uuid",
        "title": "Software Engineer",
        "slug": "software-engineer",
        "description": "Job description here...",
        "status": "OPEN",
        "createdDate": "2024-03-21T10:00:00Z"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
  ```

### 3.2 Create / Save Job
- **Endpoint**: `POST /jobs/save`
- **Request (JSON)**:
  ```json
  {
    "title": "Software Engineer",
    "description": "Detailed description of the job...",
    "department": "Engineering",
    "location": "Remote",
    "status": "DRAFT"
  }
  ```
- **Response**: `String` (ID of the created job)

### 3.3 Update Job
- **Endpoint**: `PUT /jobs/{jobId}`
- **Request (JSON)**: Same as Create Job payload.
- **Response (JSON)**: The updated Job object.

### 3.4 Change Job Status
- **Endpoint**: `PATCH /jobs/{jobId}/status?status=OPEN`
- **Response**: `String` (Confirmation message)

## 4. Candidate Profiles & Resumes

### 4.1 Get All Candidates (Admin)
- **Endpoint**: `GET /candidates`
- **Response (JSON)**: Paginated structure similar to `Get All Jobs` returning Candidate objects.

### 4.2 Upload CV for Candidate
- **Endpoint**: `POST /candidates/{candidateId}/cv`
- **Content-Type**: `multipart/form-data`
- **Body**:
  - `file`: (Binary File - PDF/Word)
- **Response (JSON)**:
  ```json
  {
    "url": "https://minio-url/presigned-link-valid-for-1-hour",
    "filename": "resume.pdf",
    "status": "SUCCESS"
  }
  ```

### 4.3 Get Candidate CV
- **Endpoint**: `GET /candidates/{candidateId}/cv`
- **Response (JSON)**: Returns the same `CvResponse` payload containing a pre-signed download URL.

## 5. Job Applications

### 5.1 Apply for a Job (Candidate)
- **Endpoint**: `POST /jobs/{slug}/apply`
- **Headers**: Requires Authorization token (`Bearer <token>`).
- **Description**: Automatically links the authenticated candidate to the job via its slug.
- **Response (JSON)**:
  ```json
  {
    "id": "application-uuid",
    "status": "SUBMITTED",
    "applicationDate": "2024-03-21T10:00:00Z",
    "score": null
  }
  ```

### 5.2 List All Applications (Admin)
- **Endpoint**: `GET /applications`
- **Response (JSON)**: Paginated structure returning JobApplication objects.

### 5.3 Update Application Status (Admin)
- **Endpoint**: `PATCH /applications/{id}/status?status=REVIEWING`
- **Description**: Update an application's progression. Typical statuses: `SUBMITTED`, `REVIEWING`, `INTERVIEWING`, `REJECTED`, `HIRED`.
- **Response (JSON)**: The updated JobApplication object.
