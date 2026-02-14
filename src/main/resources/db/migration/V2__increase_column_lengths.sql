-- Increase column lengths to support longer values, especially for password reset emails
ALTER TABLE emails ALTER COLUMN from_email TYPE VARCHAR(500);
ALTER TABLE emails ALTER COLUMN from_name TYPE VARCHAR(500);
ALTER TABLE emails ALTER COLUMN to_email TYPE VARCHAR(500);
ALTER TABLE emails ALTER COLUMN to_name TYPE VARCHAR(500);
ALTER TABLE emails ALTER COLUMN subject TYPE VARCHAR(1000);
ALTER TABLE emails ALTER COLUMN external_id TYPE VARCHAR(500);