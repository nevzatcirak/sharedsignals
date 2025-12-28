package com.nevzatcirak.sharedsignals.boot.config;

import com.nevzatcirak.sharedsignals.adapter.rest.model.*;
import com.nevzatcirak.sharedsignals.adapter.rest.model.subjects.*;
import com.nevzatcirak.sharedsignals.api.enums.SecurityIntent;
import com.nevzatcirak.sharedsignals.api.enums.SubjectStatus;
import com.nevzatcirak.sharedsignals.api.model.*;
import com.nevzatcirak.sharedsignals.web.model.*;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Registers reflection hints for GraalVM Native Image.
 * Essential for Jackson serialization/deserialization of DTOs, Enums, and Polymorphic types.
 */
@Configuration
@ImportRuntimeHints(SsfNativeRuntimeHints.class)
public class SsfNativeRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // =================================================================================
        // 1. ENUMS (Custom Serializers/Deserializers need access)
        // =================================================================================
        registerReflection(hints, SubjectStatus.class);
        registerReflection(hints, SecurityIntent.class);

        // =================================================================================
        // 2. CORE API MODELS (Used in Service & Controller Returns)
        // =================================================================================
        registerReflection(hints, StreamConfiguration.class);
        registerReflection(hints, StreamDelivery.class);
        registerReflection(hints, TransmitterMetadata.class);
        registerReflection(hints, TransmitterMetadata.AuthorizationScheme.class);
        registerReflection(hints, GenericSecurityEvent.class);
        registerReflection(hints, PollResult.class);
        registerReflection(hints, PollCommand.class);
        registerReflection(hints, PollCommand.PollError.class);
        registerReflection(hints, RateLimitResult.class);
        registerReflection(hints, StreamStatus.class);

        // =================================================================================
        // 3. REST ADAPTER MODELS (Ingestion Payload)
        // =================================================================================
        registerReflection(hints, RestIngestRequest.class);
        registerReflection(hints, RestEventPayload.class);

        // *** POLYMORPHIC SUBJECTS (CRITICAL) ***
        // Jackson needs to see the Interface and ALL implementations to handle the "format" discriminator.
        registerReflection(hints, RestSubject.class);
        registerReflection(hints, RestEmailSubject.class);
        registerReflection(hints, RestPhoneSubject.class);
        registerReflection(hints, RestIssSubSubject.class);
        registerReflection(hints, RestOpaqueSubject.class);
        registerReflection(hints, RestAccountSubject.class);
        registerReflection(hints, RestDidSubject.class);
        registerReflection(hints, RestUriSubject.class);
        registerReflection(hints, RestIpSubject.class);
        registerReflection(hints, RestJwtIdSubject.class);
        registerReflection(hints, RestSamlAssertionIdSubject.class);
        registerReflection(hints, RestComplexSubject.class);
        registerReflection(hints, RestAliasesSubject.class);

        // =================================================================================
        // 4. WEB MODULE DTOs (Controller Request/Response Bodies)
        // =================================================================================
        registerReflection(hints, StreamModeRequest.class);
        registerReflection(hints, UpdateAuthorizedEventsRequest.class);
        registerReflection(hints, UpdateSubjectStatusRequest.class);
        registerReflection(hints, SSFAddSubjectRequest.class);
        registerReflection(hints, SSFRemoveSubjectRequest.class);
        registerReflection(hints, SSFVerificationRequest.class);
        registerReflection(hints, SSFPollRequest.class);
        registerReflection(hints, SSFPollResponse.class);
    }

    private void registerReflection(RuntimeHints hints, Class<?> clazz) {
        hints.reflection().registerType(clazz, MemberCategory.values());
    }
}
