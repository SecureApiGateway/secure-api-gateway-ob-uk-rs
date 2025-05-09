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

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRParty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FRPartyRepository extends MongoRepository<FRParty, String>, FRPartyRepositoryCustom {

    FRParty findByAccountId(@Param("accountId") String accountId);

    Page<FRParty> findByAccountIdIn(@Param("accountIds") List<String> accountIds, Pageable pageable);

    Long deleteFRPartyByAccountId(@Param("accountId") String accountId);

    Long deleteFRPartyByUserId(@Param("userId") String userId);

    FRParty findByUserId(@Param("userId") String userId);
}
