package com.udedavid.message_broker.domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "topics")
public class Topic {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    public Topic() {
    }
    public Topic(String name) {
        this.name = name;
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
    public void setName(String name) { this.name = name; }
}