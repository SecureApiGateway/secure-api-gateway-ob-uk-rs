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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.event;

import static com.forgerock.sapi.gateway.rs.resource.store.api.admin.events.FRDataEventsConverter.toOBEventNotification1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPolling;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPollingError;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import uk.org.openbanking.datamodel.event.OBEvent1;
import uk.org.openbanking.datamodel.event.OBEventLink1;
import uk.org.openbanking.datamodel.event.OBEventResourceUpdate1;
import uk.org.openbanking.datamodel.event.OBEventSubject1;

/**
 * Unit test for {@link EventPollingService}.
 */
@ExtendWith(MockitoExtension.class)
public class EventPollingServiceTest {
    private static final String API_CLIENT_ID = "abc123";
    private static final List<String> JWT_ID_LIST = ImmutableList.of("jti-01", "jti02");
    // value of ${rs.data.upload.limit.events:10}
    private static final int EVENTS_LIMIT = 10;
    @Mock
    private FREventMessageRepository mockRepo;

    @Spy
    private ObjectMapper objectMapper;

    private EventPollingService eventPollingService;

    @BeforeEach
    public void setUp() {
        eventPollingService = new EventPollingService(mockRepo, objectMapper, EVENTS_LIMIT);
    }

    @Test
    public void acknowledgeEvents_findAndDeleteTwoEvents_ignoreEventNotfound() {
        // Given

        FREventPolling pollingRequest = FREventPolling.builder()
                .ack(ImmutableList.of(JWT_ID_LIST.get(0), JWT_ID_LIST.get(1), "NotFound"))
                .build();

        // When
        eventPollingService.acknowledgeEvents(pollingRequest, API_CLIENT_ID);

        // Then
        verify(mockRepo).deleteByApiClientIdAndJti(eq(API_CLIENT_ID), eq(JWT_ID_LIST.get(0)));
        verify(mockRepo).deleteByApiClientIdAndJti(eq(API_CLIENT_ID), eq(JWT_ID_LIST.get(1)));
    }

    @Test
    public void acknowledgeEvents_emptyList() {
        // Given
        FREventPolling pollingRequest = FREventPolling.builder()
                .ack(Collections.emptyList())
                .build();

        // When
        eventPollingService.acknowledgeEvents(pollingRequest, API_CLIENT_ID);

        // Then
        verifyNoMoreInteractions(mockRepo);
    }

    @Test
    public void acknowledgeEvents_nullList() {
        // Given
        FREventPolling pollingRequest = FREventPolling.builder()
                .ack(null)
                .build();

        // When
        eventPollingService.acknowledgeEvents(pollingRequest, API_CLIENT_ID);

        // Then
        verifyNoMoreInteractions(mockRepo);
    }

    @Test
    public void recordTppEventErrors_notificationExists_addError() {
        // Given
        FREventMessageEntity existingNotification = aValidFREventMessageEntity(JWT_ID_LIST.get(0));
        FREventPolling pollingRequest = FREventPolling.builder()
                .setErrs(
                        Collections.singletonMap(JWT_ID_LIST.get(1),
                                FREventPollingError.builder().error("err1").description("error msg").build())
                )
                .build();
        when(mockRepo.findByApiClientIdAndJti(any(), any())).thenReturn(Optional.of(existingNotification));

        // When
        eventPollingService.recordTppEventErrors(pollingRequest, API_CLIENT_ID);

        // Then
        verify(mockRepo, times(1)).save(any());
        assertThat(existingNotification.getErrors().getError()).isEqualTo("err1");
        assertThat(existingNotification.getErrors().getDescription()).isEqualTo("error msg");
    }

    @Test
    public void recordTppEventErrors_notificationDoesNotExist_doNothing() {
        // Given
        FREventPolling pollingRequest = FREventPolling.builder()
                .setErrs(
                        Collections.singletonMap(JWT_ID_LIST.get(0),
                                FREventPollingError.builder().error("err1").description("error msg").build())
                )
                .build();
        when(mockRepo.findByApiClientIdAndJti(any(), any())).thenReturn(Optional.empty());

        // When
        eventPollingService.recordTppEventErrors(pollingRequest, API_CLIENT_ID);

        // Then
        verify(mockRepo, times(0)).save(any());
    }

    @Test
    public void recordTppEventErrors_noErrors_doNothing() {
        // Given
        FREventPolling pollingRequest = FREventPolling.builder()
                .setErrs(null)
                .build();

        // When
        eventPollingService.recordTppEventErrors(pollingRequest, API_CLIENT_ID);

        // Then
        verifyNoMoreInteractions(mockRepo);
    }

    @Test
    public void fetchNewEvents_getAll() throws Exception {
        // Given
        FREventMessageEntity existingNotification1 = aValidFREventMessageEntity(JWT_ID_LIST.get(0));
        FREventMessageEntity existingNotification2 = aValidFREventMessageEntity(JWT_ID_LIST.get(1));
        when(mockRepo.findByApiClientId(eq(API_CLIENT_ID))).thenReturn(
                ImmutableList.of(existingNotification1, existingNotification2)
        );

        // When
        FREventPolling pollingRequest = FREventPolling.builder()
                .maxEvents(100)
                .returnImmediately(true)
                .build();
        Map<String, String> eventNotifications = eventPollingService.fetchNewEvents(pollingRequest, API_CLIENT_ID);

        // Then
        assertThat(eventNotifications.size()).isEqualTo(2);
        assertThat(eventNotifications.get(JWT_ID_LIST.get(0))).isEqualTo(
                objectMapper.writeValueAsString(toOBEventNotification1(existingNotification1))
        );
        assertThat(eventNotifications.get(JWT_ID_LIST.get(1))).isEqualTo(
                objectMapper.writeValueAsString(toOBEventNotification1(existingNotification2))
        );
    }

