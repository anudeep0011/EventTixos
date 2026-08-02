package com.eventtix.payments;

import java.util.Map;

public interface PaymentGateway {
    PaymentIntentResult createIntent(CreatePaymentRequest request);
    WebhookEvent verifyAndParseWebhook(String payload, String signatureHeader);
    RefundResult refund(String providerPaymentId, long amountCents, String reason);

    record CreatePaymentRequest(
            String orderId,
            long amountCents,
            String currency,
            String customerEmail,
            String customerName,
            Map<String, String> metadata
    ) {}

    record PaymentIntentResult(
            String providerPaymentId,
            String clientSecretOrKey,
            String status
    ) {}

    record WebhookEvent(
            String eventType,
            String providerPaymentId,
            String ourOrderId,
            long amountCents,
            boolean success
    ) {}

    record RefundResult(
            String providerRefundId,
            String status
    ) {}
}
