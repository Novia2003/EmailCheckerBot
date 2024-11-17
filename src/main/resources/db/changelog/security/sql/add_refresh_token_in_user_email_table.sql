-- liquibase formatted sql

-- changeset vyach:1731867687636-1
ALTER TABLE user_emails ADD access_token VARCHAR(255);
ALTER TABLE user_emails ADD end_access_token_life TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE user_emails ADD refresh_token VARCHAR(255);

-- changeset vyach:1731867687636-2
ALTER TABLE user_emails ALTER COLUMN  access_token SET NOT NULL;

-- changeset vyach:1731867687636-5
ALTER TABLE user_emails DROP COLUMN token;

