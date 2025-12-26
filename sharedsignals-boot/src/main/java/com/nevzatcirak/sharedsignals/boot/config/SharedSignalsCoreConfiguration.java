package com.nevzatcirak.sharedsignals.boot.config;

import com.nevzatcirak.sharedsignals.api.service.*;
import com.nevzatcirak.sharedsignals.api.spi.*;
import com.nevzatcirak.sharedsignals.core.mapper.SecurityEventMapper;
import com.nevzatcirak.sharedsignals.core.privacy.DefaultPrivacyPolicyValidator;
import com.nevzatcirak.sharedsignals.core.service.impl.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedSignalsCoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StreamConfigurationService streamConfigurationService(
            StreamStore streamStore,
            @Value("${sharedsignals.issuer}") String issuerUrl,
            @Value("${sharedsignals.defaults.inactivity-timeout:2592000}") int defaultInactivityTimeout,
            @Value("${sharedsignals.defaults.min-verification-interval:1}") int defaultMinVerificationInterval,
            @Value("${sharedsignals.features.multi-stream-per-receiver:false}") boolean allowMultipleStreams,
            @Value("${sharedsignals.defaults.max-description-length:255}") int maxDescriptionLength,
            InactivityTimeoutService inactivityService)
    {
        return new DefaultStreamConfigurationService(
                streamStore, issuerUrl, defaultInactivityTimeout, defaultMinVerificationInterval,
                allowMultipleStreams, maxDescriptionLength, inactivityService
        );
    }

    @Bean
    public SubjectManagementService subjectManagementService(StreamStore streamStore) {
        return new DefaultSubjectManagementService(streamStore);
    }

    @Bean
    public TransmitterMetadataService transmitterMetadataService(@Value("${sharedsignals.issuer}") String issuer) {
        return new DefaultTransmitterMetadataService(issuer);
    }

    @Bean
    public DefaultJwkSetService jwkSetService(
            @Value("${sharedsignals.security.signing-key:#{null}}") String signingKey,
            @Value("${sharedsignals.security.signing-key-id:ssf-key-1}") String keyId) {
        return new DefaultJwkSetService(signingKey, keyId);
    }

    @Bean
    public TokenSigningService tokenSigningService(
            @Value("${sharedsignals.issuer}") String issuer,
            DefaultJwkSetService jwkSetService) throws Exception {
        DefaultTokenSigningService signer = new DefaultTokenSigningService(issuer);
        signer.setSigningKey(jwkSetService.getRsaKey());
        return signer;
    }

    @Bean
    public EventPublisherService eventPublisherService(
            StreamStore streamStore, TokenSigningService signingService,
            EventSender eventSender, PrivacyPolicyValidator privacyValidator) {
        return new DefaultEventPublisherService(streamStore, signingService, eventSender, privacyValidator);
    }

    @Bean
    public StreamStatusService streamStatusService(StreamStore streamStore, EventPublisherService eventPublisher) {
        return new DefaultStreamStatusService(streamStore, eventPublisher);
    }

    @Bean
    public VerificationService verificationService(EventPublisherService eventPublisher, StreamStore streamStore) {
        return new DefaultVerificationService(eventPublisher, streamStore);
    }

    @Bean
    public EventRetrievalService eventRetrievalService(StreamStore streamStore) {
        return new DefaultEventRetrievalService(streamStore);
    }

    @Bean
    public InactivityTimeoutService inactivityTimeoutService(StreamStore streamStore, StreamStatusService streamStatusService) {
        return new DefaultInactivityTimeoutService(streamStore, streamStatusService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PrivacyPolicyValidator privacyPolicyValidator() {
        return new DefaultPrivacyPolicyValidator();
    }

    @Bean
    public PushQueueService pushQueueService(PushQueueStore pushQueueStore) {
        return new DefaultPushQueueService(pushQueueStore);
    }

    @Bean
    public SecurityEventMapper securityEventMapper() {
        return new SecurityEventMapper();
    }

    @Bean
    public EventIngestionService eventIngestionService(EventPublisherService eventPublisherService, SecurityEventMapper mapper) {
        return new DefaultEventIngestionService(eventPublisherService, mapper);
    }

    @Bean
    public StreamAdministrationService streamAdministrationService(StreamStore streamStore) {
        return new DefaultStreamAdministrationService(streamStore);
    }
}
