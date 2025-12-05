package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.model.StreamDelivery;
import com.nevzatcirak.sharedsignals.api.service.StreamConfigurationService;
import com.nevzatcirak.sharedsignals.api.service.InactivityTimeoutService;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.api.exception.*;
import com.nevzatcirak.sharedsignals.core.security.SecureUrlValidator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of StreamConfigurationService.
 * <p>
 * Implements SSF Draft Spec Section 8.1.1 (Stream Configuration).
 */
public class DefaultStreamConfigurationService implements StreamConfigurationService {

    private final StreamStore streamStore;
    private final String issuerUrl;
    private final int defaultInactivityTimeout;
    private final boolean allowMultipleStreamsPerReceiver;
    private final int maxDescriptionLength;
    private final InactivityTimeoutService inactivityService;

    public DefaultStreamConfigurationService(
            StreamStore streamStore,
            String issuerUrl,
            int defaultInactivityTimeout,
            boolean allowMultipleStreamsPerReceiver,
            int maxDescriptionLength,
            InactivityTimeoutService inactivityService) {
        this.streamStore = streamStore;
        this.issuerUrl = issuerUrl.endsWith("/") ? issuerUrl.substring(0, issuerUrl.length() - 1) : issuerUrl;
        this.defaultInactivityTimeout = defaultInactivityTimeout;
        this.allowMultipleStreamsPerReceiver = allowMultipleStreamsPerReceiver;
        this.maxDescriptionLength = maxDescriptionLength;
        this.inactivityService = inactivityService;
    }

    @Override
    public StreamConfiguration createStream(StreamConfiguration request, String owner) {
        // SSF Spec 8.1.1.1: Check if stream already exists for this receiver
    if (!allowMultipleStreamsPerReceiver) {
        List<StreamConfiguration> existingStreams = streamStore.findByAudience(owner);
        if (!existingStreams.isEmpty()) {
            throw new StreamAlreadyExistsException(owner);
        }
    }

    request.setStream_id(generateStreamId());
    request.setIss(this.issuerUrl);

    // Ignore any aud value sent by receiver - this is a security boundary
    List<String> audience = new ArrayList<>();
    audience.add(owner);  // owner is derived from authenticated JWT token
    request.setAud(audience);

    request.setEvents_supported(SharedSignalConstants.SUPPORTED_EVENTS);
    request.setMin_verification_interval(30);
    request.setInactivity_timeout(this.defaultInactivityTimeout);

    // Truncate if exceeds max length (Transmitter's policy)
    if (request.getDescription() != null && request.getDescription().length() > maxDescriptionLength) {
        request.setDescription(request.getDescription().substring(0, maxDescriptionLength));
    }
    computeEventsDelivered(request);
    configureDelivery(request);

    StreamConfiguration saved = streamStore.save(request);
    inactivityService.initializeStreamActivity(saved.getStream_id());
    return saved;
    }

    @Override
    public StreamConfiguration getStream(String streamId, String owner) {
        StreamConfiguration stream = findAndValidateOwner(streamId, owner);
        stream.setEvents_supported(SharedSignalConstants.SUPPORTED_EVENTS);
        return stream;
    }

    @Override
    public List<StreamConfiguration> listStreams(String owner) {
        List<StreamConfiguration> streams = streamStore.findByAudience(owner);
        streams.forEach(s -> s.setEvents_supported(SharedSignalConstants.SUPPORTED_EVENTS));
        return streams;
    }

    @Override
    public StreamConfiguration updateStream(String streamId, StreamConfiguration patch, String owner) {
        StreamConfiguration existing = findAndValidateOwner(streamId, owner);

        // SSF Spec 8.1.1.3: Validate events_delivered if present
        validateEventsDelivered(patch, existing);

        // SSF Spec 8.1.1.3: Update only Receiver-Supplied properties present in request
        if (patch.getDelivery() != null) {
            patch.setStream_id(streamId);
            configureDelivery(patch);
            existing.setDelivery(patch.getDelivery());
        }

        if (patch.getEvents_requested() != null) {
            existing.setEvents_requested(patch.getEvents_requested());
            computeEventsDelivered(existing);
        }

        if (patch.getDescription() != null) {
            String description = patch.getDescription();
            if (description.length() > maxDescriptionLength) {
                description = description.substring(0, maxDescriptionLength);
            }
            existing.setDescription(description);
        }

        existing.setEvents_supported(SharedSignalConstants.SUPPORTED_EVENTS);
        return streamStore.save(existing);
    }

