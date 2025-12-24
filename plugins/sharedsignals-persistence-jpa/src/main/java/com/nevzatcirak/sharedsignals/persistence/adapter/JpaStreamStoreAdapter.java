package com.nevzatcirak.sharedsignals.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.enums.SubjectStatus;
import com.nevzatcirak.sharedsignals.api.exception.*;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.model.StreamDelivery;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.persistence.entity.*;
import com.nevzatcirak.sharedsignals.persistence.repository.*;
import com.nevzatcirak.sharedsignals.persistence.util.SubjectHashUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JPA implementation of the {@link StreamStore} SPI.
 * <p>
 * This adapter bridges the Domain Layer's storage requirements with the JPA Persistence Layer.
 * It handles:
 * <ul>
 * <li>Entity-to-Model conversion.</li>
 * <li>Complex queries (e.g., finding streams by subject hash).</li>
 * <li>Transaction management.</li>
 * <li>JSON serialization of complex subjects.</li>
 * </ul>
 * </p>
 */
@Component
public class JpaStreamStoreAdapter implements StreamStore {

    private static final Logger log = LoggerFactory.getLogger(JpaStreamStoreAdapter.class);

    private final StreamRepository streamRepository;
    private final SubjectRepository subjectRepository;
    private final RemovedSubjectRepository removedSubjectRepository;
    private final StreamEventRepository streamEventRepository;
    private final SubjectHashUtil subjectHashUtil;
    private final ObjectMapper objectMapper;
    private final int subjectRemovalGracePeriodSeconds;

    public JpaStreamStoreAdapter(
            StreamRepository streamRepository,
            SubjectRepository subjectRepository,
            RemovedSubjectRepository removedSubjectRepository,
            StreamEventRepository streamEventRepository,
            SubjectHashUtil subjectHashUtil,
            ObjectMapper objectMapper,
            @Value("${sharedsignals.retention.subject-grace-period-seconds:604800}") int gracePeriodSeconds) {
        this.streamRepository = streamRepository;
        this.subjectRepository = subjectRepository;
        this.removedSubjectRepository = removedSubjectRepository;
        this.streamEventRepository = streamEventRepository;
        this.subjectHashUtil = subjectHashUtil;
        this.objectMapper = objectMapper;
        this.subjectRemovalGracePeriodSeconds = gracePeriodSeconds;
    }

    @Override
    @Transactional
    public void addSubject(String streamId, Map<String, Object> subject, boolean verified) {
        StreamEntity stream = streamRepository.findById(streamId).orElseThrow(() -> new StreamNotFoundException(streamId));
        String subjectHash = subjectHashUtil.computeHash(subject);
        if (subjectRepository.existsByStreamStreamIdAndSubjectHash(streamId, subjectHash)) {
            throw new SubjectAlreadyExistsException("Subject already exists in stream: " + streamId);
        }
        SubjectEntity subjectEntity = new SubjectEntity();
        subjectEntity.setStream(stream);
        subjectEntity.setSubjectPayload(serializeSubject(subject));
        subjectEntity.setSubjectHash(subjectHash);

        //Verified is ignored due to security concern (SSF 9.2. Information Harvesting)
        subjectEntity.setStatus(SubjectStatus.PENDING);

        subjectRepository.save(subjectEntity);
        log.info("Subject added to stream {} with status {}", streamId, subjectEntity.getStatus());
    }

    @Override
    @Transactional
    public void updateSubjectStatus(String streamId, String subjectHash, SubjectStatus status) {
        SubjectEntity subject = subjectRepository.findByStreamStreamIdAndSubjectHash(streamId, subjectHash)
                .orElseThrow(() -> new SsfBadRequestException("Subject not found or hash mismatch"));

        subject.setStatus(status);
        subjectRepository.save(subject);
        log.info("Subject status updated to {} for stream {}", status, streamId);
    }

    @Override
    @Transactional
    public void updateAuthorizedEvents(String streamId, Set<String> authorizedEvents) {
        StreamEntity stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        stream.setEventsAuthorized(new HashSet<>(authorizedEvents));

        Set<String> delivered = new HashSet<>(stream.getEventsRequested());
        delivered.retainAll(authorizedEvents);
        stream.setEventsDelivered(delivered);

        streamRepository.save(stream);
    }


