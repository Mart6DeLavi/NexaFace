package com.nextface.face;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.nextface.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Biometric profile of a user: a single normalized face embedding.
 */
@Entity
@Table(name = "face_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_face_profiles_user", columnNames = "user_id"))
public class FaceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "embedding", nullable = false, columnDefinition = "real[]")
    private float[] embedding;

    @Column(name = "embedding_dimension", nullable = false)
    private int embeddingDimension;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FaceProfile() {
        // for JPA
    }

    public FaceProfile(User user, float[] embedding) {
        this.user = user;
        this.embedding = embedding.clone();
        this.embeddingDimension = embedding.length;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void updateEmbedding(float[] embedding) {
        this.embedding = embedding.clone();
        this.embeddingDimension = embedding.length;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public float[] getEmbedding() {
        return embedding.clone();
    }

    public int getEmbeddingDimension() {
        return embeddingDimension;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
