--liquibase formatted sql

--changeset together:003-create-lists
CREATE TABLE todo_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    emoji VARCHAR(8) NOT NULL,
    accent_color VARCHAR(7) NOT NULL,
    progress_color VARCHAR(7) NOT NULL,
    created_by_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE list_members (
    list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY(list_id, user_id)
);
CREATE INDEX idx_list_members_user ON list_members(user_id);
--rollback DROP TABLE list_members; DROP TABLE todo_lists;
