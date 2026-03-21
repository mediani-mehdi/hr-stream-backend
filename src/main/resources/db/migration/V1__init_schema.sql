-- Initial schema: users, candidate, job, job_application

CREATE TABLE users
(
    id                        VARCHAR(36) PRIMARY KEY,
    firstname                 VARCHAR(255),
    lastname                  VARCHAR(255),
    email                     VARCHAR(255) UNIQUE,
    password                  VARCHAR(255),
    role                      VARCHAR(50),
    password_reset_token_hash VARCHAR(255),
    password_reset_expires_at TIMESTAMP
);

CREATE TABLE candidate
(
    id                          VARCHAR(36) PRIMARY KEY,
    first_name                  VARCHAR(255),
    last_name                   VARCHAR(255),
    email                       VARCHAR(255) UNIQUE NOT NULL,
    password                    VARCHAR(255)        NOT NULL,
    phone                       VARCHAR(255),
    niveau_etude                VARCHAR(255),
    domaine_expertise           VARCHAR(255),
    experience_professionnelle  VARCHAR(255),
    resume_object_key           TEXT,
    resume_url                  TEXT,
    resume_original_name        VARCHAR(255),
    resume_content_type         VARCHAR(255),
    resume_size_bytes           BIGINT
);

CREATE TABLE job
(
    id               VARCHAR(36) PRIMARY KEY,
    title            VARCHAR(255),
    description      TEXT,
    apply_link       VARCHAR(255),
    application_token VARCHAR(255),
    location         VARCHAR(255),
    experience_level VARCHAR(255),
    employment_type  VARCHAR(255),
    company_details  TEXT,
    additional_info  TEXT,
    deleted          BOOLEAN   DEFAULT FALSE,
    status           VARCHAR(50),
    created_date     TIMESTAMP,
    updated_date     TIMESTAMP,
    created_by       VARCHAR(255)
);

CREATE TABLE job_skills
(
    job_id VARCHAR(36) REFERENCES job (id),
    skills VARCHAR(255)
);

CREATE TABLE job_application
(
    id               VARCHAR(36) PRIMARY KEY,
    job_id           VARCHAR(36) REFERENCES job (id),
    candidate_id     VARCHAR(36) REFERENCES candidate (id),
    application_date TIMESTAMP,
    status           VARCHAR(50) DEFAULT 'PENDING'
);
