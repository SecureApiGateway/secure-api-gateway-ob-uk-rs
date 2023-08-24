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

import static com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType.DATA_INVALID_REQUEST;
import static com.forgerock.sapi.gateway.rs.resource.store.api.admin.events.FRDataEventsConverter.toFREventMessage;
import static com.forgerock.sapi.gateway.rs.resource.store.api.admin.events.FRDataEventsConverter.toFREventMessageEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessages;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;

import lombok.extern.slf4j.Slf4j;

@Controller("DataEventsApi")
@Slf4j
public class DataEventsApiController implements DataEventsApi {
    private final FREventMessageRepository frEventMessageRepository;
    private final Integer eventsLimit;

    @Autowired
    public DataEventsApiController(
            FREventMessageRepository frEventMessageRepository,
            @Value("${rs.data.upload.limit.events}") Integer eventsLimit
    ) {
        this.frEventMessageRepository = frEventMessageRepository;
        this.eventsLimit = eventsLimit;
    }

    @Override
    public ResponseEntity<FREventMessages> importEvents(FREventMessages frEventMessages) throws OBErrorException {
        validateApiClientId(frEventMessages.getApiClientId());
        limitValidation(frEventMessages);
        log.debug("Import events {}", frEventMessages);
        return new ResponseEntity<>(createEvents(frEventMessages), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<FREventMessages> updateEvents(FREventMessages frEventMessages) throws OBErrorException {
        validateApiClientId(frEventMessages.getApiClientId());
        validatePayloadEventsLimit(frEventMessages.getEvents().size());
        log.debug("Update events {}", frEventMessages);
        FREventMessages eventUpdated = FREventMessages.builder().apiClientId(frEventMessages.getApiClientId()).build();
        frEventMessages.getEvents().forEach(frEventMessage -> {
            Optional<FREventMessageEntity> optionalFREventNotification =
                    frEventMessageRepository.findByApiClientIdAndJti(
                            frEventMessages.getApiClientId(), frEventMessage.getJti()
                    );
            optionalFREventNotification.ifPresent(entity -> {
                        entity.setSet(frEventMessage.getSet());
                        FREventMessageEntity entityUpdated = frEventMessageRepository.save(entity);
                        eventUpdated.eventItem(toFREventMessage(entityUpdated));
                    }
            );
        });
        return ResponseEntity.ok(eventUpdated);
    }

    @Override
    public ResponseEntity<FREventMessages> exportEvents(String apiClientId) throws OBErrorException {
        validateApiClientId(apiClientId);
        log.debug("Export all Events for apiClient:{}", apiClientId);
        Collection<FREventMessageEntity> entities = frEventMessageRepository.findByApiClientId(apiClientId);
        FREventMessages dataEvent = FREventMessages.builder().apiClientId(apiClientId).events(Collections.EMPTY_LIST).build();
        if (!entities.isEmpty()) {
            dataEvent.events(entities.stream().map(FRDataEventsConverter::toFREventMessage).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(dataEvent);
    }

    @Override
    public ResponseEntity removeEvents(String apiClientId, String jti) throws OBErrorException {
        validateApiClientId(apiClientId);
        if (Objects.nonNull(jti)) {
            log.debug("Remove Event with jti: {} for apiClient:{}", jti, apiClientId);
            frEventMessageRepository.deleteByApiClientIdAndJti(apiClientId, jti);
        } else {
            log.debug("Remove all Events for apiClient:{}", apiClientId);
            frEventMessageRepository.deleteByApiClientId(apiClientId);
        }
        return new ResponseEntity(Void.class, HttpStatus.NO_CONTENT);
    }

    /**
     * Checks apiClientId property
     *
     * @param apiClientId
     * @throws OBErrorException
     */
    private void validateApiClientId(String apiClientId) throws OBErrorException {
        if (Objects.isNull(apiClientId) || apiClientId.isEmpty() || apiClientId.isBlank()) {
            throw new OBErrorException(
                    DATA_INVALID_REQUEST,
                    "The apiClientId cannot be null or empty"
            );
        }
    }

    /**
     * Checks the limit of events supported by the system depending on the configuration (rs.data.upload.limit.events)
     *
     * @param frEventMessages
     * @throws OBErrorException
     */
    private void limitValidation(FREventMessages frEventMessages) throws OBErrorException {
        validatePayloadEventsLimit(frEventMessages.getEvents().size());
        validateStoredEvents(frEventMessages);
        validateTotalEventsToImport(frEventMessages);
    }

    /**
     * Checks the number of events provided in the payload
     *
     * @param numOfEvents
     * @throws OBErrorException
     */
    private void validatePayloadEventsLimit(Integer numOfEvents) throws OBErrorException {
        if (numOfEvents > eventsLimit) {
            throw new OBErrorException(
                    DATA_INVALID_REQUEST,
                    String.format("The number of events provided in the payload (%d) exceeded maximum limit of %s", numOfEvents, eventsLimit));
        }
    }

    /**
     * Checks if the current stored events in the system has reached the limit
     *
     * @param frEventMessages
     * @throws OBErrorException
     */
    private void validateStoredEvents(FREventMessages frEventMessages) throws OBErrorException {
        Collection<FREventMessageEntity> entities = frEventMessageRepository.findByApiClientId(frEventMessages.getApiClientId());
        if (entities.size() >= eventsLimit) {
            throw new OBErrorException(
                    DATA_INVALID_REQUEST,
                    String.format("Cannot add events as it the stored events in the system has reached the maximum limit of %s, current events in the system %d", eventsLimit, entities.size())
            );
        }
    }

    /**
     * Checks if the total events to import will reach the limit
     *
     * @param frEventMessages
     * @throws OBErrorException
     */
    private void validateTotalEventsToImport(FREventMessages frEventMessages) throws OBErrorException {
        Collection<FREventMessageEntity> entities = frEventMessageRepository.findByApiClientId(frEventMessages.getApiClientId());
        Integer totalEvents = entities.size() + frEventMessages.getEvents().size();
        if (totalEvents > eventsLimit) {
            throw new OBErrorException(
                    DATA_INVALID_REQUEST,
                    String.format("Cannot add events as it will exceeded maximum limit of %s, current events in the system %d", eventsLimit, entities.size())
            );
        }
    }

    /**
     * Create new events
     *
     * @param frEventMessages the entity body {@link FREventMessages}
     */
    private FREventMessages createEvents(FREventMessages frEventMessages) {
        FREventMessages frEventMessagesCreated = FREventMessages.builder().apiClientId(frEventMessages.getApiClientId()).build();
        frEventMessages.getEvents().forEach(
                eventMessage -> {
                    FREventMessageEntity entity = toFREventMessageEntity(frEventMessages.getApiClientId(), eventMessage);
                    entity.setApiClientId(frEventMessages.getApiClientId());
                    log.debug("Create event message {}", entity);
                    frEventMessagesCreated.eventItem(toFREventMessage(frEventMessageRepository.save(entity)));
                }
        );
        return frEventMessagesCreated;
    }

}
