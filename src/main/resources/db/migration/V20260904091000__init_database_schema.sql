-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create user_accounts table
CREATE TABLE IF NOT EXISTS user_accounts (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_enabled BOOLEAN NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create user_roles join table
CREATE TABLE IF NOT EXISTS user_roles (
    user_account_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_account_id, role_id),
    CONSTRAINT fk_user_roles_user_account_id FOREIGN KEY (user_account_id) REFERENCES user_accounts(id),
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create user_profiles table
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT PRIMARY KEY,
    user_account_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    avatar_url VARCHAR(500),
    date_of_birth DATE,
    gender VARCHAR(10),
    bio TEXT,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_user_profiles_user_account_id FOREIGN KEY (user_account_id) REFERENCES user_accounts(id)
);

-- Create password_reset_tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT PRIMARY KEY,
    user_account_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used BOOLEAN NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_password_reset_tokens_user_account_id FOREIGN KEY (user_account_id) REFERENCES user_accounts(id)
);
