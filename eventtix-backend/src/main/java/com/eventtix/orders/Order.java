package com.eventtix.orders;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "tier_id")
    private UUID tierId;

    @Column(name = "quantity")
    private int quantity = 1;

    @Column(name = "hold_key")
    private String holdKey;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_name")
    private String guestName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "subtotal_cents", nullable = false)
    private long subtotalCents;

    @Column(name = "discount_cents", nullable = false)
    private long discountCents = 0;

    @Column(name = "total_cents", nullable = false)
    private long totalCents;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "payment_intent_id", unique = true)
    private String paymentIntentId;

    @Column(name = "discount_code_id")
    private UUID discountCodeId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;
}
