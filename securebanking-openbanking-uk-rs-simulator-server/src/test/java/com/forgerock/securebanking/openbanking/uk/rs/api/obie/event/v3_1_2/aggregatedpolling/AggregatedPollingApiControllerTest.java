/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.event.v3_1_2.aggregatedpolling;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.event.FREventNotification;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.events.FRPendingEventsRepository;
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
import uk.org.openbanking.datamodel.event.OBEventPolling1;
import uk.org.openbanking.datamodel.event.OBEventPollingResponse1;

import java.util.List;
import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredEventHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

/**
 * A SpringBoot test for the {@link AggregatedPollingApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class AggregatedPollingApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String EVENTS_URI = "/open-banking/v3.1.2/events";

    @LocalServerPort
    private int port;

    @Autowired
    private FRPendingEventsRepository pendingEventsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        pendingEventsRepository.deleteAll();
    }

    @Test
    public void shouldPollEvents() {
        // Given
        String tppId = UUID.randomUUID().toString();
        FREventNotification frEventNotification = aValidFREventNotificationBuilder(tppId).build();
        pendingEventsRepository.save(frEventNotification);
        OBEventPolling1 obEventPolling = aValidOBEventPolling1();
        HttpHeaders headers = requiredEventHttpHeaders(eventsUrl(), tppId);
        HttpEntity<OBEventPolling1> request = new HttpEntity<>(obEventPolling, headers);

        // When
        ResponseEntity<OBEventPollingResponse1> response = restTemplate.postForEntity(eventsUrl(), request, OBEventPollingResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().isMoreAvailable()).isFalse();
        assertThat(response.getBody().getSets().get(frEventNotification.getJti())).isEqualTo(frEventNotification.getSignedJwt());
    }

    @Test
    public void shouldAcknowledgeEvents() {
        // Given
        String tppId = UUID.randomUUID().toString();
        FREventNotification frEventNotification = aValidFREventNotificationBuilder(tppId).build();
        pendingEventsRepository.save(frEventNotification);
        OBEventPolling1 obEventPolling = aValidOBEventPolling1();
        // set the ACK for acknowledgement
        obEventPolling.setAck(List.of(frEventNotification.getJti()));
        HttpHeaders headers = requiredEventHttpHeaders(eventsUrl(), tppId);
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

    private FREventNotification.FREventNotificationBuilder aValidFREventNotificationBuilder(String tppId) {
        return FREventNotification.builder()
                .id(UUID.randomUUID().toString())
                .jti(UUID.randomUUID().toString())
                .signedJwt("eyJraWQiOiJkOGFiMzI4N2QxZTI4MDc0NDFjMWM4Yjc0MGNjYWQ3MTBiMjM2MDI4IiwiYWxnIjoiUFMyNTYifQ.eyJhdWQiOiI3dW14NW5UUjMzODExUXlRZmkiLCJzdWIiOiJodHRwczpcL1wvZXhhbXBsZWJhbmsuY29tXC9hcGlcL29wZW4tYmFua2luZ1wvdjMuMS4yXC9waXNwXC9kb21lc3RpYy1wYXltZW50c1wvcG10LTcyOTAtMDAzIiwiaXNzIjoiaHR0cHM6XC9cL2FzLmFzcHNwLnNhbmRib3gubGxveWRzYmFua2luZy5jb21cL29hdXRoMiIsInR4biI6ImRmYzUxNjI4LTM0NzktNGI4MS1hZDYwLTIxMGI0M2QwMjMwNiIsInRvZSI6MTUxNjIzOTAyMiwiaWF0IjoxNjE2NTk2NTg1LCJqdGkiOiJkYzY0OTkzMy0zMDc3LTRhZGItOGFjNy0xYmRjODA1Y2M2MTEiLCJldmVudHMiOnsidXJuOnVrOm9yZzpvcGVuYmFua2luZzpldmVudHM6cmVzb3VyY2UtdXBkYXRlIjp7InN1YmplY3QiOnsiaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9yaWQiOiJwbXQtNzI5MC0wMDMiLCJzdWJqZWN0X3R5cGUiOiJodHRwOlwvXC9vcGVuYmFua2luZy5vcmcudWtcL3JpZF9odHRwOlwvXC9vcGVuYmFua2luZy5vcmcudWtcL3J0eSIsImh0dHA6XC9cL29wZW5iYW5raW5nLm9yZy51a1wvcmxrIjpbeyJsaW5rIjoiaHR0cHM6XC9cL2V4YW1wbGViYW5rLmNvbVwvYXBpXC9vcGVuLWJhbmtpbmdcL3YzLjEuMlwvcGlzcFwvZG9tZXN0aWMtcGF5bWVudHNcL3BtdC03MjkwLTAwMyIsInZlcnNpb24iOiIzLjEuMiJ9XSwiaHR0cDpcL1wvb3BlbmJhbmtpbmcub3JnLnVrXC9ydHkiOiJkb21lc3RpYy1wYXltZW50In19fX0.kgaGq6mN3Gso7er_bKXLQF0cTc4LtHKaVRErtTOhIYzLds2af8NKPhCHcqs74epEfYb_IZc8onKJEUpjiCKDKCipLyzHUD2DFxPd3BCTVAlP1eXDTDuWSa5ZwcrINEwNfeBLbyqOp1oTS5VUJ_ld9d7ovERr271DipZ4OXTC5AR04T2BzK4GlU4ekjqOaYulVD3GLWNZuFfoVXeyPPr9t-q1SPZLGmR_e3kRETxn_v32JzIQx8iN8ACOkOvMEXYA_mKhbSiwj4i_9_O9lOYBJo2BQClfC5vcxrnNSmg5tu0V-nYw-4q4IajwhNKBIbO7yZhS6y7QWXPgeUbZEv3luA")
                .tppId(tppId);
    }

    private OBEventPolling1 aValidOBEventPolling1() {
        return new OBEventPolling1()
                .maxEvents(1)
                .returnImmediately(true);
    }
}