    @Override
    @Transactional
    public StreamConfiguration save(StreamConfiguration model) {
        StreamEntity entity;
        if (model.getStream_id() != null) {
            entity = streamRepository.findById(model.getStream_id()).orElse(new StreamEntity());
            if (entity.getStreamId() == null) entity.setStreamId(model.getStream_id());
        } else {
            entity = new StreamEntity();
        }
        mergeToEntity(model, entity);
        return toModel(streamRepository.save(entity));
    }

    private void mergeToEntity(StreamConfiguration model, StreamEntity entity) {
        entity.setIssuer(model.getIss());
        if (model.getAud() != null) entity.setAudience(new HashSet<>(model.getAud()));
        else entity.getAudience().clear();

        entity.setDescription(model.getDescription());
        entity.setMinVerificationInterval(model.getMin_verification_interval());
        entity.setInactivityTimeout(model.getInactivity_timeout());

        if (model.getStatus() != null) entity.setStatus(model.getStatus());
        entity.setStatusReason(model.getReason());

        if (model.getDelivery() != null) {
            DeliveryEmbeddable delivery = entity.getDelivery();
            if (delivery == null) {
                delivery = new DeliveryEmbeddable();
                entity.setDelivery(delivery);
            }
            delivery.setMethod(model.getDelivery().getMethod());
            delivery.setEndpointUrl(model.getDelivery().getEndpoint_url());
            delivery.setAuthorizationHeader(model.getDelivery().getAuthorization_header());
        }

        if (model.getEvents_requested() != null) {
            entity.setEventsRequested(new HashSet<>(model.getEvents_requested()));
        } else {
            entity.getEventsRequested().clear();
        }

        if (entity.getEventsAuthorized().isEmpty() && model.getEvents_requested() != null) {
             entity.setEventsAuthorized(new HashSet<>(model.getEvents_requested()));
        }

        Set<String> intersection = new HashSet<>(entity.getEventsRequested());
        intersection.retainAll(entity.getEventsAuthorized());
        entity.setEventsDelivered(intersection);
    }

