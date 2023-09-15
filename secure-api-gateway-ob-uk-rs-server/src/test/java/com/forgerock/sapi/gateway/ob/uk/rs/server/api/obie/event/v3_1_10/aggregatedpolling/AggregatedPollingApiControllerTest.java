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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.v3_1_10.aggregatedpolling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
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

    @LocalServerPort
    private int port;

    @Autowired
    private FREventMessageRepository pendingEventsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        pendingEventsRepository.deleteAll();
    }

    @Test
    public void shouldPollEvents() {
        // Given
        String apiClientId = UUID.randomUUID().toString();
        FREventMessageEntity frEventMessageEntity = aValidFREventNotificationEntityBuilder(apiClientId).build();
        pendingEventsRepository.save(frEventMessageEntity);
        OBEventPolling1 obEventPolling = aValidOBEventPolling1();
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventNotificationsHttpHeaders(apiClientId);
        HttpEntity<OBEventPolling1> request = new HttpEntity<>(obEventPolling, headers);

        // When
        ResponseEntity<OBEventPollingResponse1> response = restTemplate.postForEntity(eventsUrl(), request, OBEventPollingResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().isMoreAvailable()).isFalse();
        assertThat(response.getBody().getSets().get(frEventMessageEntity.getJti())).isEqualTo(frEventMessageEntity.getSet());
    }

    @Test
    public void shouldAcknowledgeEvents() {
        // Given
        String apiClientId = UUID.randomUUID().toString();
        FREventMessageEntity frEventMessageEntity = aValidFREventNotificationEntityBuilder(apiClientId).build();
        pendingEventsRepository.save(frEventMessageEntity);
        OBEventPolling1 obEventPolling = aValidOBEventPolling1();
        // set the ACK for acknowledgement
        obEventPolling.setAck(List.of(frEventMessageEntity.getJti()));
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventNotificationsHttpHeaders(apiClientId);
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
        String apiClientId = UUID.randomUUID().toString();
        FREventMessageEntity frEventMessageEntity = aValidFREventNotificationEntityBuilder(apiClientId).build();
        FREventMessageEntity frEventMessageEntityErr = aValidFREventNotificationEntityBuilder(apiClientId).build();
        pendingEventsRepository.save(frEventMessageEntity);
        OBEventPolling1 obEventPolling = aValidOBEventPolling1();
        // set the ACK for acknowledgement
        obEventPolling.setAck(List.of(frEventMessageEntity.getJti()));
        obEventPolling.putSetErrsItem(
                frEventMessageEntityErr.getJti(),
                new OBEventPolling1SetErrs().err("jwtIss").description("Issuer is invalid or could not be verified")
        );
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventNotificationsHttpHeaders(apiClientId);
        HttpEntity<OBEventPolling1> request = new HttpEntity<>(obEventPolling, headers);

        // When
        ResponseEntity<OBEventPollingResponse1> response = restTemplate.postForEntity(eventsUrl(), request, OBEventPollingResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().isMoreAvailable()).isNull();
        assertThat(response.getBody().getSets()).isEmpty();
    }

    private String eventsUrl() {
        return BASE_URL + port + EVENTS_URI;
    }

    private FREventMessageEntity.FREventMessageEntityBuilder aValidFREventNotificationEntityBuilder(String apiClientId) {
        return FREventMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .jti(UUID.randomUUID().toString())
                .set("eyJraWQiOiJkOGFiMzI4N2QxZTI4MDc0NDFjMWM4Yjc0MGNjYWQ3MTBiMjM2MDI4IiwiYWxnIjoiUFMyNTYifQ.eyJhdWQiOiI3dW14NW5UUjMzODExUXlRZmkiLCJzdWIiOiJodHRwczpcL1wvZXhhbXBsZWJhbmsuY29tXC9hcGlcL29wZW4tYmFua2luZ1wvdjMuMS4yXC9waXNwXC9kb21lc3RpYy1wYXltZW50c1wvcG10LTcyOTAtMDAzIiwiaXNzIjoiaHR0cHM6XC9cL2FzLmFzcHNwLnNhbmRib3gubGxveWRzYmFua2luZy5jb21cL29hdXRoMiIsInR4biI6ImRmYzUxNjI4LTM0NzktNGI4MS1hZDYwLTIxMGI0M2QwMjMwNiIsInRvZSI6MTUxNjIzOTAyMiwiaWF0IjoxNjE2NTk2NTg1LCJqdGkiOiJkYzY0OTkzMy0zMDc3LTRhZGItOGFjNy0xYmRjODA1Y2M2MTEiLCJldmVudHMiOnsidXJuOnVrOm9yZzpvcGVuYmFua2luZzpldmVudHM6cmVzb3VyY2UtdXBkYXRlIjp7InN1YmplY3QiOnsiaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9yaWQiOiJwbXQtNzI5MC0wMDMiLCJzdWJqZWN0X3R5cGUiOiJodHRwOlwvXC9vcGVuYmFua2luZy5vcmcudWtcL3JpZF9odHRwOlwvXC9vcGVuYmFua2luZy5vcmcudWtcL3J0eSIsImh0dHA6XC9cL29wZW5iYW5raW5nLm9yZy51a1wvcmxrIjpbeyJsaW5rIjoiaHR0cHM6XC9cL2V4YW1wbGViYW5rLmNvbVwvYXBpXC9vcGVuLWJhbmtpbmdcL3YzLjEuMlwvcGlzcFwvZG9tZXN0aWMtcGF5bWVudHNcL3BtdC03MjkwLTAwMyIsInZlcnNpb24iOiIzLjEuMiJ9XSwiaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9ydHkiOiJkb21lc3RpYy1wYXltZW50In19fX0.kgaGq6mN3Gso7er_bKXLQF0cTc4LtHKaVRErtTOhIYzLds2af8NKPhCHcqs74epEfYb_IZc8onKJEUpjiCKDKCipLyzHUD2DFxPd3BCTVAlP1eXDTDuWSa5ZwcrINEwNfeBLbyqOp1oTS5VUJ_ld9d7ovERr271DipZ4OXTC5AR04T2BzK4GlU4ekjqOaYulVD3GLWNZuFfoVXeyPPr9t-q1SPZLGmR_e3kRETxn_v32JzIQx8iN8ACOkOvMEXYA_mKhbSiwj4i_9_O9lOYBJo2BQClfC5vcxrnNSmg5tu0V-nYw-4q4IajwhNKBIbO7yZhS6y7QWXPgeUbZEv3luA")
                .apiClientId(apiClientId);
    }

    private OBEventPolling1 aValidOBEventPolling1() {
        return new OBEventPolling1()
                .maxEvents(1)
                .returnImmediately(true);
    }
}