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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.v3_1_2.eventsubscription;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventSubscription;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.EventSubscriptionsRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
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
import uk.org.openbanking.datamodel.event.*;

import java.util.List;

import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion.v3_1_2;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * A SpringBoot test for the {@link EventSubscriptionApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class EventSubscriptionApiControllerTest {
    private static final String BASE_URL = "http://localhost:";
    private static final String EVENT_SUBSCRIPTIONS_URI = "/open-banking/v3.1.2/event-subscriptions";

    @LocalServerPort
    private int port;

    @Autowired
    private EventSubscriptionsRepository eventSubscriptionsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        eventSubscriptionsRepository.deleteAll();
    }

    @Test
    public void shouldCreateEventSubscription() {
        // Given
        OBEventSubscription1 eventSubscription = aValidOBEventSubscription1();
        String tppId = randomUUID().toString();
        HttpEntity<OBEventSubscription1> request = new HttpEntity<>(eventSubscription, HttpHeadersTestDataFactory.requiredEventHttpHeaders(eventSubscriptionsUrl(), tppId));

        // When
        ResponseEntity<OBEventSubscriptionResponse1> response = restTemplate.postForEntity(eventSubscriptionsUrl(), request, OBEventSubscriptionResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        OBEventSubscriptionResponse1Data responseData = response.getBody().getData();
        assertThat(responseData.getCallbackUrl()).isEqualTo(eventSubscription.getData().getCallbackUrl());
        assertThat(responseData.getVersion()).isEqualTo(eventSubscription.getData().getVersion());
        assertThat(responseData.getEventTypes()).isEqualTo(eventSubscription.getData().getEventTypes());
        assertThat(response.getBody().getMeta()).isNotNull();
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(eventSubscriptionsIdUrl(responseData.getEventSubscriptionId()));
    }

    @Test
    public void shouldReadEventSubscription() {
        // Given
        OBEventSubscription1 eventSubscription = aValidOBEventSubscription1();
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventHttpHeaders(eventSubscriptionsUrl(), randomUUID().toString());
        HttpEntity<OBEventSubscription1> request = new HttpEntity<>(eventSubscription, headers);
        restTemplate.postForEntity(eventSubscriptionsUrl(), request, OBEventSubscriptionResponse1.class);

        // When
        ResponseEntity<OBEventSubscriptionsResponse1> response = restTemplate.exchange(eventSubscriptionsUrl(), GET, new HttpEntity<>(headers), OBEventSubscriptionsResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        List<OBEventSubscriptionsResponse1DataEventSubscription> eventSubscriptions = response.getBody().getData().getEventSubscription();
        assertThat(eventSubscriptions).isNotEmpty();
        assertThat(eventSubscriptions.get(0).getCallbackUrl()).isEqualTo(eventSubscription.getData().getCallbackUrl());
        assertThat(eventSubscriptions.get(0).getVersion()).isEqualTo(eventSubscription.getData().getVersion());
        assertThat(eventSubscriptions.get(0).getEventTypes()).isEqualTo(eventSubscription.getData().getEventTypes());
        assertThat(response.getBody().getMeta()).isNotNull();
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(eventSubscriptionsUrl());
    }

    @Test
    public void shouldUpdateEventSubscription() {
        // Given
        OBEventSubscription1 eventSubscription = aValidOBEventSubscription1();
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventHttpHeaders(eventSubscriptionsUrl(), randomUUID().toString());
        HttpEntity<OBEventSubscription1> createRequest = new HttpEntity<>(eventSubscription, headers);
        ResponseEntity<OBEventSubscriptionResponse1> createResponse = restTemplate.postForEntity(eventSubscriptionsUrl(), createRequest, OBEventSubscriptionResponse1.class);
        String updateUrl = eventSubscriptionsIdUrl(createResponse.getBody().getData().getEventSubscriptionId());
        OBEventSubscriptionResponse1 updatedSubscription = new OBEventSubscriptionResponse1().data(createResponse.getBody().getData());
        updatedSubscription.getData().setCallbackUrl("http://updatedcallbackurl.com");
        HttpEntity<OBEventSubscriptionResponse1> updateRequest = new HttpEntity<>(updatedSubscription, headers);

        // When
        ResponseEntity<OBEventSubscriptionResponse1> response = restTemplate.exchange(updateUrl, PUT, updateRequest, OBEventSubscriptionResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        OBEventSubscriptionResponse1Data responseData = response.getBody().getData();
        assertThat(responseData.getCallbackUrl()).isEqualTo("http://updatedcallbackurl.com");
        assertThat(response.getBody().getMeta()).isNotNull();
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(updateUrl);
    }

    @Test
    public void shouldDeleteEventSubscription() {
        // Given
        OBEventSubscription1 eventSubscription = aValidOBEventSubscription1();
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredEventHttpHeaders(eventSubscriptionsUrl(), randomUUID().toString());
        HttpEntity<OBEventSubscription1> createRequest = new HttpEntity<>(eventSubscription, headers);
        ResponseEntity<OBEventSubscriptionResponse1> createResponse = restTemplate.postForEntity(eventSubscriptionsUrl(), createRequest, OBEventSubscriptionResponse1.class);
        String deleteUrl = eventSubscriptionsIdUrl(createResponse.getBody().getData().getEventSubscriptionId());

        // When
        restTemplate.exchange(deleteUrl, DELETE, new HttpEntity<>(headers), Void.class);

        // Then
        List<FREventSubscription> callbacks = eventSubscriptionsRepository.findAll();
        assertThat(callbacks).isEmpty();
    }

    private String eventSubscriptionsUrl() {
        return BASE_URL + port + EVENT_SUBSCRIPTIONS_URI;
    }

    private String eventSubscriptionsIdUrl(String id) {
        return eventSubscriptionsUrl() + "/" + id;
    }

    private OBEventSubscription1 aValidOBEventSubscription1() {
        return new OBEventSubscription1()
                .data(new OBEventSubscription1Data()
                        .callbackUrl("http://callbackurl.com")
                        .version(v3_1_2.getCanonicalName())
                        .eventTypes(List.of("urn:uk:org:openbanking:events:resource-update"))
                );
    }
}