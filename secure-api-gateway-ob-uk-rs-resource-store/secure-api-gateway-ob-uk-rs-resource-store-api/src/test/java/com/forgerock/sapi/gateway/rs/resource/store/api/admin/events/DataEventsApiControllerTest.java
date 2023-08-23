/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessages;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessage;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;

/**
 * Test for {@link DataEventsApiController}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DataEventsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String DATA_URI = "/admin/data/events";

    private final String TPP_ID = UUID.randomUUID().toString();

    @LocalServerPort
    private int port;

    @Autowired
    private FREventMessageRepository frEventMessageRepository;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        frEventMessageRepository.deleteAll();
    }

    @Test
    public void shouldCreateNewEventMessages() {
        // Given
        final FREventMessages frEventMessages = aValidFREventMessages();
        // When
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());
    }

    @Test
    public void shouldUpdateMultipleEventMessages() {
        // Given
        final String set = "set-jwt-00000011222";

        FREventMessages frEventMessages = aValidFREventMessages(TPP_ID);
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());

        FREventMessages frEventMessagesUpdate = aValidFREventMessages(TPP_ID, frEventMessages.getEvents());
        frEventMessagesUpdate.getEvents().forEach(eventMessage -> eventMessage.setSet(set));

        // When
        ResponseEntity<FREventMessages> responseUpdate = restTemplate.exchange(dataUrl(), PUT, new HttpEntity<>(frEventMessagesUpdate), FREventMessages.class);
        // Then
        assertThat(responseUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(responseUpdate.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId()).isEqualTo(frEventMessagesUpdate.getTppId());
        assertThat(Objects.requireNonNull(responseUpdate.getBody()).getEvents()).isEqualTo(frEventMessagesUpdate.getEvents());

        // When
        ResponseEntity<FREventMessages> responseExport = restTemplate.getForEntity(dataUrl(frEventMessages.getTppId()), FREventMessages.class);
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExport.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId()).isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents()).containsExactlyInAnyOrderElementsOf(frEventMessages.getEvents());

    }

    @Test
    public void shouldExportEventMessages() {
        // Given
        // Creation of event messages for an api client (tpp id)
        FREventMessages frEventMessages = aValidFREventMessages(UUID.randomUUID().toString());
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());
        // creation of event message for another api client (tpp id)
        FREventMessages frEventMessages2 = aValidFREventMessages(UUID.randomUUID().toString());
        ResponseEntity<FREventMessages> response2 = restTemplate.postForEntity(dataUrl(), frEventMessages2, FREventMessages.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response2.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages2.getTppId());
        assertThat(Objects.requireNonNull(response2.getBody()).getEvents()).isEqualTo(frEventMessages2.getEvents());

        // When
        ResponseEntity<Collection<FREventMessages>> responseExport = restTemplate.exchange(dataUrl() + "/all", GET, null, new ParameterizedTypeReference<>() {
        });
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        Collection<FREventMessages> dataEvents = Objects.requireNonNull(responseExport.getBody());
        assertThat(dataEvents).containsExactly(frEventMessages, frEventMessages2);
    }

    @Test
    public void shouldExportEventMessagesByApiClient() {
        // Given
        FREventMessages frEventMessages = aValidFREventMessages(TPP_ID);
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());

        FREventMessages frEventMessages2 = aValidFREventMessages(TPP_ID);
        ResponseEntity<FREventMessages> response2 = restTemplate.postForEntity(dataUrl(), frEventMessages2, FREventMessages.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response2.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages2.getTppId());
        assertThat(Objects.requireNonNull(response2.getBody()).getEvents()).isEqualTo(frEventMessages2.getEvents());

        // When
        ResponseEntity<FREventMessages> responseExport = restTemplate.getForEntity(dataUrl(frEventMessages2.getTppId()), FREventMessages.class);
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExport.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages2.getTppId()).isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents()).containsAll(frEventMessages.getEvents()).containsAll(frEventMessages2.getEvents());
    }

    @Test
    public void shouldDeleteAllEventMessagesByApiClient() {
        // Given
        final FREventMessages frEventMessages = aValidFREventMessages();
        final ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());

        final ResponseEntity<Void> responseDelete = restTemplate.exchange(dataUrl(frEventMessages.getTppId()), DELETE, null, Void.class);
        // Then
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseDelete.getBody()).isNull();

        // When
        final ResponseEntity<FREventMessages> responseExport = restTemplate.getForEntity(dataUrl(frEventMessages.getTppId()), FREventMessages.class);
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExport.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId()).isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents()).isEmpty();
    }

    @Test
    public void shouldDeleteEventMessageByApiClientAndJti() {
        // Given
        final String jtiToDelete = UUID.randomUUID().toString();
        FREventMessages frEventMessages = aValidFREventMessages(TPP_ID);
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());
        // adding new event message for the same apiClient ID
        FREventMessages frEventMessages2 = aValidFREventMessages(
                TPP_ID,
                List.of(
                        FREventMessage.builder()
                                .jti(jtiToDelete)
                                .set("TEST-JWT-TO-DELETE-eyJ0eXAiOiJKV1QiLCJodHRwOi8vb3BlbmJhbmtpbmcub3J")
                                .build())
        );
        frEventMessages2.setTppId(frEventMessages.getTppId());
        ResponseEntity<FREventMessages> response2 = restTemplate.postForEntity(dataUrl(), frEventMessages2, FREventMessages.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response2.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(response2.getBody()).getEvents()).isEqualTo(frEventMessages2.getEvents());

        // When
        ResponseEntity<FREventMessages> responseExport = restTemplate.getForEntity(dataUrl(frEventMessages2.getTppId()), FREventMessages.class);
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExport.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages2.getTppId()).isEqualTo(frEventMessages.getTppId());
        List<FREventMessage> allEventMessages = new ArrayList<>();
        allEventMessages.addAll(frEventMessages.getEvents());
        allEventMessages.addAll(frEventMessages2.getEvents());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents().size()).isEqualTo(allEventMessages.size());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents()).containsExactlyInAnyOrderElementsOf(allEventMessages);

        // When
        ResponseEntity<Void> responseDelete = restTemplate.exchange(
                dataUrl(frEventMessages.getTppId(), jtiToDelete),
                DELETE,
                null,
                Void.class
        );
        // Then
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseDelete.getBody()).isNull();

        // When
        ResponseEntity<FREventMessages> responseExportAfterDelete = restTemplate.getForEntity(dataUrl(frEventMessages.getTppId()), FREventMessages.class);
        // Then
        assertThat(responseExportAfterDelete.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExportAfterDelete.getBody()).getTppId()).isNotNull().isEqualTo(frEventMessages.getTppId()).isEqualTo(frEventMessages.getTppId());
        assertThat(Objects.requireNonNull(responseExportAfterDelete.getBody()).getEvents()).containsOnlyOnceElementsOf(frEventMessages.getEvents());
    }

    private FREventMessages aValidFREventMessages() {
        return aValidFREventMessages(UUID.randomUUID().toString(), aValidEventMessageList());
    }

    private FREventMessages aValidFREventMessages(String apiClientId) {
        return aValidFREventMessages(apiClientId, aValidEventMessageList());
    }

    private FREventMessages aValidFREventMessages(String apiClientId, List<FREventMessage> eventMessages) {
        return FREventMessages.builder()
                .tppId(apiClientId)
                .events(eventMessages)
                .build();
    }

    private List<FREventMessage> aValidEventMessageList() {
        return List.of(
                FREventMessage.builder()
                        .jti(UUID.randomUUID().toString())
                        .set("TEST-JWT-01-eyJ0eXAiOiJKV1QiLCJodHRwOi8vb3BlbmJhbmtpbmcub3J")
                        .build(),
                FREventMessage.builder()
                        .jti(UUID.randomUUID().toString())
                        .set("TEST-JWT-02-eyJ0eXAiOiJKV1QiLCJodHRwOi8vb3BlbmJhbmtpbmcub3J")
                        .build(),
                FREventMessage.builder()
                        .jti(UUID.randomUUID().toString())
                        .set("TEST-JWT-03-eyJ0eXAiOiJKV1QiLCJodHRwOi8vb3BlbmJhbmtpbmcub3J")
                        .build()
        );
    }

    private String dataUrl(String tppId) {
        return dataUrl() + "?tppId=" + tppId;
    }

    private String dataUrl(String tppId, String jti) {
        return dataUrl(tppId) + "&jti=" + jti;
    }

    private String dataUrl() {
        return BASE_URL + port + DATA_URI;
    }
}
