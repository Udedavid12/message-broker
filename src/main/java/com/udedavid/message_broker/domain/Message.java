package com.udedavid.message_broker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Message() {
    }

    public Message(Topic topic, String payload) {
        this.topic = topic;
        this.payload = payload;
        this.status = MessageStatus.PENDING;
        this.retryCount = 0;
        this.maxRetries = 3;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Topic getTopic() { return topic; }
    public String getPayload() { return payload; }
    public MessageStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(MessageStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = Instant.now();
    }

    public enum MessageStatus {
        PENDING,
        IN_FLIGHT,
        ACKED,
        DLQ
    }
}