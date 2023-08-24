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
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessage;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessages;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;

/**
 * Test for {@link DataEventsApiController}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {"rs.data.upload.limit.events=10"})
public class DataEventsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String DATA_URI = "/admin/data/events";

    private final String API_CLIENT_ID = UUID.randomUUID().toString();

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

    public final Integer eventsLimit;
    public DataEventsApiControllerTest(@Value("${rs.data.upload.limit.events}") Integer eventsLimit) {
        this.eventsLimit = eventsLimit;
    }

    @Test
    public void shouldCreateEventMessages() {
        // Given
        final FREventMessages frEventMessages = aValidFREventMessages();
        // When
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());
    }

    @Test
    public void shouldExceedLimitAllowed() {
        // Given
        List<FREventMessage> frEventMessageList = new ArrayList<>();
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        final FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, frEventMessageList);
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class),
                HttpClientErrorException.class
        );
        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains(
                String.format("The number of events provided in the payload (%d) exceeded maximum limit of %s", frEventMessageList.size(), eventsLimit)
        );
    }

    @Test
    public void shouldExceedLimitStoredAllowed() {
        // Given
        List<FREventMessage> frEventMessageList = new ArrayList<>();
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        final FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, frEventMessageList);
        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        final FREventMessages moreFrEventMessages = aValidFREventMessages(API_CLIENT_ID);
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), moreFrEventMessages, FREventMessages.class),
                HttpClientErrorException.class
        );
        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains(
                String.format("Cannot add events as it the stored events in the system has reached the maximum limit of %s, current events in the system %d", eventsLimit, frEventMessageList.size())
        );
    }

    @Test
    public void shouldExceedTotalEventsAllowed() {
        // Given
        List<FREventMessage> frEventMessageList = new ArrayList<>();
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        frEventMessageList.addAll(aValidEventMessageList());
        final FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, frEventMessageList);
        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        final FREventMessages moreFrEventMessages = aValidFREventMessages(API_CLIENT_ID, frEventMessageList);
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), moreFrEventMessages, FREventMessages.class),
                HttpClientErrorException.class
        );
        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains(
                String.format("Cannot add events as it will exceeded maximum limit of %s, current events in the system %d", eventsLimit, frEventMessageList.size())
        );
    }

    @Test
    public void shouldRaisedAndErrorWhenApiClientIdIsNull() {
        // Given
        final FREventMessages frEventMessages = aValidFREventMessages(null);
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class),
                HttpClientErrorException.class
        );
        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("The apiClientId cannot be null or empty");
    }

    @Test
    public void shouldRaisedAndErrorWhenApiClientIdIsEmpty() {
        // Given
        final FREventMessages frEventMessages = aValidFREventMessages("");
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class),
                HttpClientErrorException.class
        );
        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("The apiClientId cannot be null or empty");
    }

    @Test
    public void shouldUpdateMultipleEventMessages() {
        // Given
        final String set = "set-jwt-00000011222";

        FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID);
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());

        FREventMessages frEventMessagesUpdate = aValidFREventMessages(API_CLIENT_ID, frEventMessages.getEvents());
        frEventMessagesUpdate.getEvents().forEach(eventMessage -> eventMessage.setSet(set));

        // When
        ResponseEntity<FREventMessages> responseUpdate = restTemplate.exchange(dataUrl(), PUT, new HttpEntity<>(frEventMessagesUpdate), FREventMessages.class);
        // Then
        assertThat(responseUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(responseUpdate.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId()).isEqualTo(frEventMessagesUpdate.getApiClientId());
        assertThat(Objects.requireNonNull(responseUpdate.getBody()).getEvents()).isEqualTo(frEventMessagesUpdate.getEvents());

        // When
        ResponseEntity<FREventMessages> responseExport = restTemplate.getForEntity(dataUrl(frEventMessages.getApiClientId()), FREventMessages.class);
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExport.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId()).isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents()).containsExactlyInAnyOrderElementsOf(frEventMessages.getEvents());
    }

    @Test
    public void shouldExportEventMessagesByApiClient() {
        // Given
        FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID);
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());

        FREventMessages frEventMessages2 = aValidFREventMessages(API_CLIENT_ID);
        ResponseEntity<FREventMessages> response2 = restTemplate.postForEntity(dataUrl(), frEventMessages2, FREventMessages.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response2.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages2.getApiClientId());
        assertThat(Objects.requireNonNull(response2.getBody()).getEvents()).isEqualTo(frEventMessages2.getEvents());

        // When
        ResponseEntity<FREventMessages> responseExport = restTemplate.getForEntity(dataUrl(frEventMessages2.getApiClientId()), FREventMessages.class);
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExport.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages2.getApiClientId()).isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents()).containsAll(frEventMessages.getEvents()).containsAll(frEventMessages2.getEvents());
    }

    @Test
    public void shouldDeleteAllEventMessagesByApiClient() {
        // Given
        final FREventMessages frEventMessages = aValidFREventMessages();
        final ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());

        final ResponseEntity<Void> responseDelete = restTemplate.exchange(dataUrl(frEventMessages.getApiClientId()), DELETE, null, Void.class);
        // Then
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseDelete.getBody()).isNull();

        // When
        final ResponseEntity<FREventMessages> responseExport = restTemplate.getForEntity(dataUrl(frEventMessages.getApiClientId()), FREventMessages.class);
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExport.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId()).isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents()).isEmpty();
    }

    @Test
    public void shouldDeleteEventMessageByApiClientAndJti() {
        // Given
        final String jtiToDelete = UUID.randomUUID().toString();
        FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID);
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getEvents()).isEqualTo(frEventMessages.getEvents());
        // adding new event message for the same apiClient ID
        FREventMessages frEventMessages2 = aValidFREventMessages(
                API_CLIENT_ID,
                List.of(
                        FREventMessage.builder()
                                .jti(jtiToDelete)
                                .set("TEST-JWT-TO-DELETE-eyJ0eXAiOiJKV1QiLCJodHRwOi8vb3BlbmJhbmtpbmcub3J")
                                .build())
        );
        frEventMessages2.setApiClientId(frEventMessages.getApiClientId());
        ResponseEntity<FREventMessages> response2 = restTemplate.postForEntity(dataUrl(), frEventMessages2, FREventMessages.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response2.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response2.getBody()).getEvents()).isEqualTo(frEventMessages2.getEvents());

        // When
        ResponseEntity<FREventMessages> responseExport = restTemplate.getForEntity(dataUrl(frEventMessages2.getApiClientId()), FREventMessages.class);
        // Then
        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExport.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages2.getApiClientId()).isEqualTo(frEventMessages.getApiClientId());
        List<FREventMessage> allEventMessages = new ArrayList<>();
        allEventMessages.addAll(frEventMessages.getEvents());
        allEventMessages.addAll(frEventMessages2.getEvents());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents().size()).isEqualTo(allEventMessages.size());
        assertThat(Objects.requireNonNull(responseExport.getBody()).getEvents()).containsExactlyInAnyOrderElementsOf(allEventMessages);

        // When
        ResponseEntity<Void> responseDelete = restTemplate.exchange(
                dataUrl(frEventMessages.getApiClientId(), jtiToDelete),
                DELETE,
                null,
                Void.class
        );
        // Then
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseDelete.getBody()).isNull();

        // When
        ResponseEntity<FREventMessages> responseExportAfterDelete = restTemplate.getForEntity(dataUrl(frEventMessages.getApiClientId()), FREventMessages.class);
        // Then
        assertThat(responseExportAfterDelete.getStatusCode()).isEqualTo(HttpStatus.OK);
        // An apiClient with a collection of SETs
        assertThat(Objects.requireNonNull(responseExportAfterDelete.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId()).isEqualTo(frEventMessages.getApiClientId());
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
                .apiClientId(apiClientId)
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
                        .build()
        );
    }

    private String dataUrl(String apiClientId) {
        return dataUrl() + "?apiClientId=" + apiClientId;
    }

    private String dataUrl(String apiClientId, String jti) {
        return dataUrl(apiClientId) + "&jti=" + jti;
    }

    private String dataUrl() {
        return BASE_URL + port + DATA_URI;
    }
}
