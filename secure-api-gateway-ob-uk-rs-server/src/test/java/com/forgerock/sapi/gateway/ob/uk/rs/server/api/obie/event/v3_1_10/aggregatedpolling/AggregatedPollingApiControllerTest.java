/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.v3_1_10.aggregatedpolling;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.EventTestHelper.aValidFREventMessageEntity;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.EventTestHelper.aValidOBEventPolling1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;

import uk.org.openbanking.datamodel.event.OBEventPolling1;
import uk.org.openbanking.datamodel.event.OBEventPolling1SetErrs;
import uk.org.openbanking.datamodel.event.OBEventPollingResponse1;

/**
 * A SpringBoot test for the {@link AggregatedPollingApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class AggregatedPollingApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String EVENTS_URI = "/open-banking/v3.1.10/events";

    private static final String API_CLIENT_ID = UUID.randomUUID().toString();

    @LocalServerPort
    private int port;

    @Autowired
    private FREventMessageRepository pendingEventsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private ObjectMapper objectMapper;

    @AfterEach
    void removeData() {
        pendingEventsRepository.deleteAll();
    }

    public AggregatedPollingApiControllerTest() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
    }

    @Test
    public void shouldPollEvents() throws Exception {
        // Given
        String apiClientId = UUID.randomUUID().toString();
        FREventMessageEntity frEventMessageEntity = aValidFREventMessageEntity(API_CLIENT_ID);
        pendingEventsRepository.save(frEventMessageEntity);
        OBEventPolling1 obEventPolling = aValidOBEventPolling1();
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventNotificationsHttpHeaders(API_CLIENT_ID);
        HttpEntity<OBEventPolling1> request = new HttpEntity<>(obEventPolling, headers);

        // When
        ResponseEntity<OBEventPollingResponse1> response = restTemplate.postForEntity(eventsUrl(), request, OBEventPollingResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().isMoreAvailable()).isFalse();
        validateStringSet(response.getBody().getSets().get(frEventMessageEntity.getJti()), frEventMessageEntity);
    }

    @Test
    public void shouldAcknowledgeEvents() {
        // Given
        String apiClientId = UUID.randomUUID().toString();
        FREventMessageEntity frEventMessageEntity = aValidFREventMessageEntity(API_CLIENT_ID);
        pendingEventsRepository.save(frEventMessageEntity);
        OBEventPolling1 obEventPolling = aValidOBEventPolling1();
        // set the ACK for acknowledgement
        obEventPolling.setAck(List.of(frEventMessageEntity.getJti()));
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventNotificationsHttpHeaders(API_CLIENT_ID);
        HttpEntity<OBEventPolling1> request = new HttpEntity<>(obEventPolling, headers);

        // When
        ResponseEntity<OBEventPollingResponse1> response = restTemplate.postForEntity(eventsUrl(), request, OBEventPollingResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().isMoreAvailable()).isNull();
        assertThat(response.getBody().getSets()).isEmpty();
    }

    @Test
    public void shouldPollAndAcknowledgeEvents() {
        // Given
        FREventMessageEntity frEventMessageEntity = aValidFREventMessageEntity(API_CLIENT_ID);
        FREventMessageEntity frEventMessageEntityErr = aValidFREventMessageEntity(API_CLIENT_ID);
        pendingEventsRepository.save(frEventMessageEntity);
        OBEventPolling1 obEventPolling = aValidOBEventPolling1();
        // set the ACK for acknowledgement
        obEventPolling.setAck(List.of(frEventMessageEntity.getJti()));
        obEventPolling.putSetErrsItem(
                frEventMessageEntityErr.getJti(),
                new OBEventPolling1SetErrs().err("jwtIss").description("Issuer is invalid or could not be verified")
        );
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventNotificationsHttpHeaders(API_CLIENT_ID);
        HttpEntity<OBEventPolling1> request = new HttpEntity<>(obEventPolling, headers);

        // When
        ResponseEntity<OBEventPollingResponse1> response = restTemplate.postForEntity(eventsUrl(), request, OBEventPollingResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().isMoreAvailable()).isNull();
        assertThat(response.getBody().getSets()).isEmpty();
    }

    private void validateStringSet(String set, FREventMessageEntity frEventMessageEntity) throws Exception {
        Map setMap = objectMapper.readValue(set, Map.class);
        Map<String, Object> entityMap = objectMapper.convertValue(frEventMessageEntity, new TypeReference<>() {
        });
        assertThat(setMap.entrySet()).isSubsetOf(entityMap.entrySet());
    }

    private String eventsUrl() {
        return BASE_URL + port + EVENTS_URI;
    }


}