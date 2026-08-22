-- =========================================================================
-- V2__fix_column_names.sql: Synchronize Column Names with JPA Entities
-- =========================================================================

-- 1. Add used column to otp_verifications
ALTER TABLE otp_verifications ADD COLUMN used BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Add expires_at to refresh_tokens
ALTER TABLE refresh_tokens ADD COLUMN expires_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 3. Add created_at to reviews
ALTER TABLE reviews ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
