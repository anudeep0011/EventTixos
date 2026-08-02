package com.eventtix.payments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class PaymentGatewayConfig {

    @Bean
    @Primary
    public PaymentGateway paymentGateway(
            @Autowired(required = false) StripeGateway stripeGateway,
            @Autowired(required = false) RazorpayGateway razorpayGateway,
            @Autowired(required = false) MockRazorpayGateway mockGateway) {
        if (stripeGateway != null) {
            log.info("Primary PaymentGateway: Stripe");
            return stripeGateway;
        }
        if (razorpayGateway != null) {
            log.info("Primary PaymentGateway: Razorpay");
            return razorpayGateway;
        }
        if (mockGateway != null) {
            log.warn("Primary PaymentGateway: Mock");
            return mockGateway;
        }
        throw new IllegalStateException("No PaymentGateway bean available");
    }
}
