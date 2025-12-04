package com.nevzatcirak.sharedsignals.api.model;

import java.util.List;
import java.util.Map;

/**
 * Command object for polling events.
 * Transports polling criteria from Web layer to Core layer.
 */
public class PollCommand {
    private List<String> ackIds;
    private Map<String, PollError> errorIds;
    private int maxEvents;
    private boolean returnImmediately;

    public PollCommand(List<String> ackIds, Map<String, PollError> errorIds, int maxEvents, boolean returnImmediately) {
        this.ackIds = ackIds;
        this.errorIds = errorIds;
        this.maxEvents = maxEvents;
        this.returnImmediately = returnImmediately;
    }

    public List<String> getAckIds() { return ackIds; }
    public Map<String, PollError> getErrorIds() { return errorIds; }
    public int getMaxEvents() { return maxEvents; }
    public boolean isReturnImmediately() { return returnImmediately; }

    public static class PollError {
        private String code;
        private String description;

        public PollError(String code, String description) {
            this.code = code;
            this.description = description;
        }
        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
}
