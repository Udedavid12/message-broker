package com.udedavid.message_broker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "consumer_groups",
    uniqueConstraints = @UniqueConstraint(columnNames = {"topic_id", "name"})
)
public class ConsumerGroup {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ConsumerGroup() {
    }

    public ConsumerGroup(Topic topic, String name) {
        this.topic = topic;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Topic getTopic() { return topic; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}