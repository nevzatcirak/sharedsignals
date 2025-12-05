package com.nevzatcirak.sharedsignals.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nevzatcirak.sharedsignals.api.constant.SecurityConstants;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.service.PushQueueService;
import com.nevzatcirak.sharedsignals.boot.config.TestSecurityConfig;
import com.nevzatcirak.sharedsignals.persistence.entity.StreamEntity;
import com.nevzatcirak.sharedsignals.persistence.repository.PushMessageRepository;
import com.nevzatcirak.sharedsignals.persistence.repository.StreamEventRepository;
import com.nevzatcirak.sharedsignals.persistence.repository.StreamRepository;
import com.nevzatcirak.sharedsignals.web.scheduler.PushDeliveryScheduler;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void testFullLifecycle_PushAndPoll() throws Exception {
        // ... (Previous Steps Omitted for Brevity - Assume same setup until Step 7) ...

        // 1. CREATE STREAM (PUSH)
        String receiverUrl = "http://localhost:" + wireMockServer.port() + "/receiver/events";
        Map<String, Object> createRequest = new HashMap<>();
        Map<String, Object> delivery = new HashMap<>();
        delivery.put("method", SharedSignalConstants.DELIVERY_METHOD_PUSH);
        delivery.put("endpoint_url", receiverUrl);
        createRequest.put("delivery", delivery);
        createRequest.put("events_requested", List.of(SharedSignalConstants.RISC_ACCOUNT_DISABLED));

        MvcResult createResult = mockMvc.perform(post("/ssf/stream")
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        StreamConfiguration createdStream = objectMapper.readValue(responseJson, StreamConfiguration.class);
        String streamId = createdStream.getStream_id();

        StreamEntity streamEntity = streamRepository.findById(streamId).orElseThrow();
        streamEntity.setMinVerificationInterval(0);
        streamRepository.save(streamEntity);

        // 2. ADD SUBJECT
        Map<String, Object> addSubjectRequest = new HashMap<>();
        addSubjectRequest.put("stream_id", streamId);
        addSubjectRequest.put("subject", Map.of("format", "email", "email", "user@example.com"));
        mockMvc.perform(post("/ssf/subject/add")
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSubjectRequest)))
                .andExpect(status().isOk());

        // 3. TRIGGER VERIFICATION (PUSH OUTBOX)
        Map<String, Object> verifyRequest = new HashMap<>();
        verifyRequest.put("stream_id", streamId);
        verifyRequest.put("state", "test-state-123");
        mockMvc.perform(post("/ssf/verification")
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isNoContent());

        // 4. RUN SCHEDULER
        pushDeliveryScheduler.processOutbox();
        verify(1, postRequestedFor(urlEqualTo("/receiver/events")));

        // 5. UPDATE TO POLL
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

        // 6. TRIGGER VERIFICATION (POLL BUFFER)
        verifyRequest.put("state", "poll-test-state");
        mockMvc.perform(post("/ssf/verification")
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isNoContent());

        await().atMost(2, TimeUnit.SECONDS).until(() -> streamEventRepository.count() > 0);

        // ==========================================
        // 7. POLL EVENTS (ASYNC FIX)
        // ==========================================
        Map<String, Object> pollRequest = new HashMap<>();
        pollRequest.put("maxEvents", 5);
        pollRequest.put("returnImmediately", true);

        // Step 7a: Initiate Async Request
        MvcResult mvcResult = mockMvc.perform(post("/ssf/events/poll/" + streamId)
                        .requestAttr(SecurityConstants.ATTRIBUTE_CLIENT_ID, CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pollRequest)))
                .andExpect(request().asyncStarted()) // Verify async started
                .andReturn();

        // Step 7b: Dispatch Async Result (Wait for completion)
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets").isMap())
                .andExpect(jsonPath("$.sets").isNotEmpty());

        System.out.println("TEST SUCCESS: Full Push/Poll Lifecycle Verified!");
    }
}
