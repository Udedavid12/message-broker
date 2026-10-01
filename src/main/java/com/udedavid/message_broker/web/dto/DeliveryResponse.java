package com.udedavid.message_broker.web.dto;

import com.udedavid.message_broker.domain.Delivery;
import java.time.Instant;
import java.util.UUID;

public record DeliveryResponse(
    UUID deliveryId,
    UUID deliveryToken,
    UUID messageId,
    String payload,
    Instant deliveredAt,
    Instant expiresAt
) {
    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
            delivery.getId(),
            delivery.getDeliveryToken(),
            delivery.getMessage().getId(),
            delivery.getMessage().getPayload(),
            delivery.getDeliveredAt(),
            delivery.getExpiresAt()
        );
    }
}