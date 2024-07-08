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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.event;

import static com.forgerock.sapi.gateway.rs.resource.store.api.admin.events.FRDataEventsConverter.toOBEventNotification1;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPolling;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;
import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.v3.event.OBEventNotification1;

/*
 * Intended to hold the business logic of polling and acknowledge the events separately to the persistence layer (Mongo
 * interface) and the REST layer (controller). Makes testing and re-use much easier and controller class less
 * complicated.
 */
@Service
@Slf4j
public class EventPollingService {
    private final FREventMessageRepository frEventMessageRepository;
    private final ObjectMapper objectMapper;

    // The TPP can never request more events than this.
    private final int eventsLimit;

    public EventPollingService(
            FREventMessageRepository frEventMessageRepository,
            ObjectMapper objectMapper,
            @Value("${rs.data.upload.limit.events:10}") int eventsLimit
    ) {
        this.frEventMessageRepository = frEventMessageRepository;
        this.objectMapper = objectMapper;
        this.eventsLimit = eventsLimit;
    }

    public void acknowledgeEvents(FREventPolling frEventPolling, String apiClientId) {
        Preconditions.checkNotNull(apiClientId);
        Preconditions.checkNotNull(frEventPolling);

        if (frEventPolling.getAck() != null && !frEventPolling.getAck().isEmpty()) {
            log.debug("TPP '{}' is acknowledging (and therefore deleting) the following event notifications: {}", apiClientId, frEventPolling.getAck());
            frEventPolling.getAck()
                    .forEach(
                            jti -> frEventMessageRepository.deleteByApiClientIdAndJti(apiClientId, jti)
                    );
        }
    }

    public void recordTppEventErrors(FREventPolling frEventPolling, String apiClientId) {
        Preconditions.checkNotNull(apiClientId);
        Preconditions.checkNotNull(frEventPolling);
        if (frEventPolling.getSetErrs() != null && !frEventPolling.getSetErrs().isEmpty()) {
            log.debug("Persisting {} event notification errors for keys: {}", frEventPolling.getSetErrs().size(), frEventPolling.getSetErrs().keySet());
            frEventPolling.getSetErrs()
                    .forEach((key, value) -> frEventMessageRepository.findByApiClientIdAndJti(apiClientId, key)
                            .ifPresent(event -> {
                                event.setErrors(value);
                                frEventMessageRepository.save(event);
                            }));
        }

    }

    public Map<String, String> fetchNewEvents(FREventPolling frEventPolling, String apiClientId) throws OBErrorResponseException {
        Preconditions.checkNotNull(apiClientId);
        Preconditions.checkNotNull(frEventPolling);
        if (frEventPolling.getMaxEvents() != null && frEventPolling.getMaxEvents() <= 0) {
            // Zero notifications can be requested by TPP when they just want to send acknowledgements and/or errors to sandbox
            log.debug("Polling request for TPP: '{}' requested no event notifications so none will be returned", apiClientId);
            return Collections.emptyMap();
        }

        // Long polling is currently optional in OB specs and as it requires more work to implement it will be left out for now.
        if (frEventPolling.getReturnImmediately() != null && !frEventPolling.isReturnImmediately()) {
            log.warn("TPP: {} requested long polling on the event notification API but it is not supported", apiClientId);
            throw new OBErrorResponseException(
                    HttpStatus.NOT_IMPLEMENTED,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.LONG_POLLING_NOT_SUPPORTED_FOR_EVENTS.toOBError1());
        }

        // Load all event notifications for TPP
        log.debug("Loading all notifications for TPP: {}", apiClientId);
        try {
            return frEventMessageRepository.findByApiClientId(apiClientId).stream()
                    .filter(event -> !event.hasErrors())
                    .collect(
                            Collectors.toMap(
                                    FREventMessageEntity::getJti,
                                    frEventMessageEntity ->
                                            writeValueAsString(toOBEventNotification1(frEventMessageEntity))

                            )
                    );
        } catch (Exception e) {
            throw new OBErrorResponseException(
                    HttpStatus.NOT_IMPLEMENTED,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.LONG_POLLING_NOT_SUPPORTED_FOR_EVENTS.toOBError1());
        }
    }

    private String writeValueAsString(OBEventNotification1 obEventNotification1) {
        try {
            return objectMapper.writeValueAsString(obEventNotification1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> truncateEvents(Integer maxEvents, Map<String, String> eventNotifications, String apiClientId) {
        Preconditions.checkNotNull(apiClientId);
        log.debug("Request to truncate {} event notification to be max of {}", eventNotifications.size(), maxEvents);
        if (eventNotifications.isEmpty()) {
            return eventNotifications; // Nothing to do
        }

        if (maxEvents == null || maxEvents > eventsLimit) {
            log.debug("TPP {} requested a number of event notifications ({}) on polling that exceeds that allowed maximum on the sandbox ({}). Only {} will be returned.", apiClientId, maxEvents, eventsLimit, eventsLimit);
            maxEvents = eventsLimit;
        }

        if (eventNotifications.size() > maxEvents) {
            log.debug("TPP has {} pending event notifications. Only the first {} will be returned.", eventNotifications.size(), maxEvents);
            return eventNotifications.entrySet().stream()
                    .limit(maxEvents)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return eventNotifications;
        }

    }
}
