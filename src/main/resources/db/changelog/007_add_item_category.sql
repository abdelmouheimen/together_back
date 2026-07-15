--liquibase formatted sql

--changeset together:007-add-item-category
ALTER TABLE todo_items ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'OTHER';
--rollback ALTER TABLE todo_items DROP COLUMN category;
