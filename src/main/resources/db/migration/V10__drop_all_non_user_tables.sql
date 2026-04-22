-- Destructive cleanup: keep only the users table
DROP TABLE IF EXISTS shedlock CASCADE;
DROP TABLE IF EXISTS job_application CASCADE;
DROP TABLE IF EXISTS job_required_skills CASCADE;
DROP TABLE IF EXISTS job_nice_to_have_skills CASCADE;
DROP TABLE IF EXISTS job_skills CASCADE;
DROP TABLE IF EXISTS job CASCADE;
DROP TABLE IF EXISTS candidate_languages CASCADE;
DROP TABLE IF EXISTS candidate_skills CASCADE;
DROP TABLE IF EXISTS candidate_experience CASCADE;
DROP TABLE IF EXISTS candidate_education CASCADE;
DROP TABLE IF EXISTS candidate CASCADE;
