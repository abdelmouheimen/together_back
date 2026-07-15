--liquibase formatted sql

--changeset together:008-add-list-parent
ALTER TABLE todo_lists ADD COLUMN parent_id UUID REFERENCES todo_lists(id) ON DELETE CASCADE;
CREATE INDEX idx_lists_parent ON todo_lists(parent_id);
--rollback ALTER TABLE todo_lists DROP COLUMN parent_id;
