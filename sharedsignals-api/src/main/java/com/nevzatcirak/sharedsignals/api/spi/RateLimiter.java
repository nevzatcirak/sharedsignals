package com.nevzatcirak.sharedsignals.api.spi;

import com.nevzatcirak.sharedsignals.api.model.RateLimitResult;

/**
 * SPI for Rate Limiting Logic.
 * Implementations provided by adapters (Caffeine, Redis, etc.).
 */
public interface RateLimiter {
    RateLimitResult tryConsume(String key);
}
