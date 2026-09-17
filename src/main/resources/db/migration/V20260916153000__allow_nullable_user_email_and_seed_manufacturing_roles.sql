-- Migration: Allow nullable email in user_accounts and seed manufacturing roles
-- Version: 20260916153000

-- 1. Allow email column in user_accounts to be NULL
ALTER TABLE user_accounts ALTER COLUMN email DROP NOT NULL;

-- 2. Seed manufacturing roles if they do not exist
INSERT INTO roles (id, name, description, created_by, created_at)
SELECT 10001, 'ROLE_OPERATOR', 'Operator / Line worker role - no email required', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_OPERATOR');

INSERT INTO roles (id, name, description, created_by, created_at)
SELECT 10002, 'ROLE_INSPECTOR', 'QC Inspector / KCS role - email required', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_INSPECTOR');

INSERT INTO roles (id, name, description, created_by, created_at)
SELECT 10003, 'ROLE_QC_ENGINEER', 'Quality Engineer role - email required', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_QC_ENGINEER');

INSERT INTO roles (id, name, description, created_by, created_at)
SELECT 10004, 'ROLE_SUPERVISOR', 'Production / Line Supervisor role - email required', 'system', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SUPERVISOR');
