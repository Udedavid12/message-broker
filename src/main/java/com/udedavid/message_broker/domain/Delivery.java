package com.udedavid.message_broker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consumer_group_id", nullable = false)
    private ConsumerGroup consumerGroup;

    @Column(name = "delivery_token", nullable = false, unique = true)
    private UUID deliveryToken;

    @Column(name = "delivered_at", nullable = false)
    private Instant deliveredAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public Delivery() {
    }

    public Delivery(Message message, ConsumerGroup consumerGroup, Instant expiresAt) {
        this.message = message;
        this.consumerGroup = consumerGroup;
        this.deliveryToken = UUID.randomUUID();
        this.deliveredAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public Message getMessage() { return message; }
    public ConsumerGroup getConsumerGroup() { return consumerGroup; }
    public UUID getDeliveryToken() { return deliveryToken; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getExpiresAt() { return expiresAt; }
}