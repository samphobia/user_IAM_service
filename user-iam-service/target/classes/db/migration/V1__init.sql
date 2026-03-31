CREATE TABLE merchants (
    merchant_id UUID PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    business_category VARCHAR(255) NOT NULL,
    business_email VARCHAR(255) NOT NULL UNIQUE,
    business_phone VARCHAR(64) NOT NULL,
    rc_number VARCHAR(128),
    address_line_1 VARCHAR(255) NOT NULL,
    address_line_2 VARCHAR(255),
    city VARCHAR(128) NOT NULL,
    state VARCHAR(128) NOT NULL,
    country VARCHAR(128) NOT NULL,
    postal_code VARCHAR(32) NOT NULL,
    status VARCHAR(64) NOT NULL,
    tier VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128) NOT NULL,
    phone VARCHAR(64),
    role VARCHAR(64) NOT NULL,
    merchant_id UUID REFERENCES merchants(merchant_id),
    status VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auth_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    type VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_users_merchant_id ON users (merchant_id);
CREATE INDEX idx_auth_tokens_user_type ON auth_tokens (user_id, type);
CREATE INDEX idx_auth_tokens_hash_type ON auth_tokens (token_hash, type);
