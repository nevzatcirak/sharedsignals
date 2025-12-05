
package com.nevzatcirak.sharedsignals.api.service;

/**
 * Service interface for managing stream inactivity timeout.
 * <p>
 * SSF Spec Section 8.1.1: After the inactivity timeout duration passes
 * with no eligible activity from the Receiver, the Transmitter MAY
 * pause, disable, or delete the stream.
 */
public interface InactivityTimeoutService {

    /**
     * Records activity for a stream (resets inactivity timer).
     * <p>
     * SSF Spec: The following constitutes eligible Receiver activity:
     * - PUSH: Receiver calls any management API endpoint
     * - POLL: Receiver polls for events OR calls any management API endpoint
     *
     * @param streamId the stream identifier
     */
    void recordActivity(String streamId);

    /**
     * Checks all streams for inactivity timeout and takes action.
     * <p>
     * SSF Spec: If timeout exceeded, Transmitter MAY pause, disable, or delete.
     * If pausing or disabling, MUST send Stream Updated event.
     * <p>
     * This method is typically called by a scheduled job.
     */
    void checkAndHandleInactiveStreams();

    /**
     * Initializes activity tracking for a newly created stream.
     * <p>
     * Should be called when a stream is created or re-enabled.
     *
     * @param streamId the stream identifier
     */
    void initializeStreamActivity(String streamId);

    /**
     * Removes activity tracking for a deleted stream.
     * <p>
     * Should be called when a stream is deleted.
     *
     * @param streamId the stream identifier
     */
    void removeStreamActivity(String streamId);
}