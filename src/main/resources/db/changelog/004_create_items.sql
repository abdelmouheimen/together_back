--liquibase formatted sql

--changeset together:004-create-items
CREATE TABLE todo_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
    text VARCHAR(500) NOT NULL,
    done BOOLEAN NOT NULL DEFAULT false,
    checked_by_id UUID REFERENCES users(id),
    checked_at TIMESTAMPTZ,
    position INT NOT NULL DEFAULT 0,
    created_by_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_items_list ON todo_items(list_id);
--rollback DROP TABLE todo_items;
