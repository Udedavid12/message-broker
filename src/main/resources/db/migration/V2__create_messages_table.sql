CREATE TABLE messages (
    id UUID PRIMARY KEY,
    topic_id UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_messages_topic_status ON messages(topic_id, status);
CREATE INDEX idx_messages_created_at ON messages(created_at);