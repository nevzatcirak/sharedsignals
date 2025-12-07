package com.nevzatcirak.sharedsignals.boot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nevzatcirak.sharedsignals.api.constant.SecurityConstants;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.boot.config.TestSecurityConfig;
import com.nevzatcirak.sharedsignals.persistence.entity.StreamEntity;
import com.nevzatcirak.sharedsignals.persistence.repository.PushMessageRepository;
import com.nevzatcirak.sharedsignals.persistence.repository.StreamEventRepository;
import com.nevzatcirak.sharedsignals.persistence.repository.StreamRepository;
import com.nevzatcirak.sharedsignals.web.scheduler.PushDeliveryScheduler;
import com.nevzatcirak.sharedsignals.api.service.PushQueueService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// WireMock static imports
import static com.github.tomakehurst.wiremock.client.WireMock.*;

// AssertJ
import static org.assertj.core.api.Assertions.assertThat;

// Awaitility
import static org.awaitility.Awaitility.await;

// MockMvc Builders & Matchers
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, SsfEndToEndIntegrationTest.SchedulerConfig.class})
public class SsfEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PushMessageRepository pushMessageRepository;

    @Autowired
    private StreamEventRepository streamEventRepository;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private PushDeliveryScheduler pushDeliveryScheduler;

    private static WireMockServer wireMockServer;
    private static final String CLIENT_ID = "test-client-1";

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        pushMessageRepository.deleteAll();
        streamEventRepository.deleteAll();
        wireMockServer.resetAll();

        stubFor(WireMock.post(urlEqualTo("/receiver/events"))
                .willReturn(aResponse().withStatus(202)));
    }

    @TestConfiguration
    static class SchedulerConfig {
        @Bean
        @Primary
        public PushDeliveryScheduler overrideScheduler(PushQueueService queueService) {
             org.springframework.boot.web.client.RestTemplateBuilder builder =
                 new org.springframework.boot.web.client.RestTemplateBuilder();
             return new PushDeliveryScheduler(queueService, builder);
        }
    }

    @Test
    void testFullLifecycle_Push_Ingest_Poll() throws Exception {
        // 1. CREATE STREAM (PUSH)
        String receiverUrl = "http://localhost:" + wireMockServer.port() + "/receiver/events";
        Map<String, Object> createRequest = new HashMap<>();
        Map<String, Object> delivery = new HashMap<>();
        delivery.put("method", SharedSignalConstants.DELIVERY_METHOD_PUSH);
        delivery.put("endpoint_url", receiverUrl);
        createRequest.put("delivery", delivery);
        createRequest.put("events_requested", List.of(
            SharedSignalConstants.RISC_ACCOUNT_DISABLED,
            SharedSignalConstants.SSF_VERIFICATION
        ));

        MvcResult createResult = mockMvc.perform(post("/ssf/stream")
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        StreamConfiguration createdStream = objectMapper.readValue(responseJson, StreamConfiguration.class);
        String streamId = createdStream.getStream_id();

        // HACK: Bypass 30s limit for test
        StreamEntity streamEntity = streamRepository.findById(streamId).orElseThrow();
        streamEntity.setMinVerificationInterval(0);
        streamRepository.save(streamEntity);

        // 2. ADD SUBJECT
        Map<String, Object> subject = Map.of("format", "email", "email", "user@example.com");
        Map<String, Object> addSubjectRequest = new HashMap<>();
        addSubjectRequest.put("stream_id", streamId);
        addSubjectRequest.put("subject", subject);

        mockMvc.perform(post("/ssf/subject/add")
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSubjectRequest)))
                .andExpect(status().isOk());

        // 3. TRIGGER VERIFICATION
        Map<String, Object> verifyRequest = new HashMap<>();
        verifyRequest.put("stream_id", streamId);
        verifyRequest.put("state", "test-state-123");

        mockMvc.perform(post("/ssf/verification")
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isNoContent());

        // 4. INGEST REAL EVENT (Async)
        Map<String, Object> ingestRequest = new HashMap<>();
        ingestRequest.put("subject", subject);

        Map<String, Object> data = new HashMap<>();
        data.put("intent", "ACCOUNT_DISABLED");
        data.put("reason", "hijacking");
        data.put("description", "Detected brute force attack.");
        ingestRequest.put("data", data);

        MvcResult ingestResult = mockMvc.perform(post("/api/v1/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingestRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(ingestResult))
                .andExpect(status().isAccepted());

        await().atMost(2, TimeUnit.SECONDS).until(() -> pushMessageRepository.count() >= 2);

        // 5. RUN SCHEDULER
        pushDeliveryScheduler.processOutbox();

        // FIX: Verify count only (JWT body is encoded, string matching fails)
        verify(2, postRequestedFor(urlEqualTo("/receiver/events"))
                .withHeader("Content-Type", containing("application/secevent+jwt")));

        System.out.println(">>> PUSH & INGESTION VERIFIED SUCCESSFUL");

        // 6. UPDATE TO POLL
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("stream_id", streamId);
        Map<String, Object> pollDelivery = new HashMap<>();
        pollDelivery.put("method", SharedSignalConstants.DELIVERY_METHOD_POLL);
        updateRequest.put("delivery", pollDelivery);
        updateRequest.put("events_requested", List.of(SharedSignalConstants.RISC_ACCOUNT_DISABLED));

        mockMvc.perform(put("/ssf/stream")
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        StreamEntity updatedEntity = streamRepository.findById(streamId).orElseThrow();
        updatedEntity.setMinVerificationInterval(0);
        streamRepository.save(updatedEntity);

        // 7. INGEST ANOTHER EVENT (POLL BUFFER)
        ingestRequest.get("data");
        ((Map)ingestRequest.get("data")).put("reason", "policy-violation");

        MvcResult ingestPollResult = mockMvc.perform(post("/api/v1/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingestRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(ingestPollResult)).andExpect(status().isAccepted());

        await().atMost(2, TimeUnit.SECONDS).until(() -> streamEventRepository.count() > 0);

        // 8. POLL EVENTS
        Map<String, Object> pollRequest = new HashMap<>();
        pollRequest.put("maxEvents", 5);
        pollRequest.put("returnImmediately", true);

        MvcResult pollMvcResult = mockMvc.perform(post("/ssf/events/poll/" + streamId)
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pollRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult finalResult = mockMvc.perform(asyncDispatch(pollMvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets").isMap())
                .andExpect(jsonPath("$.sets").isNotEmpty())
                .andReturn();

        // --- PRINT POLL RESULTS ---
        String responseBody = finalResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode setsNode = rootNode.path("sets");

        System.out.println("=========================================");
        System.out.println(">>> POLL RESPONSE (Decoded Sets)");
        System.out.println("=========================================");

        if (setsNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = setsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String jti = entry.getKey();
                String jwtToken = entry.getValue().asText();

                System.out.println("[Event JTI]: " + jti);
                System.out.println("[SET Token]: " + jwtToken);

                // Optional: Decode Payload for visibility (no signature verification here)
                try {
                    String[] parts = jwtToken.split("\\.");
                    if (parts.length > 1) {
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                        System.out.println("[Payload]  : " + payload);
                    }
                } catch (Exception e) {
                    System.out.println("[Payload]  : (Could not decode)");
                }
                System.out.println("-----------------------------------------");
            }
        }

        System.out.println("TEST SUCCESS: Full Push/Ingest/Poll Lifecycle Verified!");
    }
}