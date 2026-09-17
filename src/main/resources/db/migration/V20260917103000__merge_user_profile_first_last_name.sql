-- Migration to merge first_name and last_name into a single full_name column in user_profiles table

-- 1. Add full_name column
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS full_name VARCHAR(150);

-- 2. Migrate existing data by concatenating first_name and last_name
UPDATE user_profiles
SET full_name = NULLIF(TRIM(CONCAT(COALESCE(first_name, ''), ' ', COALESCE(last_name, ''))), '')
WHERE first_name IS NOT NULL OR last_name IS NOT NULL;

-- 3. Drop deprecated first_name and last_name columns
ALTER TABLE user_profiles DROP COLUMN IF EXISTS first_name;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS last_name;