    @Override
    @Transactional
    public void updateStreamMode(String streamId, boolean processAllSubjects) {
        StreamEntity stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));
        stream.setProcessAllSubjects(processAllSubjects);
        streamRepository.save(stream);
        log.info("Stream {} processAllSubjects mode updated to {}", streamId, processAllSubjects);
    }

    @Override
    public Optional<StreamConfiguration> findById(String streamId) {
        return streamRepository.findById(streamId).map(this::toModel);
    }

    @Override
    @Transactional
    public void deleteById(String streamId) {
        streamRepository.deleteById(streamId);
    }

    @Override
    public List<StreamConfiguration> findByAudience(String audience) {
        return streamRepository.findByAudience(audience).stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public List<StreamConfiguration> findStreamsBySubject(Map<String, Object> subject) {
        String hash = subjectHashUtil.computeHash(subject);
        return streamRepository.findEnabledStreamsBySubjectHash(hash).stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeSubject(String streamId, Map<String, Object> subject) {
        String subjectHash = subjectHashUtil.computeHash(subject);

        SubjectEntity subjectEntity = subjectRepository
                .findByStreamStreamIdAndSubjectHash(streamId, subjectHash)
                .orElseThrow(() -> new StreamNotFoundException(
                        "Subject not found in stream: " + streamId));

        RemovedSubjectEntity removedSubject = new RemovedSubjectEntity();
        removedSubject.setStreamId(streamId);
        removedSubject.setSubjectHash(subjectHash);
        removedSubject.setSubjectPayload(subjectEntity.getSubjectPayload());
        removedSubject.setRemovedAt(Instant.now());
        removedSubject.setGracePeriodExpiresAt(
                Instant.now().plusSeconds(subjectRemovalGracePeriodSeconds)
        );

        removedSubjectRepository.save(removedSubject);
        subjectRepository.delete(subjectEntity);

        log.info("Subject removed from stream {} with grace period until {}",
                streamId, removedSubject.getGracePeriodExpiresAt());
    }

    @Override
    public boolean isSubjectInGracePeriod(String streamId, Map<String, Object> subject) {
        String subjectHash = subjectHashUtil.computeHash(subject);
        return removedSubjectRepository
                .findActiveGracePeriod(streamId, subjectHash, Instant.now())
                .isPresent();
    }

    @Override
    public void deleteByGracePeriodExpiresAtBefore(Instant expiryTime) {
        removedSubjectRepository.deleteByGracePeriodExpiresAtBefore(expiryTime);
    }

    @Override
    public boolean isSubjectRegistered(String streamId, Map<String, Object> subject) {
        return subjectRepository.existsByStreamStreamIdAndSubjectHash(streamId, subjectHashUtil.computeHash(subject));
    }

    @Override
    public void saveEvent(String streamId, String jti, String setToken) {
        log.debug("Saving event to buffer: stream={}, jti={}", streamId, jti);
        StreamEventEntity event = new StreamEventEntity();
        event.setStreamId(streamId);
        event.setJti(jti);
        event.setSetToken(setToken);

        streamEventRepository.save(event);

        log.info("Event buffered for POLL delivery: stream={}, jti={}", streamId, jti);
    }

    @Override
    public Map<String, String> fetchEvents(String streamId, int maxEvents) {
        log.debug("Fetching events from buffer: stream={}, maxEvents={}", streamId, maxEvents);
        List<StreamEventEntity> events = streamEventRepository.findUnacknowledgedEvents(
                streamId,
                Pageable.ofSize(maxEvents)
        );

        Map<String, String> result = new LinkedHashMap<>();
        for (StreamEventEntity event : events) {
            result.put(event.getJti(), event.getSetToken());
        }

        log.info("Fetched {} events for stream: {}", result.size(), streamId);
        return result;
    }

    @Override
    @Transactional
    public void acknowledgeEvents(String streamId, List<String> jtis) {
        if (jtis == null || jtis.isEmpty()) {
            return;
        }

        log.debug("Acknowledging events: stream={}, jtis={}", streamId, jtis);
        streamEventRepository.acknowledgeEvents(jtis, Instant.now());
        log.info("Acknowledged {} events for stream: {}", jtis.size(), streamId);

    }

    @Override
    public boolean hasMoreEvents(String streamId) {
        return countUnacknowledgedEvents(streamId) > 0;
    }

    @Override
    public void deleteByAcknowledgedTrueAndAcknowledgedAtBefore(Instant before) {
        streamEventRepository.deleteByAcknowledgedTrueAndAcknowledgedAtBefore(before);
    }

    @Override
    public long countUnacknowledgedEvents(String streamId) {
        return streamEventRepository.countByStreamIdAndAcknowledgedFalse(streamId);
    }

    @Override
    public long getEventCount() {
        return streamEventRepository.count();
    }

    private StreamConfiguration toModel(StreamEntity entity) {
        StreamConfiguration model = new StreamConfiguration();
        model.setStream_id(entity.getStreamId());
        model.setIss(entity.getIssuer());
        if (entity.getAudience() != null) model.setAud(new ArrayList<>(entity.getAudience()));
        model.setDescription(entity.getDescription());
        model.setMin_verification_interval(entity.getMinVerificationInterval());
        model.setInactivity_timeout(entity.getInactivityTimeout());

        model.setStatus(entity.getStatus());
        model.setReason(entity.getStatusReason());

        if (entity.getDelivery() != null) {
            StreamDelivery delivery = new StreamDelivery();
            delivery.setMethod(entity.getDelivery().getMethod());
            delivery.setEndpoint_url(entity.getDelivery().getEndpointUrl());
            delivery.setAuthorization_header(entity.getDelivery().getAuthorizationHeader());
            model.setDelivery(delivery);
        }


        if (entity.getEventsRequested() != null)
            model.setEvents_requested(new ArrayList<>(entity.getEventsRequested()));

        if (entity.getEventsDelivered() != null) {
            model.setEvents_delivered(new ArrayList<>(entity.getEventsDelivered()));
        } else {
            model.setEvents_delivered(new ArrayList<>());
        }

        model.setEvents_supported(SharedSignalConstants.SUPPORTED_EVENTS);
        return model;
    }

    private String serializeSubject(Map<String, Object> subject) {
        try {
            return objectMapper.writeValueAsString(subject);
        } catch (JsonProcessingException e) {
            throw new SsfInternalServerException("Failed to serialize subject: " + e.getMessage());
        }
    }
}