package com.udedavid.message_broker.web.dto;
import com.udedavid.message_broker.domain.Topic;
import java.time.Instant;
import java.util.UUID;
public record TopicResponse(UUID id, String name, Instant createdAt) {
    public static TopicResponse from(Topic topic) {
        return new TopicResponse(topic.getId(), topic.getName(), topic.getCreatedAt());
    }
}