-- Ensure job columns can store long text (e.g., AI generated descriptions)
ALTER TABLE job ALTER COLUMN description TYPE TEXT;
ALTER TABLE job ALTER COLUMN company_details TYPE TEXT;
ALTER TABLE job ALTER COLUMN additional_info TYPE TEXT;

