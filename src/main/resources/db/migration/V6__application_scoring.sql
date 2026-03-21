-- Add AI-scoring columns to job_application
ALTER TABLE job_application
    ADD COLUMN score           INTEGER,
    ADD COLUMN score_reasoning TEXT;
