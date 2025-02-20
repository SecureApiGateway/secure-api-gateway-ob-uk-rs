/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.v3_1_10.eventsubscription;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.event.FREventSubscriptionConverter.toFREventSubscriptionData;

import java.net.URI;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventSubscriptionData;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventSubscription;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.events.EventSubscriptionsRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.event.v3_1_10.eventsubscription.EventSubscriptionsApi;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import uk.org.openbanking.datamodel.v3.common.Links;
import uk.org.openbanking.datamodel.v3.common.Meta;
import uk.org.openbanking.datamodel.v3.event.OBEventSubscription1;
import uk.org.openbanking.datamodel.v3.event.OBEventSubscription1Data;
import uk.org.openbanking.datamodel.v3.event.OBEventSubscriptionResponse1;
import uk.org.openbanking.datamodel.v3.event.OBEventSubscriptionResponse1Data;
import uk.org.openbanking.datamodel.v3.event.OBEventSubscriptionsResponse1;
import uk.org.openbanking.datamodel.v3.event.OBEventSubscriptionsResponse1Data;
import uk.org.openbanking.datamodel.v3.event.OBEventSubscriptionsResponse1DataEventSubscriptionInner;

@Controller("EventSubscriptionApiV3.1.10")
public class EventSubscriptionsApiController implements EventSubscriptionsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final EventSubscriptionsRepository eventSubscriptionsRepository;

    public EventSubscriptionsApiController(EventSubscriptionsRepository eventSubscriptionsRepository) {
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
        logger.debug("Create new event subscriptions: {} for TPP: {}", obEventSubscription, tppId);

        // Check if an event already exists for this TPP
        Collection<FREventSubscription> byClientId = eventSubscriptionsRepository.findByTppId(tppId);
        if (!byClientId.isEmpty()) {
            logger.debug("An event subscription already exists for this TPP id: '{}' for the version: {}", tppId,
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
        logger.debug("Read event subscription for TPP: {}", tppId);

        // A TPP must not access an event-subscription on an older version, via the EventSubscriptionId for an event-subscription created in a newer version
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        Collection<FREventSubscription> frEventSubscriptions = Optional.ofNullable(eventSubscriptionsRepository.findByTppId(tppId))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(frEventSubs -> ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, OBVersion.fromString(frEventSubs.getEventSubscription().getVersion())))
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
            OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
            OBVersion resourceVersion = OBVersion.fromString(existingEventSubscription.getEventSubscription().getVersion());
            if (ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, resourceVersion)) {
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
            OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
            OBVersion resourceVersion = OBVersion.fromString(byId.get().getEventSubscription().getVersion());
            if (ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, resourceVersion)) {
                logger.debug("Deleting event subscriptions URL: {}", byId.get());
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
        List<OBEventSubscriptionsResponse1DataEventSubscriptionInner> eventSubsByClient = eventSubs.stream()
                .map(e -> new OBEventSubscriptionsResponse1DataEventSubscriptionInner()
                        .callbackUrl(e.getEventSubscription().getCallbackUrl() != null ? URI.create(e.getEventSubscription().getCallbackUrl()) : null)
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
                    .links(LinksHelper.createEventSubscriptionResourcesLink(this.getClass()));
        }
    }

    private OBEventSubscriptionResponse1 packageResponse(FREventSubscription frEventSubscription) {
        FREventSubscriptionData obEventSubs = frEventSubscription.getEventSubscription();
        return new OBEventSubscriptionResponse1()
                .data(new OBEventSubscriptionResponse1Data()
                        .callbackUrl(obEventSubs.getCallbackUrl() != null ? URI.create(obEventSubs.getCallbackUrl()) : null)
                        .eventSubscriptionId(frEventSubscription.getId())
                        .eventTypes(obEventSubs.getEventTypes())
                        .version(obEventSubs.getVersion())
                )
                .links(LinksHelper.createEventSubscriptionSelfLink(this.getClass(), frEventSubscription.getId()))
                .meta(new Meta());
    }

}
