package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.enums.SubjectStatus;
import com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException;
import com.nevzatcirak.sharedsignals.api.service.StreamAdministrationService;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DefaultStreamAdministrationService implements StreamAdministrationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultStreamAdministrationService.class);
    private final StreamStore streamStore;

    public DefaultStreamAdministrationService(StreamStore streamStore) {
        this.streamStore = streamStore;
    }

    @Override
    public void updateSubjectStatus(String streamId, String subjectHash, SubjectStatus status, String owner) {
        // Validate stream existence
        if (streamStore.findById(streamId).isEmpty()) {
            throw new StreamNotFoundException(streamId);
        }

        log.info("Admin [{}] updating subject status to {} for stream {}", owner, status, streamId);
        streamStore.updateSubjectStatus(streamId, subjectHash, status);
    }

    @Override
    public void updateAuthorizedEvents(String streamId, Set<String> authorizedEvents, String owner) {
        if (streamStore.findById(streamId).isEmpty()) {
            throw new StreamNotFoundException(streamId);
        }

        log.info("Admin [{}] updating authorized events for stream {}: {}", owner, streamId, authorizedEvents);
        streamStore.updateAuthorizedEvents(streamId, authorizedEvents);
    }

    @Override
    public void setStreamBroadcastMode(String streamId, boolean enabled, String owner) {
        if (streamStore.findById(streamId).isEmpty()) {
            throw new StreamNotFoundException(streamId);
        }
        log.warn("Admin [{}] setting ProcessAllSubjects={} for stream {}. This is a high-privilege operation.", owner, enabled, streamId);
        streamStore.updateStreamMode(streamId, enabled);
    }
}
