package com.eventtix.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private static final Duration HOLD_TTL = Duration.ofMinutes(10);
    private static final String HOLD_KEY_PREFIX = "ticket_hold:";

    private final StringRedisTemplate redisTemplate;

    public HoldResult hold(UUID tierId, int quantity, String sessionId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        String holdKey = HOLD_KEY_PREFIX + tierId + ":" + sessionId;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(holdKey, String.valueOf(quantity), HOLD_TTL.toSeconds(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            redisTemplate.opsForValue().set(holdKey, String.valueOf(quantity), HOLD_TTL.toSeconds(), TimeUnit.SECONDS);
        }
        Instant expiresAt = Instant.now().plus(HOLD_TTL);
        log.info("Held {} units of tier {} session {} until {}", quantity, tierId, sessionId, expiresAt);
        return new HoldResult(holdKey, quantity, expiresAt);
    }

    public void release(String holdKey) {
        redisTemplate.delete(holdKey);
        log.info("Released hold {}", holdKey);
    }

    public void confirm(String holdKey) {
        release(holdKey);
    }

    public boolean isHoldActive(String holdKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(holdKey));
    }

    public record HoldResult(String holdKey, int quantity, Instant expiresAt) {}
}
