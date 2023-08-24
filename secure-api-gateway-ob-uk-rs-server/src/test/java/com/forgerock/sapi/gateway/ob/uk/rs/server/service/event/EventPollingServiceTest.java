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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPolling;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPollingError;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link EventPollingService}.
 */
@ExtendWith(MockitoExtension.class)
public class EventPollingServiceTest {
    private static final String API_CLIENT_ID = "abc123";

    @Mock
    private FREventMessageRepository mockRepo;
    @InjectMocks
    private EventPollingService eventPollingService;

    @Test
    public void acknowledgeEvents_findAndDeleteTwoEvents_ignoreEventNotfound() {
        // Given
        FREventPolling pollingRequest = FREventPolling.builder()
                .ack(ImmutableList.of("11111", "22222", "NotFound"))
                .build();

        // When
        eventPollingService.acknowledgeEvents(pollingRequest, API_CLIENT_ID);

        // Then
        verify(mockRepo).deleteByApiClientIdAndJti(eq(API_CLIENT_ID), eq("11111"));
        verify(mockRepo).deleteByApiClientIdAndJti(eq(API_CLIENT_ID), eq("11111"));
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
        FREventMessageEntity existingNotification = FREventMessageEntity.builder()
                .id("1")
                .jti("11111")
                .errors(null)
                .build();
        FREventPolling pollingRequest = FREventPolling.builder()
                .setErrs(Collections.singletonMap("11111", FREventPollingError.builder().error("err1").description("error msg").build()))
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
                .setErrs(Collections.singletonMap("11111", FREventPollingError.builder().error("err1").description("error msg").build()))
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
    public void fetchNewEvents_getAll() throws Exception{
        // Given
        FREventMessageEntity existingNotification1 = FREventMessageEntity.builder()
                .id("1")
                .jti("11111")
                .set("test111")
                .build();
        FREventMessageEntity existingNotification2 = FREventMessageEntity.builder()
                .id("2")
                .jti("22222")
                .set("test222")
                .build();
        when(mockRepo.findByApiClientId(eq(API_CLIENT_ID))).thenReturn(ImmutableList.of(existingNotification1, existingNotification2));

        // When
        FREventPolling pollingRequest = FREventPolling.builder()
                .maxEvents(100)
                .returnImmediately(true)
                .build();
        Map<String, String> eventNotifications = eventPollingService.fetchNewEvents(pollingRequest, API_CLIENT_ID);

        // Then
        assertThat(eventNotifications.size()).isEqualTo(2);
        assertThat(eventNotifications.get("11111")).isEqualTo("test111");
        assertThat(eventNotifications.get("22222")).isEqualTo("test222");
    }

    @Test
    public void fetchNewEvents_excludeEventsWithErrorsFromResults() throws Exception{
        // Given
        FREventMessageEntity existingNotificationWithoutError = FREventMessageEntity.builder()
                .id("1")
                .jti("11111")
                .set("test111")
                .build();
        FREventMessageEntity existingNotificationWithError = FREventMessageEntity.builder()
                .id("2")
                .jti("22222")
                .set("test222")
                .errors(FREventPollingError.builder().error("err1").description("error").build())
                .build();
        when(mockRepo.findByApiClientId(eq(API_CLIENT_ID))).thenReturn(ImmutableList.of(existingNotificationWithoutError, existingNotificationWithError));

        // When
        FREventPolling pollingRequest = FREventPolling.builder()
                .maxEvents(null) // Do not restrict
                .returnImmediately(true)
                .build();
        Map<String, String> eventNotifications = eventPollingService.fetchNewEvents(pollingRequest, API_CLIENT_ID);

        // Then
        assertThat(eventNotifications.size()).isEqualTo(1);
        assertThat(eventNotifications.get("11111")).isEqualTo("test111");
    }

    @Test
    public void fetchNewEvents_zeroEventsRequested_returnNothing() throws Exception{
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
    public void fetchNewEvents_longPollingRequest_rejectUnsupported() throws Exception{
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
}


