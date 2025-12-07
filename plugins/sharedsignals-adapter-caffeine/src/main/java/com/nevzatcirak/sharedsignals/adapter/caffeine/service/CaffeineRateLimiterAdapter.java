package com.nevzatcirak.sharedsignals.adapter.caffeine.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nevzatcirak.sharedsignals.api.model.RateLimitResult;
import com.nevzatcirak.sharedsignals.api.spi.RateLimiter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.local.LocalBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * In-Memory Rate Limiter implementation using Caffeine Cache.
 * Limits are configurable via application properties.
 */
@Component
public class CaffeineRateLimiterAdapter implements RateLimiter {

    // Cache to store token buckets associated with a key (IP or ClientID)
    private final Map<String, LocalBucket> buckets;

    // Configuration Fields
    private final long capacity;
    private final long refillTokens;
    private final Duration refillDuration;

    public CaffeineRateLimiterAdapter(
            @Value("${sharedsignals.ratelimit.capacity:100}") long capacity,
            @Value("${sharedsignals.ratelimit.refill-tokens:100}") long refillTokens,
            @Value("${sharedsignals.ratelimit.refill-duration-seconds:60}") long refillDurationSeconds
    ) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = Duration.ofSeconds(refillDurationSeconds);

        this.buckets = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .<String, LocalBucket>build()
                .asMap();
    }

    @Override
    public RateLimitResult tryConsume(String key) {
        LocalBucket bucket = buckets.computeIfAbsent(key, this::createNewBucket);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return RateLimitResult.success(probe.getRemainingTokens());
        } else {
            long waitForRefillSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            return RateLimitResult.rejected(waitForRefillSeconds);
        }
    }

    private LocalBucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(this.capacity)
                .refillGreedy(this.refillTokens, this.refillDuration)
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}