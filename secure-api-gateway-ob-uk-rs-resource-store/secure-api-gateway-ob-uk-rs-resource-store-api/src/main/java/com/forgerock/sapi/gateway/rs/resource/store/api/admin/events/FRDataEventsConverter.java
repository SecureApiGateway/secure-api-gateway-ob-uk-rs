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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessages;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.events.FREventMessage;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;

public class FRDataEventsConverter {

    public static final FREventMessageEntity toFREventMessageEntity(String apiClientId, FREventMessage frEventMessage) {
        Objects.requireNonNull(apiClientId, "api client Id must not be null");
        FREventMessageEntity entity = FRModelMapper.map(frEventMessage, FREventMessageEntity.class);
        entity.setApiClientId(apiClientId);
        return entity;
    }

    public static final List<FREventMessageEntity> toFREventMessageEntityList(FREventMessages frEventMessages) {
        List<FREventMessageEntity> frEventMessageEntityList = new ArrayList<>();
        frEventMessages.getEvents().forEach(
                frEventMessage -> {
                    FREventMessageEntity eventMessageEntity = toFREventMessageEntity(frEventMessages.getTppId(), frEventMessage);
                    frEventMessageEntityList.add(eventMessageEntity);
                }
        );
        return frEventMessageEntityList;
    }

    public static final FREventMessage toFREventMessage(FREventMessageEntity entity) {
        return FRModelMapper.map(entity, FREventMessage.class);
    }

}
