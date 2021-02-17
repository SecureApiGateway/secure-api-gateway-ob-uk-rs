/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.service.standingorder;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRStandingOrder;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.standingorders.FRStandingOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRStandingOrder.StandingOrderStatus.PENDING;

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
                .status(PENDING)
                // TODO - do we need to persist the pispId?
                //.pispId(pispId)
                .build();
        return standingOrderRepository.save(frStandingOrder);
    }
}
