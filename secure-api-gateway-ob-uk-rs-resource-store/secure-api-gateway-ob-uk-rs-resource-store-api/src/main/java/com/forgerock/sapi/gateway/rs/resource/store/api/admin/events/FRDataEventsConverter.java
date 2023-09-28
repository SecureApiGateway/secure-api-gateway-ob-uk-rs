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

import java.util.Objects;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessage;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;

import uk.org.openbanking.datamodel.event.OBEventNotification1;

public class FRDataEventsConverter {

    public static final FREventMessageEntity toFREventMessageEntity(String apiClientId, FREventMessage frEventMessage) {
        Objects.requireNonNull(apiClientId, "api client Id must not be null");
        FREventMessageEntity entity = FRModelMapper.map(frEventMessage, FREventMessageEntity.class);
        entity.setApiClientId(apiClientId);
        return entity;
    }

    public static final FREventMessageEntity toFREventMessageEntity(String apiClientId, OBEventNotification1 obEventNotification1) {
        Objects.requireNonNull(apiClientId, "api client Id must not be null");
        FREventMessageEntity entity = FRModelMapper.map(obEventNotification1, FREventMessageEntity.class);
        entity.setApiClientId(apiClientId);
        return entity;
    }

    public static final OBEventNotification1 toOBEventNotification1(FREventMessageEntity entity) {
        return FRModelMapper.map(entity, OBEventNotification1.class);
    }

    public static final FREventMessage toFREventMessage(FREventMessageEntity entity) {
        return FRModelMapper.map(entity, FREventMessage.class);
    }

}
