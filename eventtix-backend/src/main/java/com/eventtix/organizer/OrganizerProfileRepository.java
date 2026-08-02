package com.eventtix.organizer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, UUID> {
    Optional<OrganizerProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
