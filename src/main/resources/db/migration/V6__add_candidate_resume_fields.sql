ALTER TABLE candidate
    ADD COLUMN IF NOT EXISTS resume_object_key TEXT,
    ADD COLUMN IF NOT EXISTS resume_url TEXT,
    ADD COLUMN IF NOT EXISTS resume_original_name VARCHAR(512),
    ADD COLUMN IF NOT EXISTS resume_content_type VARCHAR(255),
    ADD COLUMN IF NOT EXISTS resume_size_bytes BIGINT;

