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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventMessages;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;

import uk.org.openbanking.datamodel.v3.event.OBEvent1;
import uk.org.openbanking.datamodel.v3.event.OBEventLink1;
import uk.org.openbanking.datamodel.v3.event.OBEventNotification1;
import uk.org.openbanking.datamodel.v3.event.OBEventResourceUpdate1;
import uk.org.openbanking.datamodel.v3.event.OBEventSubject1;

/**
 * Test for {@link DataEventsApiController}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {"rs.data.upload.limit.events=10"})
@AutoConfigureWebClient(registerRestTemplate = true)
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
        assertThat(Objects.requireNonNull(response.getBody()).getObEventNotification1List()).isEqualTo(frEventMessages.getObEventNotification1List());
    }

    @Test
    public void shouldExceedLimitAllowed() {
        // Given
        List<OBEventNotification1> eventNotification1List = new ArrayList<>();
        IntStream.range(1, eventsLimit + 2).forEach(i -> eventNotification1List.add(aValidOBEventNotification1()));

        final FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, eventNotification1List);
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class),
                HttpClientErrorException.class
        );
        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains(
                String.format(
                        "The number of events provided in the payload (%d) exceeded maximum limit of %s",
                        frEventMessages.getObEventNotification1List().size(),
                        eventsLimit
                )
        );
    }

    @Test
    public void shouldExceedLimitStoredAllowed() {
        // Given
        List<OBEventNotification1> eventNotification1List = new ArrayList<>();
        IntStream.range(1, eventsLimit + 1).forEach(i -> eventNotification1List.add(aValidOBEventNotification1()));

        final FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, eventNotification1List);
        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class),
                HttpClientErrorException.class
        );
        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains(
                String.format(
                        "Cannot add events as it the stored events in the system has reached the maximum limit of %s, current events in the system %d",
                        eventsLimit,
                        eventsLimit
                )
        );
    }

    @Test
    public void shouldExceedTotalEventsAllowed() {
        // Given
        int totalEvents = 8;
        List<OBEventNotification1> eventNotification1List = new ArrayList<>();
        IntStream.range(1, totalEvents + 1).forEach(i -> eventNotification1List.add(aValidOBEventNotification1()));

        final FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, eventNotification1List);
        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class),
                HttpClientErrorException.class
        );
        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains(
                String.format("Cannot add events as it will exceeded maximum limit of %s, current events in the system %d",
                        eventsLimit,
                        totalEvents
                )
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
        int totalEvents = 3;
        List<OBEventNotification1> eventNotification1List = new ArrayList<>();
        IntStream.range(1, totalEvents + 1).forEach(i -> eventNotification1List.add(aValidOBEventNotification1()));

        FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, eventNotification1List);
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getObEventNotification1List()).isEqualTo(frEventMessages.getObEventNotification1List());

        FREventMessages frEventMessagesUpdate = aValidFREventMessages(API_CLIENT_ID, eventNotification1List);
        // to test the update set the jti value as txn
        frEventMessagesUpdate.getObEventNotification1List().forEach(obEventNotification1 -> obEventNotification1.txn(obEventNotification1.getJti()));

        // When
        ResponseEntity<FREventMessages> responseUpdate = restTemplate.exchange(dataUrl(), PUT, new HttpEntity<>(frEventMessagesUpdate), FREventMessages.class);
        // Then
        assertThat(responseUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(responseUpdate.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId()).isEqualTo(frEventMessagesUpdate.getApiClientId());
        assertThat(Objects.requireNonNull(responseUpdate.getBody()).getObEventNotification1List()).isEqualTo(frEventMessagesUpdate.getObEventNotification1List());
        validateEventsStored(frEventMessages, totalEvents);
    }

    @Test
    public void shouldExportEventMessagesByApiClient() {
        // Given
        int totalEvents = 3;
        List<OBEventNotification1> eventNotification1List = new ArrayList<>();
        IntStream.range(1, totalEvents + 1).forEach(i -> eventNotification1List.add(aValidOBEventNotification1()));
        FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, eventNotification1List);
        // When
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getObEventNotification1List()).isEqualTo(frEventMessages.getObEventNotification1List());
        // Then
        validateEventsStored(frEventMessages, totalEvents);
    }

    @Test
    public void shouldDeleteAllEventMessagesByApiClient() {
        // Given
        final FREventMessages frEventMessages = aValidFREventMessages();
        final ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getObEventNotification1List()).isEqualTo(frEventMessages.getObEventNotification1List());
        // When
        final ResponseEntity<Void> responseDelete = restTemplate.exchange(dataUrl(frEventMessages.getApiClientId()), DELETE, null, Void.class);
        // Then
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseDelete.getBody()).isNull();
        validateEventsStored(frEventMessages, 0);
    }

    @Test
    public void shouldDeleteEventMessageByApiClientAndJti() {
        // Given
//        final String jtiToDelete = UUID.randomUUID().toString();
        OBEventNotification1 obEventNotification1 = aValidOBEventNotification1();
        OBEventNotification1 obEventNotification1_2 = aValidOBEventNotification1();
        FREventMessages frEventMessages = aValidFREventMessages(API_CLIENT_ID, List.of(obEventNotification1, obEventNotification1_2));
        ResponseEntity<FREventMessages> response = restTemplate.postForEntity(dataUrl(), frEventMessages, FREventMessages.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getApiClientId()).isNotNull().isEqualTo(frEventMessages.getApiClientId());
        assertThat(Objects.requireNonNull(response.getBody()).getObEventNotification1List()).isEqualTo(frEventMessages.getObEventNotification1List());
        // When
        ResponseEntity<Void> responseDelete = restTemplate.exchange(
                dataUrl(frEventMessages.getApiClientId(), obEventNotification1.getJti()),
                DELETE,
                null,
                Void.class
        );
        // Then
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseDelete.getBody()).isNull();
        validateEventsStored(frEventMessages, 1);
    }

    private void validateEventsStored(FREventMessages frEventMessages, int totalEvents) {
        ParameterizedTypeReference<List<FREventMessageEntity>> typeReference = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<List<FREventMessageEntity>> responseExport = restTemplate.exchange(
                dataUrl(frEventMessages.getApiClientId()),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                typeReference);

        assertThat(responseExport.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(responseExport.getBody()).size()).isEqualTo(totalEvents);

        responseExport.getBody().forEach(e -> {
            assertThat(e.getApiClientId()).isEqualTo(frEventMessages.getApiClientId());
            assertThat(e.getEvents()).isEqualTo(
                    frEventMessages.getObEventNotification1List().stream()
                            .filter(obEventNotification1 -> obEventNotification1.getJti().equals(e.getJti()))
                            .findAny().get().getEvents()
            );
        });
    }

    private FREventMessages aValidFREventMessages() {
        return aValidFREventMessages(UUID.randomUUID().toString(), aValidOBEventNotification1List());
    }

    private FREventMessages aValidFREventMessages(String apiClientId) {
        return aValidFREventMessages(apiClientId, aValidOBEventNotification1List());
    }

    private OBEventNotification1 aValidOBEventNotification1() {
        return aValidOBEventNotification1(UUID.randomUUID().toString());
    }

    private OBEventNotification1 aValidOBEventNotification1(String jti) {
        return new OBEventNotification1()
                .aud("7umx5nTR33811QyQfi")
                .iat(1516239022)
                .iss("https://examplebank.com/")
                .jti(jti)
                .sub(URI.create("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003"))
                .toe(1516239022)
                .txn("dfc51628-3479-4b81-ad60-210b43d02306")
                .events(new OBEvent1().urnColonUkColonOrgColonOpenbankingColonEventsColonResourceUpdate(
                                new OBEventResourceUpdate1()
                                        .subject(
                                                new OBEventSubject1()
                                                        .subjectType("http://openbanking.org.uk/rid_http://openbanking.org.uk/rty")
                                                        .httpColonOpenbankingOrgUkRid("pmt-7290-003")
                                                        .httpColonOpenbankingOrgUkRlk(
                                                                List.of(
                                                                        new OBEventLink1()
                                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                                .version("v3.1.5"),
                                                                        new OBEventLink1()
                                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                                .version("v3.1.10")
                                                                )
                                                        )
                                                        .httpColonOpenbankingOrgUkRty("domestic-payment")
                                        )
                        )
                );
    }

    private List<OBEventNotification1> aValidOBEventNotification1List() {
        return List.of(aValidOBEventNotification1());
    }

    private FREventMessages aValidFREventMessages(String apiClientId, List<OBEventNotification1> obEventNotification1List) {
        return FREventMessages.builder()
                .apiClientId(apiClientId)
                .obEventNotification1List(obEventNotification1List)
                .build();
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