    @Override
    public StreamConfiguration replaceStream(String streamId, StreamConfiguration put, String owner) {
        StreamConfiguration existing = findAndValidateOwner(streamId, owner);

        // SSF Spec 8.1.1.4: Validate events_delivered if present
        validateEventsDelivered(put, existing);

        // Preserve stream_id and aud (cannot be updated per spec)
        put.setStream_id(streamId);

        // SSF Spec 8.1.1: aud property cannot be updated
        put.setAud(existing.getAud());

        // Set Transmitter-Supplied properties
        put.setIss(this.issuerUrl);
        put.setEvents_supported(SharedSignalConstants.SUPPORTED_EVENTS);
        put.setMin_verification_interval(30);
        put.setInactivity_timeout(this.defaultInactivityTimeout);

        // Truncate description if needed
        if (put.getDescription() != null && put.getDescription().length() > maxDescriptionLength) {
            put.setDescription(put.getDescription().substring(0, maxDescriptionLength));
        }

        computeEventsDelivered(put);
        configureDelivery(put);
        return streamStore.save(put);
    }

    @Override
    public void deleteStream(String streamId, String owner) {
        findAndValidateOwner(streamId, owner);
        streamStore.deleteById(streamId);

        inactivityService.removeStreamActivity(streamId);
    }

    /**
     * Computes events_delivered based on intersection of events_supported and events_requested.
     * <p>
     * SSF Spec 8.1.1: events_delivered is a subset of the intersection.
     *
     * @param config the stream configuration
     */
    private void computeEventsDelivered(StreamConfiguration config) {
        List<String> requested = config.getEvents_requested();
        if (requested == null || requested.isEmpty()) {
            config.setEvents_delivered(Collections.emptyList());
            return;
        }

        List<String> delivered = requested.stream()
                .filter(SharedSignalConstants.SUPPORTED_EVENTS::contains)
                .collect(Collectors.toList());

        config.setEvents_delivered(delivered);
    }

    /**
     * Validates ownership and retrieves the stream.
     *
     * @param streamId the stream identifier
     * @param owner the authenticated client ID
     * @return the stream configuration
     * @throws StreamNotFoundException if stream doesn't exist
     * @throws SsfSecurityException if owner is not in aud
     */
    private StreamConfiguration findAndValidateOwner(String streamId, String owner) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        if (stream.getAud() == null || !stream.getAud().contains(owner)) {
            throw new SsfSecurityException("Access Denied: You are not an audience of this stream.");
        }
        return stream;
    }

    /**
     * Configures delivery method and endpoint.
     * <p>
     * SSF Spec 8.1.1.1: If delivery is not provided, assume POLL method.
     *
     * @param config the stream configuration
     */
    private void configureDelivery(StreamConfiguration config) {
        if (config.getDelivery() == null) {
            StreamDelivery defaults = new StreamDelivery();
            defaults.setMethod(SharedSignalConstants.DELIVERY_METHOD_POLL);
            config.setDelivery(defaults);
        }

        String method = config.getDelivery().getMethod();

        if (SharedSignalConstants.DELIVERY_METHOD_PUSH.equals(method)) {
            SecureUrlValidator.validate(config.getDelivery().getEndpoint_url());
        } else if (SharedSignalConstants.DELIVERY_METHOD_POLL.equals(method)) {
            String pollUrl = this.issuerUrl + "/ssf/events/poll/" + config.getStream_id();
            config.getDelivery().setEndpoint_url(pollUrl);
        } else {
            throw new SsfBadRequestException("Unsupported delivery method: " + method);
        }
    }

    /**
     * Validates that events_delivered in the request matches the transmitter's expected value.
     * <p>
     * SSF Spec 8.1.1.3 & 8.1.1.4: If events_delivered is present, it MUST match.
     *
     * @param request the incoming request
     * @param existing the existing stream configuration
     * @throws EventsDeliveredMismatchException if mismatch detected
     */
    private void validateEventsDelivered(StreamConfiguration request, StreamConfiguration existing) {
        if (request.getEvents_delivered() != null) {
            List<String> requestedDelivered = request.getEvents_delivered();
            List<String> expectedDelivered = existing.getEvents_delivered();

            if (expectedDelivered == null) {
                expectedDelivered = Collections.emptyList();
            }

            if (!new HashSet<>(requestedDelivered).equals(new HashSet<>(expectedDelivered))) {
                throw new EventsDeliveredMismatchException();
            }
        }
    }

    /**
     * Generates a unique stream identifier.
     * <p>
     * SSF Spec 8.1.1: Uses RFC3986 unreserved character set.
     *
     * @return a unique stream ID
     */
    private String generateStreamId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}