package com.udedavid.message_broker.web.dto;
import com.udedavid.message_broker.domain.Message;
import java.time.Instant;
import java.util.UUID;
public record MessageResponse(
    UUID id,
    String topicName,
    String payload,
    String status,
    int retryCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getTopic().getName(),
            message.getPayload(),
            message.getStatus().name(),
            message.getRetryCount(),
            message.getCreatedAt(),
            message.getUpdatedAt()
        );
    }
}