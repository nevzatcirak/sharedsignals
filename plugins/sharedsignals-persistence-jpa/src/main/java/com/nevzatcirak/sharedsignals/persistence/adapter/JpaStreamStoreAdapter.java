package com.nevzatcirak.sharedsignals.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.exception.*;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.model.StreamDelivery;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.persistence.entity.DeliveryEmbeddable;
import com.nevzatcirak.sharedsignals.persistence.entity.StreamEntity;
import com.nevzatcirak.sharedsignals.persistence.entity.StreamEventEntity;
import com.nevzatcirak.sharedsignals.persistence.entity.SubjectEntity;
import com.nevzatcirak.sharedsignals.persistence.repository.StreamEventRepository;
import com.nevzatcirak.sharedsignals.persistence.repository.StreamRepository;
import com.nevzatcirak.sharedsignals.persistence.repository.SubjectRepository;
import com.nevzatcirak.sharedsignals.persistence.util.SubjectHashUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JPA-based implementation of StreamStore SPI.
 */
@Component
public class JpaStreamStoreAdapter implements StreamStore {

    private final StreamRepository streamRepository;
    private final SubjectRepository subjectRepository;
    private final StreamEventRepository eventRepository;
    private final SubjectHashUtil subjectHashUtil;
    private final ObjectMapper objectMapper;


    public JpaStreamStoreAdapter(StreamRepository streamRepository,
                                 SubjectRepository subjectRepository,
                                 StreamEventRepository eventRepository,
                                 SubjectHashUtil subjectHashUtil,
                                 ObjectMapper objectMapper) {
        this.streamRepository = streamRepository;
        this.subjectRepository = subjectRepository;
        this.eventRepository = eventRepository;
        this.subjectHashUtil = subjectHashUtil;
        this.objectMapper = objectMapper;
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
        entity.setInactivityTimeout(model.getInactivity_timeout()); // New Field

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

        if (model.getEvents_delivered() != null) {
            entity.setEventsDelivered(new HashSet<>(model.getEvents_delivered()));
        }
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
    public void addSubject(String streamId, Map<String, Object> subject, boolean verified) {
        StreamEntity stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        String subjectHash = subjectHashUtil.computeHash(subject);

        if (subjectRepository.existsByStreamStreamIdAndSubjectHash(streamId, subjectHash)) {
            throw new SubjectAlreadyExistsException("Subject already exists in stream: " + streamId);
        }

        SubjectEntity subjectEntity = new SubjectEntity();
        subjectEntity.setStream(stream);
        subjectEntity.setSubjectPayload(serializeSubject(subject));
        subjectEntity.setSubjectHash(subjectHash);
        subjectEntity.setVerified(verified);

        subjectRepository.save(subjectEntity);
    }

    @Override
    @Transactional
    public void removeSubject(String streamId, Map<String, Object> subject) {
        String hash = subjectHashUtil.computeHash(subject);
        subjectRepository.findByStreamStreamIdAndSubjectHash(streamId, hash).ifPresent(subjectRepository::delete);
    }

    @Override
    public boolean isSubjectRegistered(String streamId, Map<String, Object> subject) {
        return subjectRepository.existsByStreamStreamIdAndSubjectHash(streamId, subjectHashUtil.computeHash(subject));
    }

    @Override
    @Transactional
    public void saveEvent(String streamId, String jti, String setToken) {
        StreamEventEntity entity = new StreamEventEntity();
        entity.setStreamId(streamId);
        entity.setJti(jti);
        entity.setSetToken(setToken);
        eventRepository.save(entity);
    }

    @Override
    public Map<String, String> fetchEvents(String streamId, int maxEvents) {
        int limit = (maxEvents <= 0) ? 10 : maxEvents;
        List<StreamEventEntity> events = eventRepository.findByStreamIdOrderByCreatedAtAsc(streamId, PageRequest.of(0, limit));
        return events.stream().collect(Collectors.toMap(StreamEventEntity::getJti, StreamEventEntity::getSetToken));
    }

    @Override
    @Transactional
    public void acknowledgeEvents(String streamId, List<String> jtis) {
        if (jtis != null && !jtis.isEmpty()) eventRepository.deleteByStreamIdAndJtiIn(streamId, jtis);
    }

    @Override
    public boolean hasMoreEvents(String streamId) {
        return eventRepository.countByStreamId(streamId) > 0;
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

        if (entity.getEventsRequested() != null) {
            model.setEvents_requested(new ArrayList<>(entity.getEventsRequested()));
        }

        if (entity.getEventsDelivered() != null && !entity.getEventsDelivered().isEmpty()) {
            model.setEvents_delivered(new ArrayList<>(entity.getEventsDelivered()));
        } else if (entity.getEventsRequested() != null) {
            model.setEvents_delivered(new ArrayList<>(entity.getEventsRequested()));
        }

        //TODO: Supported events should be configurable by the transmitter for real use cases
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
