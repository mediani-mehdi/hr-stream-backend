CREATE TABLE IF NOT EXISTS job_application (
    id VARCHAR(36) PRIMARY KEY,
    job_id VARCHAR(36) NOT NULL,
    candidate_id VARCHAR(36) NOT NULL,
    application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    CONSTRAINT fk_job FOREIGN KEY (job_id) REFERENCES job(id),
    CONSTRAINT fk_candidate FOREIGN KEY (candidate_id) REFERENCES candidate(id)
);

