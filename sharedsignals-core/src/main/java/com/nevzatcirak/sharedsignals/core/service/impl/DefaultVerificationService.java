package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.model.VerificationRequest;
import com.nevzatcirak.sharedsignals.api.service.EventPublisherService;
import com.nevzatcirak.sharedsignals.api.service.VerificationService;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.core.builder.SecurityEventBuilder;
import com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException;
import com.nevzatcirak.sharedsignals.api.exception.SsfSecurityException;

import java.util.HashMap;
import java.util.Map;

public class DefaultVerificationService implements VerificationService {

    private final EventPublisherService eventPublisher;
    private final StreamStore streamStore;

    public DefaultVerificationService(EventPublisherService eventPublisher, StreamStore streamStore) {
        this.eventPublisher = eventPublisher;
        this.streamStore = streamStore;
    }

    @Override
    public void triggerVerification(VerificationRequest request, String owner) {
        validateOwner(request.getStream_id(), owner);
        Map<String, Object> payload = SecurityEventBuilder.createVerification(request.getState());

        Map<String, Object> subject = request.getSubject();
        if (subject == null) {
            subject = new HashMap<>();
        }

        eventPublisher.publishToStream(request.getStream_id(), subject, payload);
    }

    private void validateOwner(String streamId, String owner) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        if (stream.getAud() == null || !stream.getAud().contains(owner)) {
            throw new SsfSecurityException("Access Denied: You are not an audience of this stream.");
        }
    }
}
