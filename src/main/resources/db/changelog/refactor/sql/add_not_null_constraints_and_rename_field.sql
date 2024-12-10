-- liquibase formatted sql

-- changeset vyach:1733608398622-6
ALTER TABLE user_emails ADD access_token_ended TIMESTAMP WITHOUT TIME ZONE;

-- changeset vyach:1733608398622-7
ALTER TABLE user_emails ALTER COLUMN  access_token_ended SET NOT NULL;

-- changeset vyach:1733608398622-8
ALTER TABLE user_emails DROP COLUMN end_access_token_life;
ALTER TABLE user_emails DROP COLUMN access_token;
ALTER TABLE user_emails DROP COLUMN refresh_token;

-- changeset vyach:1733608398622-2
ALTER TABLE user_emails ADD access_token BYTEA NOT NULL;

-- changeset vyach:1733608398622-4
ALTER TABLE user_emails ADD refresh_token BYTEA NOT NULL;

-- changeset vyach:1733608398622-5
ALTER TABLE user_emails ALTER COLUMN  refresh_token SET NOT NULL;

