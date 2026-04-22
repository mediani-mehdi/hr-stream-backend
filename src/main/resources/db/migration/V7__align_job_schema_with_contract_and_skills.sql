ALTER TABLE job
    ADD COLUMN IF NOT EXISTS contract_type VARCHAR(50);

UPDATE job
SET contract_type = employment_type
WHERE contract_type IS NULL
  AND employment_type IS NOT NULL;

ALTER TABLE job
    DROP COLUMN IF EXISTS employment_type;

ALTER TABLE job
    ADD COLUMN IF NOT EXISTS date_limte TIMESTAMP;

CREATE TABLE IF NOT EXISTS job_required_skills (
    job_id VARCHAR(255) NOT NULL,
    required_skills VARCHAR(255),
    CONSTRAINT fk_job_required_skills_job FOREIGN KEY (job_id) REFERENCES job (id) ON DELETE CASCADE
);

INSERT INTO job_required_skills (job_id, required_skills)
SELECT js.job_id, js.skills
FROM job_skills js
WHERE js.skills IS NOT NULL;

CREATE TABLE IF NOT EXISTS job_nice_to_have_skills (
    job_id VARCHAR(255) NOT NULL,
    nice_to_have_skills VARCHAR(255),
    CONSTRAINT fk_job_nice_to_have_skills_job FOREIGN KEY (job_id) REFERENCES job (id) ON DELETE CASCADE
);

