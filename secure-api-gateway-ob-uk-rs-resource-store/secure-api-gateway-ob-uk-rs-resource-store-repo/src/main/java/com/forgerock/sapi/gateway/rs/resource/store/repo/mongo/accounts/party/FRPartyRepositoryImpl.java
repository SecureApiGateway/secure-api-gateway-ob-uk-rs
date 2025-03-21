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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.party;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRParty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class FRPartyRepositoryImpl implements FRPartyRepositoryCustom {
    @Autowired
    @Lazy
    private FRPartyRepository party1Repository;

    @Override
    public FRParty byAccountIdWithPermissions(String accountId, List<FRExternalPermissionsCode> permissions) {
        return filter(party1Repository.findByAccountId(accountId), permissions);
    }

    @Override
    public FRParty byUserIdWithPermissions(String userId, List<FRExternalPermissionsCode> permissions) {
        return filter(party1Repository.findByUserId(userId), permissions);
    }

    @Override
    public Page<FRParty> byAccountIdInWithPermissions(List<String> accountIds, List<FRExternalPermissionsCode> permissions, Pageable pageable) {
        return filter(party1Repository.findByAccountIdIn(accountIds, pageable), permissions);
    }

    private Page<FRParty> filter(Page<FRParty> parties, List<FRExternalPermissionsCode> permissions) {
        return parties;
    }

    private FRParty filter(FRParty party, List<FRExternalPermissionsCode> permissions) {
        return party;
    }
}
