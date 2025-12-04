package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.model.StreamStatus;
import com.nevzatcirak.sharedsignals.api.service.StreamStatusService;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException;
import com.nevzatcirak.sharedsignals.api.exception.SsfBadRequestException;
import com.nevzatcirak.sharedsignals.api.exception.SsfSecurityException;

public class DefaultStreamStatusService implements StreamStatusService {

    private final StreamStore streamStore;

    public DefaultStreamStatusService(StreamStore streamStore) {
        this.streamStore = streamStore;
    }

    @Override
    public StreamStatus getStatus(String streamId, String owner) {
        validateOwner(streamId, owner);
        return fetchStatus(streamId);
    }

    @Override
    public StreamStatus updateStatus(String streamId, String status, String reason, String owner) {
        validateOwner(streamId, owner);
        return performUpdate(streamId, status, reason);
    }

    @Override
    public void updateStatus(String streamId, String status, String reason) {
        performUpdate(streamId, status, reason);
    }

    private StreamStatus performUpdate(String streamId, String newStatus, String reason) {
        if (!SharedSignalConstants.STATUS_ENABLED.equals(newStatus) &&
            !SharedSignalConstants.STATUS_PAUSED.equals(newStatus) &&
            !SharedSignalConstants.STATUS_DISABLED.equals(newStatus)) {
            throw new SsfBadRequestException("Invalid status. Must be enabled, paused, or disabled.");
        }

        StreamConfiguration config = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        config.setStatus(newStatus);
        config.setReason(reason);

        StreamConfiguration saved = streamStore.save(config);

        StreamStatus response = new StreamStatus();
        response.setStream_id(saved.getStream_id());
        response.setStatus(saved.getStatus());
        response.setReason(saved.getReason());
        return response;
    }

    private StreamStatus fetchStatus(String streamId) {
        StreamConfiguration config = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        StreamStatus status = new StreamStatus();
        status.setStream_id(config.getStream_id());
        status.setStatus(config.getStatus());
        status.setReason(config.getReason());
        return status;
    }

    private void validateOwner(String streamId, String owner) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        if (stream.getAud() == null || !stream.getAud().contains(owner)) {
            throw new SsfSecurityException("Access Denied: You are not an audience of this stream.");
        }
    }
}
