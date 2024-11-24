-- liquibase formatted sql

-- changeset vyach:1732392149040-1
ALTER TABLE user_emails ADD mail_provider VARCHAR(255);

-- changeset vyach:1732392149040-2
ALTER TABLE user_emails ALTER COLUMN  mail_provider SET NOT NULL;

-- changeset vyach:1732392149040-3
ALTER TABLE user_emails DROP COLUMN email_provider;

