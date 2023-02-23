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
package com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.balances;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FRBalanceRepositoryImpl implements FRBalanceRepositoryCustom {
    private static final Logger LOGGER = LoggerFactory.getLogger(FRBalanceRepositoryImpl.class);

    @Autowired
    @Lazy
    private FRBalanceRepository balanceRepository;

    @Override
    public Page<FRBalance> byAccountIdWithPermissions(String accountId, List<FRExternalPermissionsCode> permissions,
                                                      Pageable pageable) {
        return filter(balanceRepository.findByAccountId(accountId, pageable), permissions);
    }

    @Override
    public Page<FRBalance> byAccountIdInWithPermissions(List<String> accountIds, List<FRExternalPermissionsCode> permissions,
                                                        Pageable pageable) {
        return filter(balanceRepository.findByAccountIdIn(accountIds, pageable), permissions);
    }

    private Page<FRBalance> filter(Page<FRBalance> balances, List<FRExternalPermissionsCode> permissions) {
        return balances;
    }
}
