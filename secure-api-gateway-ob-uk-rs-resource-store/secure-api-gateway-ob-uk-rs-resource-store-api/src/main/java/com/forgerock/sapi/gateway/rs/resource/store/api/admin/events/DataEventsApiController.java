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

import static com.forgerock.sapi.gateway.rs.resource.store.api.admin.events.FRDataEventsConverter.toFREventMessage;
import static com.forgerock.sapi.gateway.rs.resource.store.api.admin.events.FRDataEventsConverter.toFREventMessageEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessage;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessages;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.FREventMessageRepository;

import lombok.extern.slf4j.Slf4j;

@Controller("DataEventsApi")
@Slf4j
public class DataEventsApiController implements DataEventsApi {
    private final FREventMessageRepository frEventMessageRepository;

    @Autowired
    public DataEventsApiController(
            FREventMessageRepository frEventMessageRepository
    ) {
        this.frEventMessageRepository = frEventMessageRepository;
    }

    @Override
    public ResponseEntity<FREventMessages> importEvents(FREventMessages frEventMessages) throws OBErrorResponseException {
        try {
            return new ResponseEntity<>(createEvents(frEventMessages), HttpStatus.CREATED);
        } catch (Exception exception) {
            throw handleError(exception);
        }
    }

    @Override
    public ResponseEntity<FREventMessages> updateEvents(FREventMessages frEventMessages) throws OBErrorResponseException {
        try {
            FREventMessages eventUpdated = FREventMessages.builder().tppId(frEventMessages.getTppId()).build();
            frEventMessages.getEvents().forEach(frEventMessage -> {
                Optional<FREventMessageEntity> optionalFREventNotification =
                        frEventMessageRepository.findByApiClientIdAndJti(
                                frEventMessages.getTppId(), frEventMessage.getJti()
                        );
                optionalFREventNotification.ifPresent(entity -> {
                            entity.setSet(frEventMessage.getSet());
                            FREventMessageEntity entityUpdated = frEventMessageRepository.save(entity);
                            eventUpdated.eventItem(toFREventMessage(entityUpdated));
                        }
                );
            });
            return ResponseEntity.ok(eventUpdated);
        } catch (Exception exception) {
            throw handleError(exception);
        }
    }

    @Override
    public ResponseEntity<Collection<FREventMessages>> exportEvents() {
        log.debug("Find all Events");
        List<FREventMessages> frEventMessages = new ArrayList<>();
        List<FREventMessageEntity> entities = frEventMessageRepository.findAll();
        if (!entities.isEmpty()) {
            // Get the apiClients
            List<String> apiClients = entities
                    .stream().map(
                            entity -> entity.getApiClientId()
                    ).distinct().collect(Collectors.toList());
            // create a list of SETs per apiClient
            apiClients.forEach(apiClient -> {
                List<FREventMessage> clientEvents = entities.stream()
                        .filter(entity -> entity.getApiClientId().equals(apiClient))
                        .map(entity -> toFREventMessage(entity))
                        .collect(Collectors.toList());

                frEventMessages.add(FREventMessages.builder().tppId(apiClient).events(clientEvents).build());
            });
        }

        return ResponseEntity.ok(frEventMessages);
    }

    @Override
    public ResponseEntity<FREventMessages> exportEventsByTppId(String tppId) {
        log.debug("Find all Events for TPP:{}", tppId);
        Collection<FREventMessageEntity> entities = frEventMessageRepository.findByApiClientId(tppId);
        FREventMessages dataEvent = FREventMessages.builder().tppId(tppId).events(Collections.EMPTY_LIST).build();
        if (!entities.isEmpty()) {
            dataEvent.events(entities.stream().map(entity -> toFREventMessage(entity)).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(dataEvent);
    }

    @Override
    public ResponseEntity<Void> removeEvents(String tppId, String jti) {
        if (Objects.nonNull(jti)) {
            log.debug("Remove Event with jti: {} for TPP:{}", jti, tppId);
            frEventMessageRepository.deleteByApiClientIdAndJti(tppId, jti);
        } else {
            log.debug("Remove all Events for TPP:{}", tppId);
            frEventMessageRepository.deleteByApiClientId(tppId);
        }
        return new ResponseEntity(Void.class, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> removeAllEvents() {
        frEventMessageRepository.deleteAll();
        return new ResponseEntity(Void.class, HttpStatus.NO_CONTENT);
    }

    /**
     * Create new events
     *
     * @param frEventMessages the entity body {@link FREventMessages}
     */
    private FREventMessages createEvents(FREventMessages frEventMessages) {
        log.debug("creating event messages\n{}", frEventMessages);
        FREventMessages frEventMessagesCreated = FREventMessages.builder().tppId(frEventMessages.getTppId()).build();

        frEventMessages.getEvents().forEach(
                eventMessage -> {
                    FREventMessageEntity entity = toFREventMessageEntity(frEventMessages.getTppId(), eventMessage);
                    entity.setApiClientId(frEventMessages.getTppId());
                    log.debug("Create event message {}", entity);
                    frEventMessagesCreated.eventItem(toFREventMessage(frEventMessageRepository.save(entity)));
                }
        );
        return frEventMessagesCreated;
    }


    /**
     * Handle the exception and reformat to {@link OBErrorResponseException}
     *
     * @param exception the exception to handle
     * @return {@link OBErrorResponseException} the response exception
     */
    private OBErrorResponseException handleError(Exception exception) {
        log.error("DataEvents API Error: {}", exception);
        if (exception instanceof OBErrorException) {
            OBErrorException obErrorException = (OBErrorException) exception;
            return new OBErrorResponseException(obErrorException.getObriErrorType().getHttpStatus(),
                    OBRIErrorResponseCategory.SERVER_INTERNAL_ERROR,
                    obErrorException.getOBError());
        } else if (exception instanceof OBErrorResponseException) {
            return (OBErrorResponseException) exception;
        } else if (exception.getCause() != null && exception.getCause() instanceof OBErrorResponseException) {
            return (OBErrorResponseException) exception.getCause();
        } else {
            return new OBErrorResponseException(
                    OBRIErrorType.SERVER_ERROR.getHttpStatus(),
                    OBRIErrorResponseCategory.SERVER_INTERNAL_ERROR,
                    OBRIErrorType.SERVER_ERROR.toOBError1(exception.getMessage()));
        }
    }
}
