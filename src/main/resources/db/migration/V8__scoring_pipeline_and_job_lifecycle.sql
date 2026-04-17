-- V8: ATS scoring pipeline + job lifecycle columns
-- 1. Drop legacy profile-based scoring columns on job_application
ALTER TABLE job_application
    DROP COLUMN IF EXISTS score,
    DROP COLUMN IF EXISTS score_reasoning;

-- 2. Add pipeline + PDF-driven scoring columns on job_application
ALTER TABLE job_application
    ADD COLUMN pipeline_status VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    ADD COLUMN cv_blob_key VARCHAR(512),
    ADD COLUMN cv_extracted_chars INTEGER,
    ADD COLUMN rule_score INTEGER,
    ADD COLUMN rule_score_details JSONB,
    ADD COLUMN ai_score INTEGER,
    ADD COLUMN ai_reasoning TEXT,
    ADD COLUMN ai_provider VARCHAR(32),
    ADD COLUMN processing_error_code VARCHAR(64),
    ADD COLUMN processing_error_message TEXT,
    ADD COLUMN processed_at TIMESTAMP,
    ADD COLUMN pipeline_attempt_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN pipeline_last_attempt_at TIMESTAMP;

CREATE INDEX idx_job_application_pipeline_status ON job_application (pipeline_status);
CREATE INDEX idx_job_application_job_status ON job_application (job_id, status);

-- 3. Add job lifecycle columns
ALTER TABLE job
    ADD COLUMN closed_at TIMESTAMP,
    ADD COLUMN closed_reason VARCHAR(32);

CREATE INDEX idx_job_status_date_limte ON job (status, date_limte);

-- 4. ShedLock table for distributed scheduler coordination
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
