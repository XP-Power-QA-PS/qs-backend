-- Migration: Update manufacturing roles to standard 18-digit Snowflake IDs
-- Version: 20260916154500

-- 1. Temporarily drop FK constraint on user_roles to allow updating PK/FK
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_role_id;

-- 2. Update roles to 18-digit Snowflake IDs
UPDATE roles SET id = 272280320496111617 WHERE id = 10001 OR (name = 'ROLE_OPERATOR' AND id < 100000000000000000);
UPDATE roles SET id = 272280320496111618 WHERE id = 10002 OR (name = 'ROLE_INSPECTOR' AND id < 100000000000000000);
UPDATE roles SET id = 272280320496111619 WHERE id = 10003 OR (name = 'ROLE_QC_ENGINEER' AND id < 100000000000000000);
UPDATE roles SET id = 272280320496111620 WHERE id = 10004 OR (name = 'ROLE_SUPERVISOR' AND id < 100000000000000000);

-- 3. Update existing references in user_roles join table
UPDATE user_roles SET role_id = 272280320496111617 WHERE role_id = 10001;
UPDATE user_roles SET role_id = 272280320496111618 WHERE role_id = 10002;
UPDATE user_roles SET role_id = 272280320496111619 WHERE role_id = 10003;
UPDATE user_roles SET role_id = 272280320496111620 WHERE role_id = 10004;

-- 4. Re-add FK constraint with ON UPDATE CASCADE
ALTER TABLE user_roles 
    ADD CONSTRAINT fk_user_roles_role_id 
    FOREIGN KEY (role_id) REFERENCES roles(id) ON UPDATE CASCADE;
