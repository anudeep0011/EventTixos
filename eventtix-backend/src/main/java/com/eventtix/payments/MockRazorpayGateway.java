package com.eventtix.payments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnMissingBean(RazorpayGateway.class)
@Slf4j
public class MockRazorpayGateway implements PaymentGateway {

    public MockRazorpayGateway() {
        log.warn("Using MockRazorpayGateway — set RAZORPAY_KEY_ID/SECRET for production");
    }

    @Override
    public PaymentIntentResult createIntent(CreatePaymentRequest request) {
        String providerId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return new PaymentIntentResult(providerId, providerId, "created");
    }

    @Override
    public WebhookEvent verifyAndParseWebhook(String payload, String signatureHeader) {
        // Expect JSON with orderId field in mock mode
        String orderId = "00000000-0000-0000-0000-000000000000";
        if (payload != null && payload.contains("orderId")) {
            try {
                int i = payload.indexOf("orderId");
                int q1 = payload.indexOf('"', payload.indexOf(':', i)) + 1;
                int q2 = payload.indexOf('"', q1);
                if (q1 > 0 && q2 > q1) orderId = payload.substring(q1, q2);
            } catch (Exception ignored) {}
        }
        return new WebhookEvent("payment.captured", "order_mock", orderId, 0, true);
    }

    @Override
    public RefundResult refund(String providerPaymentId, long amountCents, String reason) {
        return new RefundResult("rfnd_mock_" + UUID.randomUUID().toString().substring(0, 8), "processed");
    }
}
