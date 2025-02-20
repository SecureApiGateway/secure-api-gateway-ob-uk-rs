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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.standingorders;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStandingOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class FRStandingOrderRepositoryImpl implements FRStandingOrderRepositoryCustom {

    @Autowired
    @Lazy
    private FRStandingOrderRepository standingOrderRepository;

    @Override
    public Page<FRStandingOrder> byAccountIdWithPermissions(String accountId, List<FRExternalPermissionsCode> permissions, Pageable pageable) {
        return filter(standingOrderRepository.findByAccountId(accountId, pageable), permissions);
    }

    @Override
    public Page<FRStandingOrder> byAccountIdInWithPermissions(List<String> accountIds, List<FRExternalPermissionsCode> permissions, Pageable pageable) {
        return filter(standingOrderRepository.findByAccountIdIn(accountIds, pageable), permissions);
    }

    private Page<FRStandingOrder> filter(Page<FRStandingOrder> standingOrders, List<FRExternalPermissionsCode> permissions) {
        for (FRExternalPermissionsCode permission : permissions) {
            switch (permission) {

                case READSTANDINGORDERSBASIC:
                    for (FRStandingOrder standingOrder : standingOrders) {
                        standingOrder.getStandingOrder().setCreditorAccount(null);
                    }
                    break;
            }
        }
        return standingOrders;
    }
}
