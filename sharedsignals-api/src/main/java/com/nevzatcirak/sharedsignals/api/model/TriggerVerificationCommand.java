
package com.nevzatcirak.sharedsignals.api.model;

/**
 * Command object for triggering a verification event.
 * <p>
 * Framework-agnostic POJO for transporting verification trigger data
 * from Web layer to Core layer.
 * <p>
 * SSF Spec Section 8.1.4.2: Triggering a Verification Event
 */
public class TriggerVerificationCommand {

    private final String streamId;
    private final String state;

    public TriggerVerificationCommand(String streamId, String state) {
        this.streamId = streamId;
        this.state = state;
    }

    /**
     * Gets the stream identifier.
     * <p>
     * SSF Spec: REQUIRED. Identifies the stream for verification.
     *
     * @return the stream ID
     */
    public String getStreamId() {
        return streamId;
    }

    /**
     * Gets the opaque state value.
     * <p>
     * SSF Spec: OPTIONAL. An arbitrary string that the Transmitter
     * will echo back in the Verification Event.
     *
     * @return the state value, or null if not provided
     */
    public String getState() {
        return state;
    }
}