package com.eventtix.orders;

import com.eventtix.auth.User;
import com.eventtix.events.TicketTier;
import com.eventtix.events.TicketTierRepository;
import com.eventtix.inventory.InventoryService;
import com.eventtix.payments.PaymentGateway;
import com.eventtix.tickets.EmailService;
import com.eventtix.tickets.QrService;
import com.eventtix.tickets.Ticket;
import com.eventtix.tickets.TicketRepository;
import com.eventtix.tickets.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final InventoryService inventoryService;
    private final TicketTierRepository tierRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final PaymentGateway paymentGateway;
    private final QrService qrService;
    private final EmailService emailService;

    public InventoryService.HoldResult hold(UUID tierId, int quantity, String sessionId) {
        TicketTier tier = tierRepository.findById(tierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tier not found"));
        if (quantity < tier.getMinPerOrder() || quantity > tier.getMaxPerOrder()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be between " + tier.getMinPerOrder() + " and " + tier.getMaxPerOrder());
        }
        if (tier.getAvailable() < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough tickets remaining");
        }
        return inventoryService.hold(tierId, quantity, sessionId);
    }

    @Transactional
    public CreateIntentResponse createPaymentIntent(CreateIntentRequest req, User userOrNull) {
        TicketTier tier = tierRepository.findById(req.tierId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tier not found"));
        if (!inventoryService.isHoldActive(req.holdKey())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Hold expired — please try again");
        }
        long subtotal = tier.getPriceCents() * req.quantity();
        long total = subtotal;
        Order order = Order.builder()
                .eventId(tier.getEventId())
                .tierId(req.tierId())
                .quantity(req.quantity())
                .holdKey(req.holdKey())
                .userId(userOrNull != null ? userOrNull.getId() : null)
                .guestEmail(req.guestEmail())
                .guestName(req.guestName())
                .status(OrderStatus.PENDING)
                .subtotalCents(subtotal)
                .discountCents(0)
                .totalCents(total)
                .currency(tier.getCurrency() != null ? tier.getCurrency() : "INR")
                .paymentProvider("MOCK")
                .build();
        order = orderRepository.save(order);
        try {
            var intent = paymentGateway.createIntent(new PaymentGateway.CreatePaymentRequest(
                    order.getId().toString(), total, order.getCurrency(),
                    req.guestEmail(), req.guestName(),
                    Map.of("orderId", order.getId().toString(), "tierId", req.tierId().toString(),
                            "quantity", String.valueOf(req.quantity()), "holdKey", req.holdKey())
            ));
            order.setPaymentIntentId(intent.providerPaymentId());
            if (intent.providerPaymentId() != null && intent.providerPaymentId().startsWith("order_mock"))
                order.setPaymentProvider("MOCK");
            else if (intent.providerPaymentId() != null && intent.providerPaymentId().startsWith("pi_"))
                order.setPaymentProvider("STRIPE");
            else
                order.setPaymentProvider("RAZORPAY");
            orderRepository.save(order);
            return new CreateIntentResponse(order.getId(), intent.providerPaymentId(), intent.clientSecretOrKey(), total, order.getPaymentProvider());
        } catch (Exception e) {
            inventoryService.release(req.holdKey());
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            log.error("Payment intent creation failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment provider error: " + e.getMessage());
        }
    }

    @Transactional
    public ConfirmResponse confirmMockPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() == OrderStatus.PAID)
            return new ConfirmResponse(order.getId(), "ALREADY_PAID", ticketRepository.findByOrderItemId(order.getId()).size());
        if (order.getStatus() != OrderStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is " + order.getStatus());
        finalizePaidOrder(order);
        return new ConfirmResponse(order.getId(), "PAID", ticketRepository.findByOrderItemId(order.getId()).size());
    }

    @Transactional
    public void confirmFromWebhook(String providerPaymentId, String ourOrderId, long amountCents) {
        UUID orderId = UUID.fromString(ourOrderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("Webhook already processed for order {}", ourOrderId);
            return;
        }
        try {
            if (order.getPaymentIntentId() == null) order.setPaymentIntentId(providerPaymentId);
            finalizePaidOrder(order);
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate webhook for payment_intent_id {}", providerPaymentId);
        }
    }

    private void finalizePaidOrder(Order order) {
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(Instant.now());
        orderRepository.save(order);
        UUID tierId = order.getTierId();
        int quantity = order.getQuantity() > 0 ? order.getQuantity() : 1;
        if (tierId == null) {
            log.error("Order {} has no tierId", order.getId());
            return;
        }
        TicketTier tier = tierRepository.findByIdForUpdate(tierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tier not found"));
        if (tier.getQuantitySold() + quantity > tier.getQuantityTotal()) {
            log.error("Oversell prevented tier {} order {}", tierId, order.getId());
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            return;
        }
        tier.setQuantitySold(tier.getQuantitySold() + quantity);
        tierRepository.save(tier);
        if (order.getHoldKey() != null) inventoryService.confirm(order.getHoldKey());
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Ticket t = Ticket.builder()
                    .orderItemId(order.getId())
                    .eventId(order.getEventId())
                    .attendeeName(order.getGuestName())
                    .attendeeEmail(order.getGuestEmail())
                    .qrSignature("pending")
                    .status(TicketStatus.VALID)
                    .build();
            t = ticketRepository.save(t);
            t.setQrSignature(qrService.sign(t.getId(), order.getEventId()));
            tickets.add(ticketRepository.save(t));
        }
        log.info("Order {} PAID. {} tickets. sold={}", order.getId(), tickets.size(), tier.getQuantitySold());
        if (order.getGuestEmail() != null && !order.getGuestEmail().isBlank()) {
            try {
                emailService.sendOrderConfirmationWithTickets(order.getGuestEmail(), "Your Event", order.getId().toString(), tickets);
            } catch (Exception e) {
                log.warn("Email send failed (non-fatal): {}", e.getMessage());
            }
        }
    }

    public record CreateIntentRequest(UUID tierId, int quantity, String holdKey, String guestEmail, String guestName) {}
    public record CreateIntentResponse(UUID orderId, String providerPaymentId, String clientSecret, long totalCents, String paymentProvider) {}
    public record ConfirmResponse(UUID orderId, String status, int ticketCount) {}
}
