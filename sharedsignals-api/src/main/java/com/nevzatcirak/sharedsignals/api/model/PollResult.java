package com.nevzatcirak.sharedsignals.api.model;

import java.util.Map;

/**
 * Result object from polling events.
 * Framework agnostic.
 */
public class PollResult {
    private final Map<String, String> events;
    private final boolean moreAvailable;

    public PollResult(Map<String, String> events, boolean moreAvailable) {
        this.events = events;
        this.moreAvailable = moreAvailable;
    }

    public Map<String, String> getEvents() { return events; }
    public boolean isMoreAvailable() { return moreAvailable; }
}
