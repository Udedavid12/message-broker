package com.udedavid.message_broker.web.dto;

import com.udedavid.message_broker.domain.ConsumerGroup;
import java.time.Instant;
import java.util.UUID;

public record ConsumerGroupResponse(
    UUID id,
    String topicName,
    String name,
    Instant createdAt
) {
    public static ConsumerGroupResponse from(ConsumerGroup group) {
        return new ConsumerGroupResponse(
            group.getId(),
            group.getTopic().getName(),
            group.getName(),
            group.getCreatedAt()
        );
    }
}