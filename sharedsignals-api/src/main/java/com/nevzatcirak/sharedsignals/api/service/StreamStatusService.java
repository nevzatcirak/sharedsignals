package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.StreamStatus;

/**
 * Service interface for managing Stream Status.
 */
public interface StreamStatusService {

    /**
     * Retrieves status with ownership check.
     */
    StreamStatus getStatus(String streamId, String owner);

    /**
     * Updates status with ownership check (User initiated).
     * Returns the updated status object.
     */
    StreamStatus updateStatus(String streamId, String status, String reason, String owner);

    /**
     * Updates status WITHOUT ownership check (System initiated, e.g. Auto-Pause).
     * Returns void as it's an internal background operation.
     */
    void updateStatus(String streamId, String status, String reason);
}
