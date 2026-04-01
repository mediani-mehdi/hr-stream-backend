ALTER TABLE candidate
    ADD COLUMN IF NOT EXISTS headline VARCHAR(255),
    ADD COLUMN IF NOT EXISTS summary TEXT,
    ADD COLUMN IF NOT EXISTS location VARCHAR(255),
    ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(512);

CREATE TABLE candidate_education (
    id UUID PRIMARY KEY,
    candidate_id VARCHAR(255) NOT NULL,
    institution VARCHAR(255) NOT NULL,
    degree VARCHAR(255),
    field_of_study VARCHAR(255),
    start_year INTEGER,
    end_year INTEGER,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT,
    CONSTRAINT fk_candidate_education_candidate FOREIGN KEY (candidate_id)
        REFERENCES candidate (id) ON DELETE CASCADE
);

CREATE INDEX idx_candidate_education_candidate_id ON candidate_education(candidate_id);

CREATE TABLE candidate_experience (
    id UUID PRIMARY KEY,
    candidate_id VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT,
    CONSTRAINT fk_candidate_experience_candidate FOREIGN KEY (candidate_id)
        REFERENCES candidate (id) ON DELETE CASCADE
);

CREATE INDEX idx_candidate_experience_candidate_id ON candidate_experience(candidate_id);

CREATE TABLE candidate_skills (
    id UUID PRIMARY KEY,
    candidate_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    level VARCHAR(32) NOT NULL,
    CONSTRAINT chk_candidate_skills_level CHECK (level IN ('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT')),
    CONSTRAINT fk_candidate_skills_candidate FOREIGN KEY (candidate_id)
        REFERENCES candidate (id) ON DELETE CASCADE
);

CREATE INDEX idx_candidate_skills_candidate_id ON candidate_skills(candidate_id);

CREATE TABLE candidate_languages (
    id UUID PRIMARY KEY,
    candidate_id VARCHAR(255) NOT NULL,
    language VARCHAR(128) NOT NULL,
    level VARCHAR(16) NOT NULL,
    CONSTRAINT chk_candidate_languages_level CHECK (level IN ('A1','A2','B1','B2','C1','C2','NATIVE')),
    CONSTRAINT fk_candidate_languages_candidate FOREIGN KEY (candidate_id)
        REFERENCES candidate (id) ON DELETE CASCADE
);

CREATE INDEX idx_candidate_languages_candidate_id ON candidate_languages(candidate_id);

