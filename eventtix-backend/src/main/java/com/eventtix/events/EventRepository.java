package com.eventtix.events;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findBySlug(String slug);

    Page<Event> findByStatusAndVisibility(EventStatus status, String visibility, Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE e.status = 'PUBLISHED' AND e.visibility = 'PUBLIC'
        AND (:categoryId IS NULL OR e.categoryId = :categoryId)
        AND (:from IS NULL OR e.startAt >= :from)
        AND (:to IS NULL OR e.startAt <= :to)
        AND (:q IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY e.startAt ASC
        """)
    Page<Event> searchPublic(
            @Param("categoryId") Integer categoryId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("q") String q,
            Pageable pageable);

    Page<Event> findByOrganizerId(UUID organizerId, Pageable pageable);
}
