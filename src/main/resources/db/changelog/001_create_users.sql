--liquibase formatted sql

--changeset together:001-create-users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    initials VARCHAR(2) NOT NULL,
    avatar_color VARCHAR(7) NOT NULL,
    avatar_text_color VARCHAR(7) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'FRIEND',
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON users(email);
--rollback DROP TABLE users;
