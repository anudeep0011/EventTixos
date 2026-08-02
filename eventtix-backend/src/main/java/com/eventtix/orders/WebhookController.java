package com.eventtix.orders;

import com.eventtix.payments.PaymentGateway;
import com.eventtix.payments.RazorpayGateway;
import com.eventtix.payments.StripeGateway;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final CheckoutService checkoutService;
    private final PaymentGateway paymentGateway;

    @Autowired(required = false)
    private StripeGateway stripeGateway;

    @Autowired(required = false)
    private RazorpayGateway razorpayGateway;

    @PostMapping(value = "/stripe", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    public ResponseEntity<String> stripe(HttpServletRequest request) throws IOException {
        if (stripeGateway == null) {
            log.warn("Stripe webhook received but StripeGateway not configured");
            return ResponseEntity.badRequest().body("stripe_not_configured");
        }
        String payload = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        String signature = request.getHeader("Stripe-Signature");
        try {
            PaymentGateway.WebhookEvent event = stripeGateway.verifyAndParseWebhook(payload, signature);
            if (event.success()) {
                if (event.ourOrderId() == null || event.ourOrderId().isBlank()) {
                    return ResponseEntity.ok("ignored_missing_order_id");
                }
                checkoutService.confirmFromWebhook(
                        event.providerPaymentId(), event.ourOrderId(), event.amountCents());
                return ResponseEntity.ok("ok");
            }
            return ResponseEntity.ok("ignored");
        } catch (SecurityException e) {
            log.warn("Stripe webhook rejected: {}", e.getMessage());
            return ResponseEntity.status(400).body("invalid_signature");
        } catch (Exception e) {
            log.error("Stripe webhook processing error", e);
            return ResponseEntity.internalServerError().body("error");
        }
    }

    @PostMapping(value = "/razorpay", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    public ResponseEntity<String> razorpay(HttpServletRequest request) throws IOException {
        PaymentGateway gw = razorpayGateway != null ? razorpayGateway : paymentGateway;
        String payload = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        String signature = request.getHeader("X-Razorpay-Signature");
        try {
            PaymentGateway.WebhookEvent event = gw.verifyAndParseWebhook(payload, signature);
            if (event.success() && event.ourOrderId() != null && !event.ourOrderId().isBlank()) {
                checkoutService.confirmFromWebhook(
                        event.providerPaymentId(), event.ourOrderId(), event.amountCents());
            }
            return ResponseEntity.ok("ok");
        } catch (SecurityException e) {
            return ResponseEntity.status(400).body("invalid_signature");
        } catch (Exception e) {
            log.error("Razorpay webhook error", e);
            return ResponseEntity.internalServerError().body("error");
        }
    }
}
