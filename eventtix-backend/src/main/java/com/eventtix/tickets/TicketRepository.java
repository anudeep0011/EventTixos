package com.eventtix.tickets;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByEventIdAndAttendeeEmail(UUID eventId, String email);
    List<Ticket> findByOrderItemId(UUID orderItemId);
    Optional<Ticket> findByQrSignature(String qrSignature);

    @Modifying
    @Query("""
        UPDATE Ticket t SET t.status = 'CHECKED_IN', t.checkedInAt = :now, t.checkedInBy = :staffId
        WHERE t.id = :id AND t.status = 'VALID'
        """)
    int markCheckedInIfValid(@Param("id") UUID id, @Param("now") Instant now, @Param("staffId") UUID staffId);
}
