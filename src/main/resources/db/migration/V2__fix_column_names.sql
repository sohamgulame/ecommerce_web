-- =========================================================================
-- V2__fix_column_names.sql: Synchronize Column Names with JPA Entities
-- =========================================================================

-- 1. Ensure used column exists in otp_verifications
ALTER TABLE otp_verifications ADD COLUMN IF NOT EXISTS used BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Ensure expires_at exists in refresh_tokens
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 3. Ensure created_at exists in reviews
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
