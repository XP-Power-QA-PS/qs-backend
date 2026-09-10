-- Add machine_verified column to equipment_test_attempts table
ALTER TABLE equipment_test_attempts
ADD COLUMN machine_verified BOOLEAN NOT NULL DEFAULT FALSE;
