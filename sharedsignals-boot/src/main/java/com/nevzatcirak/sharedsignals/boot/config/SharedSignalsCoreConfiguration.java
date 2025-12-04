package com.nevzatcirak.sharedsignals.boot.config;

import com.nevzatcirak.sharedsignals.api.service.*;
import com.nevzatcirak.sharedsignals.core.service.impl.*;
import com.nevzatcirak.sharedsignals.api.spi.EventSender;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring configuration that registers Core POJOs as Spring Beans.
 */
@Configuration
public class SharedSignalsCoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StreamConfigurationService streamConfigurationService(
            StreamStore streamStore,
            @Value("${sharedsignals.issuer}") String issuerUrl,
            @Value("${sharedsignals.defaults.inactivity-timeout:2592000}") int defaultInactivityTimeout,
            @Value("${sharedsignals.features.allow-multiple-streams-per-receiver:false}") boolean allowMultipleStreams,
            @Value("${sharedsignals.defaults.max-description-length:255}") int maxDescriptionLength) {
        return new DefaultStreamConfigurationService(
                streamStore,
                issuerUrl,
                defaultInactivityTimeout,
                allowMultipleStreams,
                maxDescriptionLength
        );
    }

    @Bean
    public SubjectManagementService subjectManagementService(StreamStore streamStore) {
        return new DefaultSubjectManagementService(streamStore);
    }

    @Bean
    public TransmitterMetadataService transmitterMetadataService(
            @Value("${sharedsignals.issuer}") String issuer) {
        return new DefaultTransmitterMetadataService(issuer);
    }

    @Bean
    public DefaultJwkSetService defaultJwkSetService() {
        return new DefaultJwkSetService();
    }

    @Bean
    public JwkSetService jwkSetService(DefaultJwkSetService service) {
        return service;
    }

    @Bean
    public TokenSigningService tokenSigningService(
            @Value("${sharedsignals.issuer}") String issuer,
            DefaultJwkSetService jwkSetService) throws Exception {

        DefaultTokenSigningService signer = new DefaultTokenSigningService(issuer, jwkSetService);
        signer.setSigningKey(jwkSetService.getRsaKey());
        return signer;
    }

    @Bean
    public EventPublisherService eventPublisherService(
            StreamStore streamStore,
            TokenSigningService signingService,
            EventSender eventSender) {
        return new DefaultEventPublisherService(streamStore, signingService, eventSender);
    }

    @Bean
    public StreamStatusService streamStatusService(StreamStore streamStore) {
        return new DefaultStreamStatusService(streamStore);
    }

    @Bean
    public VerificationService verificationService(EventPublisherService eventPublisher, StreamStore streamStore) {
        return new DefaultVerificationService(eventPublisher, streamStore);
    }

    @Bean
    public EventRetrievalService eventRetrievalService(StreamStore streamStore) {
        return new DefaultEventRetrievalService(streamStore);
    }
}
