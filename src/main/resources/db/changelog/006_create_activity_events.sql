--liquibase formatted sql

--changeset together:006-create-activity-events
CREATE TABLE activity_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(30) NOT NULL,
    actor_id UUID NOT NULL REFERENCES users(id),
    list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
    item_id UUID REFERENCES todo_items(id) ON DELETE SET NULL,
    extra_text VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_activity_list ON activity_events(list_id);
CREATE INDEX idx_activity_actor ON activity_events(actor_id);
CREATE INDEX idx_activity_created ON activity_events(created_at DESC);
--rollback DROP TABLE activity_events;
