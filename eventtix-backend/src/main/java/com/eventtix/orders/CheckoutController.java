package com.eventtix.orders;

import com.eventtix.auth.User;
import com.eventtix.config.RateLimitService;
import com.eventtix.inventory.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final RateLimitService rateLimitService;

    @PostMapping("/hold")
    public InventoryService.HoldResult hold(
            @Valid @RequestBody HoldDto dto,
            HttpServletRequest request) {
        rateLimitService.checkCheckout(clientKey(request));
        return checkoutService.hold(dto.tierId(), dto.quantity(), dto.sessionId());
    }

    @PostMapping("/create-payment-intent")
    public CheckoutService.CreateIntentResponse createIntent(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateIntentDto dto,
            HttpServletRequest request) {
        rateLimitService.checkCheckout(clientKey(request));
        return checkoutService.createPaymentIntent(
                new CheckoutService.CreateIntentRequest(
                        dto.tierId(), dto.quantity(), dto.holdKey(),
                        dto.guestEmail(), dto.guestName()),
                user);
    }

    @PostMapping("/confirm-mock")
    public CheckoutService.ConfirmResponse confirmMock(
            @Valid @RequestBody ConfirmMockDto dto,
            HttpServletRequest request) {
        rateLimitService.checkCheckout(clientKey(request));
        return checkoutService.confirmMockPayment(dto.orderId());
    }

    private String clientKey(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    public record HoldDto(
            @NotNull UUID tierId,
            @Positive int quantity,
            @NotBlank String sessionId
    ) {}

    public record CreateIntentDto(
            @NotNull UUID tierId,
            @Positive int quantity,
            @NotBlank String holdKey,
            @NotBlank String guestEmail,
            String guestName
    ) {}

    public record ConfirmMockDto(@NotNull UUID orderId) {}
}
