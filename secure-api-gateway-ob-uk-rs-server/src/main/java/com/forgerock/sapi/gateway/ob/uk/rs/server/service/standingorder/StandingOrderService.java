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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.standingorder;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRStandingOrderData;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStandingOrder;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.standingorders.FRStandingOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for saving, retrieving and updating {@link FRStandingOrder FRStandingOrders} for the Accounts API.
 */
@Service
@Slf4j
public class StandingOrderService {

    private final FRStandingOrderRepository standingOrderRepository;

    public StandingOrderService(FRStandingOrderRepository standingOrderRepository) {
        this.standingOrderRepository = standingOrderRepository;
    }

    /**
     * Saves a {@link FRStandingOrder} within the repository.
     *
     * @param standingOrderData The {@link FRStandingOrder} containing the required standing order information.
     * @return The persisted {@link FRStandingOrder}.
     */
    public FRStandingOrder createStandingOrder(FRStandingOrderData standingOrderData) {
        log.debug("Create a standing order in the repository: {}", standingOrderData);

        FRStandingOrder frStandingOrder = FRStandingOrder.builder()
                .id(UUID.randomUUID().toString())
                .standingOrder(standingOrderData)
                .accountId(standingOrderData.getAccountId())
                .status(FRStandingOrder.StandingOrderStatus.PENDING)
                // TODO - do we need to persist the pispId?
                //.pispId(pispId)
                .build();
        return standingOrderRepository.save(frStandingOrder);
    }
}
