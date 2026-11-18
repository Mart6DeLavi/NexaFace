package com.nextface.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Audit record of a security-relevant event. Never contains passwords, tokens,
 * images or embeddings.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuditLog() {
        // for JPA
    }

    public AuditLog(UUID userId, UUID applicationId, AuditEventType eventType, boolean success) {
        this.userId = userId;
        this.applicationId = applicationId;
        this.eventType = eventType.name();
        this.success = success;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isSuccess() {
        return success;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
