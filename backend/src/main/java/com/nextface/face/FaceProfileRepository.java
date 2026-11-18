package com.nextface.face;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FaceProfileRepository extends JpaRepository<FaceProfile, UUID> {

    Optional<FaceProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
