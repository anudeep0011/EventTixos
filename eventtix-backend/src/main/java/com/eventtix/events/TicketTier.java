package com.eventtix.events;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "price_cents", nullable = false)
    private long priceCents = 0;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(name = "quantity_total", nullable = false)
    private int quantityTotal;

    @Column(name = "quantity_sold", nullable = false)
    private int quantitySold = 0;

    @Column(name = "min_per_order", nullable = false)
    private int minPerOrder = 1;

    @Column(name = "max_per_order", nullable = false)
    private int maxPerOrder = 10;

    @Column(name = "sale_start_at")
    private Instant saleStartAt;

    @Column(name = "sale_end_at")
    private Instant saleEndAt;

    public int getAvailable() {
        return Math.max(0, quantityTotal - quantitySold);
    }
}
