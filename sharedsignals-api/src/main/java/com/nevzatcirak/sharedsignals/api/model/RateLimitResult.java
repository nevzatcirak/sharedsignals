package com.nevzatcirak.sharedsignals.api.model;

public class RateLimitResult {
    private final boolean allowed;
    private final long remainingTokens;
    private final long waitTimeSeconds;

    private RateLimitResult(boolean allowed, long remainingTokens, long waitTimeSeconds) {
        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public static RateLimitResult success(long remainingTokens) {
        return new RateLimitResult(true, remainingTokens, 0);
    }

    public static RateLimitResult rejected(long waitTimeSeconds) {
        return new RateLimitResult(false, 0, waitTimeSeconds);
    }

    public boolean isAllowed() { return allowed; }
    public long getRemainingTokens() { return remainingTokens; }
    public long getWaitTimeSeconds() { return waitTimeSeconds; }
}
