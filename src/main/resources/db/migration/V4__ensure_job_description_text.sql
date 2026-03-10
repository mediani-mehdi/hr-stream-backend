-- Create job table if not exists and ensure columns are TEXT
CREATE TABLE IF NOT EXISTS job (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    apply_link VARCHAR(255),
    application_token VARCHAR(255),
    location VARCHAR(255),
    experience_level VARCHAR(255),
    employment_type VARCHAR(255),
    company_details TEXT,
    additional_info TEXT,
    deleted BOOLEAN DEFAULT FALSE,
    status VARCHAR(50),
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    created_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS job_skills (
    job_id VARCHAR(36) NOT NULL,
    skills VARCHAR(255),
    FOREIGN KEY (job_id) REFERENCES job(id)
);

-- In case table already existed but with wrong types:
DO $$
BEGIN
    ALTER TABLE job ALTER COLUMN description TYPE TEXT;
    ALTER TABLE job ALTER COLUMN company_details TYPE TEXT;
    ALTER TABLE job ALTER COLUMN additional_info TYPE TEXT;
EXCEPTION
    WHEN others THEN NULL;
END $$;



