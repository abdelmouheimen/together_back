--liquibase formatted sql

--changeset together:005-create-comments
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id UUID NOT NULL REFERENCES todo_items(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    text VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_comments_item ON comments(item_id);
--rollback DROP TABLE comments;
