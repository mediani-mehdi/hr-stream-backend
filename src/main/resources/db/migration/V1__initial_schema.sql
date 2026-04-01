CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    firstname VARCHAR(255),
    lastname VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50),
    password_reset_token_hash VARCHAR(255),
    password_reset_expires_at TIMESTAMP
);

CREATE TABLE candidate (
    id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    niveau_etude VARCHAR(255),
    domaine_expertise VARCHAR(255),
    experience_professionnelle VARCHAR(255),
    resume_object_key TEXT,
    resume_url TEXT,
    resume_original_name VARCHAR(255),
    resume_content_type VARCHAR(255),
    resume_size_bytes BIGINT
);

CREATE TABLE job (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    apply_link VARCHAR(255),
    application_token VARCHAR(255),
    location VARCHAR(255),
    experience_level VARCHAR(255),
    employment_type VARCHAR(255),
    company_details TEXT,
    additional_info TEXT,
    deleted BOOLEAN,
    status VARCHAR(50),
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    created_by VARCHAR(255)
);

CREATE TABLE job_skills (
    job_id VARCHAR(255) NOT NULL,
    skills VARCHAR(255),
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES job (id) ON DELETE CASCADE
);

CREATE TABLE job_application (
    id VARCHAR(255) PRIMARY KEY,
    job_id VARCHAR(255),
    candidate_id VARCHAR(255),
    application_date TIMESTAMP,
    status VARCHAR(50),
    CONSTRAINT fk_job_application_job FOREIGN KEY (job_id) REFERENCES job (id) ON DELETE SET NULL,
    CONSTRAINT fk_job_application_candidate FOREIGN KEY (candidate_id) REFERENCES candidate (id) ON DELETE SET NULL
);

CREATE INDEX idx_candidate_email ON candidate(email);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_job_application_job_id ON job_application(job_id);
CREATE INDEX idx_job_application_candidate_id ON job_application(candidate_id);
