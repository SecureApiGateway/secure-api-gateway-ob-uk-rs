/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.event.v3_1_2.eventsubscription;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.event.FREventSubscriptionData;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.event.FREventSubscription;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.events.EventSubscriptionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Links;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.event.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.event.FREventSubscriptionConverter.toFREventSubscriptionData;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor.getVersionFromPath;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.link.LinksHelper.*;
import static com.forgerock.securebanking.openbanking.uk.rs.validator.ResourceVersionValidator.isAccessToResourceAllowed;

@Controller("EventSubscriptionApiV3.1.2")
@Slf4j
public class EventSubscriptionApiController implements EventSubscriptionApi {

    private final EventSubscriptionsRepository eventSubscriptionsRepository;

    public EventSubscriptionApiController(EventSubscriptionsRepository eventSubscriptionsRepository) {
        this.eventSubscriptionsRepository = eventSubscriptionsRepository;
    }

    @Override
    public ResponseEntity<OBEventSubscriptionResponse1> createEventSubscription(
            @Valid OBEventSubscription1 obEventSubscription,
            String authorization,
            String xJwsSignature,
            String xFapiInteractionId,
            String tppId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Create new event subscriptions: {} for TPP: {}", obEventSubscription, tppId);

        // Check if an event already exists for this TPP
        Collection<FREventSubscription> byClientId = eventSubscriptionsRepository.findByTppId(tppId);
        if (!byClientId.isEmpty()) {
            log.debug("An event subscription already exists for this TPP id: '{}' for the version: {}", tppId,
                    byClientId.stream().findFirst().get());
            throw new OBErrorResponseException(
                    HttpStatus.CONFLICT,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.EVENT_SUBSCRIPTION_ALREADY_EXISTS.toOBError1()
            );
        }

        // Persist the event subscription
        FREventSubscription frEventSubscription = FREventSubscription.builder()
                .id(UUID.randomUUID().toString())
                .tppId(tppId)
                .eventSubscription(toFREventSubscriptionData(obEventSubscription))
                .build();
        eventSubscriptionsRepository.save(frEventSubscription);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(packageResponse(frEventSubscription));
    }

    @Override
    public ResponseEntity readEventSubscription(
            String authorization,
            String xFapiInteractionId,
            String tppId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Read event subscription for TPP: {}", tppId);

        // A TPP must not access an event-subscription on an older version, via the EventSubscriptionId for an event-subscription created in a newer version
        OBVersion apiVersion = getVersionFromPath(request);
        Collection<FREventSubscription> frEventSubscriptions = Optional.ofNullable(eventSubscriptionsRepository.findByTppId(tppId))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(frEventSubs -> isAccessToResourceAllowed(apiVersion, OBVersion.fromString(frEventSubs.getEventSubscription().getVersion())))
                .collect(Collectors.toList());
        if (!frEventSubscriptions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(packageResponse(frEventSubscriptions));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("The event subscription can't be read via an older API version.");
        }
    }

    @Override
    public ResponseEntity updateEventSubscription(
            String eventSubscriptionId,
            @Valid OBEventSubscriptionResponse1 obEventSubscription,
            String authorization,
            String xJwsSignature,
            String xFapiInteractionId,
            String tppId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        OBEventSubscription1 updatedSubscription =
                new OBEventSubscription1().data(new OBEventSubscription1Data()
                        .callbackUrl(obEventSubscription.getData().getCallbackUrl())
                        .eventTypes(obEventSubscription.getData().getEventTypes())
                        .version(obEventSubscription.getData().getVersion())
                );
        Optional<FREventSubscription> byId = eventSubscriptionsRepository.findById(eventSubscriptionId);
        if (byId.isPresent()) {
            FREventSubscription existingEventSubscription = byId.get();
            // A TPP must not update a event-subscription on an older version, via the EventSubscriptionId for an event-subscription created in a newer version
            OBVersion apiVersion = getVersionFromPath(request);
            OBVersion resourceVersion = OBVersion.fromString(existingEventSubscription.getEventSubscription().getVersion());
            if (isAccessToResourceAllowed(apiVersion, resourceVersion)) {
                existingEventSubscription.setEventSubscription(toFREventSubscriptionData(updatedSubscription));
                eventSubscriptionsRepository.save(existingEventSubscription);
                return ResponseEntity.ok(packageResponse(existingEventSubscription));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("The event subscription can't be update via an older API version.");
            }
        } else {
            // PUT is only used for amending existing subscriptions
            throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.EVENT_SUBSCRIPTION_NOT_FOUND.toOBError1(eventSubscriptionId)
            );
        }
    }

    @Override
    public ResponseEntity deleteEventSubscription(
            String eventSubscriptionId,
            String authorization,
            String xFapiInteractionId,
            String tppId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        final Optional<FREventSubscription> byId = eventSubscriptionsRepository.findById(eventSubscriptionId);
        if (byId.isPresent()) {
            // A TPP must not delete a event-subscription on an older version, via the EventSubscriptionId for an event-subscription created in a newer version
            OBVersion apiVersion = getVersionFromPath(request);
            OBVersion resourceVersion = OBVersion.fromString(byId.get().getEventSubscription().getVersion());
            if (isAccessToResourceAllowed(apiVersion, resourceVersion)) {
                log.debug("Deleting event subscriptions URL: {}", byId.get());
                eventSubscriptionsRepository.deleteById(eventSubscriptionId);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("The event subscription can't be delete via an older API version.");
            }
        } else {
            throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.EVENT_SUBSCRIPTION_NOT_FOUND.toOBError1(eventSubscriptionId)
            );
        }
    }

    private OBEventSubscriptionsResponse1 packageResponse(Collection<FREventSubscription> eventSubs) {
        List<OBEventSubscriptionsResponse1DataEventSubscription> eventSubsByClient = eventSubs.stream()
                .map(e -> new OBEventSubscriptionsResponse1DataEventSubscription()
                        .callbackUrl(e.getEventSubscription().getCallbackUrl())
                        .eventSubscriptionId(e.getId())
                        .eventTypes(e.getEventSubscription().getEventTypes())
                        .version(e.getEventSubscription().getVersion())
                ).collect(Collectors.toList());

        if (eventSubsByClient.isEmpty()) {
            return new OBEventSubscriptionsResponse1()
                    .data(new OBEventSubscriptionsResponse1Data().eventSubscription(Collections.emptyList()))
                    .meta(new Meta())
                    .links(new Links());
        } else {
            return new OBEventSubscriptionsResponse1()
                    .data(new OBEventSubscriptionsResponse1Data()
                            .eventSubscription(eventSubsByClient))
                    .meta(new Meta())
                    .links(createEventSubscriptionResourcesLink(this.getClass()));
        }
    }

    private OBEventSubscriptionResponse1 packageResponse(FREventSubscription frEventSubscription) {
        FREventSubscriptionData obEventSubs = frEventSubscription.getEventSubscription();
        return new OBEventSubscriptionResponse1()
                .data(new OBEventSubscriptionResponse1Data()
                        .callbackUrl(obEventSubs.getCallbackUrl())
                        .eventSubscriptionId(frEventSubscription.getId())
                        .eventTypes(obEventSubs.getEventTypes())
                        .version(obEventSubs.getVersion())
                )
                .links(createEventSubscriptionSelfLink(this.getClass(), frEventSubscription.getId()))
                .meta(new Meta());
    }
}
