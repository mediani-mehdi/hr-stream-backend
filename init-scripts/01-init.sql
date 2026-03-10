-- init-scripts/01-init.sql
--
-- Production-friendly bootstrap for ATS database.
-- NOTE:
--  - Your project also uses Flyway migrations under src/main/resources/db/migration.
--    This init script is meant for "first-time" database creation in Docker.
--  - All objects are created with IF NOT EXISTS (or guarded DO blocks) to reduce conflicts.
--
-- Requirements implemented:
--  1) UUID extension (pgcrypto)
--  2) Tables: users, jobs, applications, resume_files
--  3) Performance indexes
--  4) Read-only analytics user
--  5) Optional row-level security scaffolding

BEGIN;

-- 1) UUID support
-- Prefer pgcrypto (gen_random_uuid) on Postgres 13+.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2) Core tables

-- USERS
CREATE TABLE IF NOT EXISTS public.users (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    firstname    varchar(255),
    lastname     varchar(255),
    email        varchar(255) UNIQUE NOT NULL,
    password     varchar(255) NOT NULL,
    role         varchar(50) NOT NULL,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now()
);

-- JOBS
CREATE TABLE IF NOT EXISTS public.jobs (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    title             varchar(255) NOT NULL,
    description       text,
    apply_link        varchar(512),
    application_token varchar(255) UNIQUE,
    location          varchar(255),
    experience_level  varchar(255),
    employment_type   varchar(255),
    company_details   text,
    additional_info   text,
    status            varchar(50) NOT NULL DEFAULT 'DRAFT',
    deleted           boolean NOT NULL DEFAULT false,
    created_by        uuid NULL,
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_jobs_created_by
        FOREIGN KEY (created_by)
        REFERENCES public.users(id)
        ON DELETE SET NULL
);

-- APPLICATIONS
-- One row per candidate submission to a job.
CREATE TABLE IF NOT EXISTS public.applications (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id         uuid NOT NULL,

    -- Applicant personal details (kept here so you can query without joining candidate table)
    first_name     varchar(255),
    last_name      varchar(255),
    email          varchar(255) NOT NULL,
    phone          varchar(50),

    status         varchar(50) NOT NULL DEFAULT 'PENDING',
    applied_at     timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_applications_job
        FOREIGN KEY (job_id)
        REFERENCES public.jobs(id)
        ON DELETE CASCADE
);

-- RESUME FILES
-- Stores metadata for uploaded resume objects (MinIO key).
CREATE TABLE IF NOT EXISTS public.resume_files (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id  uuid NOT NULL,

    -- MinIO/S3 object key (ex: "ats-resumes/2026/01/<uuid>.pdf")
    object_key      varchar(1024) NOT NULL,
    bucket          varchar(255) NOT NULL,

    original_name   varchar(512),
    content_type    varchar(255),
    size_bytes      bigint,
    sha256          varchar(64),

    uploaded_at     timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_resume_files_application
        FOREIGN KEY (application_id)
        REFERENCES public.applications(id)
        ON DELETE CASCADE
);

-- 3) Indexes for performance

-- Users
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users (email);
CREATE INDEX IF NOT EXISTS idx_users_role ON public.users (role);

-- Jobs
CREATE INDEX IF NOT EXISTS idx_jobs_status ON public.jobs (status);
CREATE INDEX IF NOT EXISTS idx_jobs_deleted ON public.jobs (deleted);
CREATE INDEX IF NOT EXISTS idx_jobs_created_at ON public.jobs (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_jobs_created_by ON public.jobs (created_by);
CREATE INDEX IF NOT EXISTS idx_jobs_location ON public.jobs (location);
CREATE INDEX IF NOT EXISTS idx_jobs_token ON public.jobs (application_token);

-- Applications
CREATE INDEX IF NOT EXISTS idx_applications_job_id ON public.applications (job_id);
CREATE INDEX IF NOT EXISTS idx_applications_email ON public.applications (email);
CREATE INDEX IF NOT EXISTS idx_applications_status ON public.applications (status);
CREATE INDEX IF NOT EXISTS idx_applications_applied_at ON public.applications (applied_at DESC);

-- Resume files
CREATE UNIQUE INDEX IF NOT EXISTS ux_resume_files_object_key ON public.resume_files (bucket, object_key);
CREATE INDEX IF NOT EXISTS idx_resume_files_application_id ON public.resume_files (application_id);
CREATE INDEX IF NOT EXISTS idx_resume_files_uploaded_at ON public.resume_files (uploaded_at DESC);

-- 4) Read-only analytics user
-- Uses env vars if supplied by docker-compose (POSTGRES_USER is bootstrap superuser).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ats_analytics') THEN
        CREATE ROLE ats_analytics LOGIN;
    END IF;
END $$;

-- Set password only if a secret is passed in via psql variable or environment.
-- In Docker official image, init scripts are executed by the container entrypoint using psql.
-- You can provide ANALYTICS_PASSWORD in env and use it via psql \set if you want.
-- For now, leave it unset; you should ALTER ROLE ... PASSWORD from a secure pipeline.

-- Grant read-only access on current and future tables
GRANT CONNECT ON DATABASE postgres TO ats_analytics;
GRANT USAGE ON SCHEMA public TO ats_analytics;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO ats_analytics;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO ats_analytics;

-- 5) Row Level Security (optional scaffolding)
-- If you want per-tenant/per-user isolation later, you can enable RLS.
-- We keep it OFF by default to avoid breaking apps unexpectedly.
-- Example policies (commented out):
--
-- ALTER TABLE public.jobs ENABLE ROW LEVEL SECURITY;
-- CREATE POLICY jobs_owner_policy ON public.jobs
--   FOR SELECT
--   USING (created_by = current_setting('app.current_user_id', true)::uuid);
--
-- ALTER TABLE public.applications ENABLE ROW LEVEL SECURITY;
-- CREATE POLICY applications_owner_policy ON public.applications
--   FOR SELECT
--   USING (
--     EXISTS (
--       SELECT 1 FROM public.jobs j
--       WHERE j.id = applications.job_id
--         AND j.created_by = current_setting('app.current_user_id', true)::uuid
--     )
--   );

COMMIT;

