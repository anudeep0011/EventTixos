package com.eventtix.payments;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@ConditionalOnExpression("!'${stripe.secret-key:}'.trim().isEmpty()")
@Slf4j
public class StripeGateway implements PaymentGateway {

    private static final Set<String> SUCCESS_TYPES = Set.of(
            "payment_intent.succeeded",
            "checkout.session.completed"
    );
    private static final Set<String> FAILURE_TYPES = Set.of(
            "payment_intent.payment_failed",
            "payment_intent.canceled"
    );

    private final String webhookSecret;
    private final boolean requireSignature;

    public StripeGateway(
            @Value("${stripe.secret-key}") String secretKey,
            @Value("${stripe.webhook-secret:}") String webhookSecret,
            @Value("${stripe.require-webhook-signature:true}") boolean requireSignature) {
        Stripe.apiKey = secretKey;
        this.webhookSecret = webhookSecret != null ? webhookSecret.trim() : "";
        this.requireSignature = requireSignature;
        log.info("StripeGateway initialized");
    }

    @Override
    public PaymentIntentResult createIntent(CreatePaymentRequest request) {
        try {
            PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                    .setAmount(request.amountCents())
                    .setCurrency(request.currency() != null ? request.currency().toLowerCase() : "inr")
                    .putMetadata("orderId", request.orderId())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true).build());
            if (request.customerEmail() != null && !request.customerEmail().isBlank()) {
                builder.setReceiptEmail(request.customerEmail());
            }
            if (request.metadata() != null) {
                request.metadata().forEach((k, v) -> { if (v != null) builder.putMetadata(k, v); });
            }
            PaymentIntent intent = PaymentIntent.create(builder.build());
            return new PaymentIntentResult(intent.getId(), intent.getClientSecret(), intent.getStatus());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }

    @Override
    public WebhookEvent verifyAndParseWebhook(String payload, String signatureHeader) {
        if (payload == null || payload.isBlank()) {
            throw new SecurityException("Empty Stripe webhook payload");
        }
        Event event = constructEvent(payload, signatureHeader);
        String type = event.getType();
        boolean success = SUCCESS_TYPES.contains(type);
        if (!success && !FAILURE_TYPES.contains(type)) {
            return new WebhookEvent(type, event.getId(), "", 0, false);
        }
        ParsedObject parsed = parseDataObject(event);
        if (success && (parsed.orderId == null || parsed.orderId.isBlank())) {
            return new WebhookEvent(type, parsed.providerPaymentId, "", parsed.amountCents, false);
        }
        return new WebhookEvent(type,
                parsed.providerPaymentId != null ? parsed.providerPaymentId : event.getId(),
                parsed.orderId != null ? parsed.orderId : "",
                parsed.amountCents, success);
    }

    private Event constructEvent(String payload, String signatureHeader) {
        boolean hasSecret = webhookSecret != null && !webhookSecret.isBlank();
        if (hasSecret) {
            if (signatureHeader == null || signatureHeader.isBlank()) {
                throw new SecurityException("Missing Stripe-Signature header");
            }
            try {
                return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
            } catch (SignatureVerificationException e) {
                throw new SecurityException("Invalid Stripe webhook signature", e);
            }
        }
        if (requireSignature) {
            throw new SecurityException("STRIPE_WEBHOOK_SECRET is not set");
        }
        return Event.GSON.fromJson(payload, Event.class);
    }

    private ParsedObject parseDataObject(Event event) {
        String providerPaymentId = "";
        String orderId = "";
        long amountCents = 0;
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (deserializer.getObject().isPresent()) {
            stripeObject = deserializer.getObject().get();
        } else {
            try { stripeObject = deserializer.deserializeUnsafe(); }
            catch (Exception ignored) {}
        }
        if (stripeObject instanceof PaymentIntent pi) {
            providerPaymentId = pi.getId();
            amountCents = pi.getAmount() != null ? pi.getAmount() : 0;
            orderId = meta(pi.getMetadata(), "orderId");
        } else if (stripeObject instanceof Session session) {
            providerPaymentId = session.getPaymentIntent() != null
                    ? session.getPaymentIntent() : session.getId();
            amountCents = session.getAmountTotal() != null ? session.getAmountTotal() : 0;
            orderId = meta(session.getMetadata(), "orderId");
            if (orderId.isBlank() && session.getClientReferenceId() != null) {
                orderId = session.getClientReferenceId();
            }
        }
        return new ParsedObject(providerPaymentId, orderId, amountCents);
    }

    private static String meta(Map<String, String> metadata, String key) {
        if (metadata == null) return "";
        String v = metadata.get(key);
        return v != null ? v : "";
    }

    @Override
    public RefundResult refund(String providerPaymentId, long amountCents, String reason) {
        try {
            RefundCreateParams.Builder b = RefundCreateParams.builder()
                    .setPaymentIntent(providerPaymentId).setAmount(amountCents);
            if (reason != null && !reason.isBlank()) {
                b.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
            }
            Refund refund = Refund.create(b.build());
            return new RefundResult(refund.getId(), refund.getStatus());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe refund failed: " + e.getMessage(), e);
        }
    }

    private record ParsedObject(String providerPaymentId, String orderId, long amountCents) {}
}