    @Test
    public void fetchNewEvents_excludeEventsWithErrorsFromResults() throws Exception {
        // Given
        FREventMessageEntity existingNotificationWithoutError = aValidFREventMessageEntity(JWT_ID_LIST.get(0));
        FREventMessageEntity existingNotificationWithError = aValidFREventMessageEntity(
                JWT_ID_LIST.get(1),
                FREventPollingError.builder().error("err1").description("error").build()
        );

        when(mockRepo.findByApiClientId(eq(API_CLIENT_ID))).thenReturn(
                ImmutableList.of(existingNotificationWithoutError, existingNotificationWithError)
        );

        // When
        FREventPolling pollingRequest = FREventPolling.builder()
                .maxEvents(null) // Do not restrict
                .returnImmediately(true)
                .build();
        Map<String, String> eventNotifications = eventPollingService.fetchNewEvents(pollingRequest, API_CLIENT_ID);

        // Then
        assertThat(eventNotifications.size()).isEqualTo(1);
        assertThat(eventNotifications.get(JWT_ID_LIST.get(0))).isEqualTo(
                objectMapper.writeValueAsString(toOBEventNotification1(existingNotificationWithoutError))
        );
    }

    @Test
    public void fetchNewEvents_zeroEventsRequested_returnNothing() throws Exception {
        // When
        FREventPolling pollingRequest = FREventPolling.builder()
                .maxEvents(0)
                .returnImmediately(true)
                .build();
        Map<String, String> eventNotifications = eventPollingService.fetchNewEvents(pollingRequest, API_CLIENT_ID);

        // Then
        assertThat(eventNotifications.size()).isEqualTo(0);
        verifyNoMoreInteractions(mockRepo);
    }


    @Test
    public void fetchNewEvents_longPollingRequest_rejectUnsupported() throws Exception {
        // Given
        FREventPolling pollingRequest = FREventPolling.builder()
                .maxEvents(10)
                .returnImmediately(false)
                .build();

        assertThatThrownBy(() ->
                // When
                eventPollingService.fetchNewEvents(pollingRequest, API_CLIENT_ID))
                // Then
                .isInstanceOf(OBErrorResponseException.class);
    }

    @Test
    public void truncateEvents_threeEventsExist_maxOfTwo_truncateEventsToTwo() {
        // Given
        int maxEventsParam = 2;
        ImmutableMap<String, String> allEventsMap = ImmutableMap.of("11111", "jwt1", "22222", "jwt2", "33333", "jwt3");

        // When
        Map<String, String> truncatedEvents = eventPollingService.truncateEvents(maxEventsParam, allEventsMap, API_CLIENT_ID);

        // Then
        assertThat(truncatedEvents.size()).isEqualTo(2);
    }

    @Test
    public void truncateEvents_noEvents_doNothing() {
        // When
        Map<String, String> truncatedEvents = eventPollingService.truncateEvents(2, Collections.emptyMap(), API_CLIENT_ID);

        // Then
        assertThat(truncatedEvents.size()).isEqualTo(0);
    }

    private FREventMessageEntity aValidFREventMessageEntity() {
        return aValidFREventMessageEntity(UUID.randomUUID().toString(), null);
    }

    private FREventMessageEntity aValidFREventMessageEntity(String jti) {
        return aValidFREventMessageEntity(jti, null);
    }

    private FREventMessageEntity aValidFREventMessageEntity(FREventPollingError error) {
        return aValidFREventMessageEntity(UUID.randomUUID().toString(), error);
    }

    private FREventMessageEntity aValidFREventMessageEntity(String jti, FREventPollingError error) {
        return FREventMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .jti(jti)
                .apiClientId(UUID.randomUUID().toString())
                .iss("https://examplebank.com/")
                .iat(1516239022)
                .sub("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                .aud("7umx5nTR33811QyQfi")
                .txn("dfc51628-3479-4b81-ad60-210b43d02306")
                .toe(1516239022)
                .events(new OBEvent1().urnukorgopenbankingeventsresourceUpdate(
                                new OBEventResourceUpdate1()
                                        .subject(
                                                new OBEventSubject1()
                                                        .subjectType("http://openbanking.org.uk/rid_http://openbanking.org.uk/rty")
                                                        .httpopenbankingOrgUkrid("pmt-7290-003")
                                                        .httpopenbankingOrgUkrlk(
                                                                List.of(
                                                                        new OBEventLink1()
                                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                                .version("v3.1.5"),
                                                                        new OBEventLink1()
                                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                                .version("v3.1.10")
                                                                )
                                                        )
                                                        .httpopenbankingOrgUkrty("domestic-payment")
                                        )
                        )
                )
                .errors(error != null ? error : null)
                .build();
    }
}


