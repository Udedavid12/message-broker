CREATE TABLE consumer_groups (
    id UUID PRIMARY KEY,
    topic_id UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(topic_id, name)
);

CREATE TABLE deliveries (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    consumer_group_id UUID NOT NULL REFERENCES consumer_groups(id) ON DELETE CASCADE,
    delivery_token UUID NOT NULL UNIQUE,
    delivered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_deliveries_token ON deliveries(delivery_token);
CREATE INDEX idx_deliveries_expires ON deliveries(expires_at);