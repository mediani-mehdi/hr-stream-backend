-- Add profile columns to the candidates table
ALTER TABLE candidate
    ADD COLUMN headline     VARCHAR(255),
    ADD COLUMN summary      TEXT,
    ADD COLUMN location     VARCHAR(255),
    ADD COLUMN linkedin_url VARCHAR(255);

-- Education records for a candidate
CREATE TABLE candidate_education
(
    id             VARCHAR(36) PRIMARY KEY,
    candidate_id   VARCHAR(36) NOT NULL REFERENCES candidate (id),
    institution    VARCHAR(255),
    degree         VARCHAR(255),
    field_of_study VARCHAR(255),
    start_year     INTEGER,
    end_year       INTEGER,
    is_current     BOOLEAN DEFAULT FALSE,
    description    TEXT
);

-- Work-experience records for a candidate
CREATE TABLE candidate_experience
(
    id           VARCHAR(36) PRIMARY KEY,
    candidate_id VARCHAR(36) NOT NULL REFERENCES candidate (id),
    company      VARCHAR(255),
    title        VARCHAR(255),
    location     VARCHAR(255),
    start_date   DATE,
    end_date     DATE,
    is_current   BOOLEAN DEFAULT FALSE,
    description  TEXT
);

-- Skills for a candidate
CREATE TABLE candidate_skills
(
    id           VARCHAR(36) PRIMARY KEY,
    candidate_id VARCHAR(36) NOT NULL REFERENCES candidate (id),
    name         VARCHAR(255),
    level        VARCHAR(50) CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'))
);

-- Language proficiency records for a candidate
CREATE TABLE candidate_languages
(
    id           VARCHAR(36) PRIMARY KEY,
    candidate_id VARCHAR(36) NOT NULL REFERENCES candidate (id),
    language     VARCHAR(255),
    level        VARCHAR(50) CHECK (level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2', 'NATIVE'))
);
