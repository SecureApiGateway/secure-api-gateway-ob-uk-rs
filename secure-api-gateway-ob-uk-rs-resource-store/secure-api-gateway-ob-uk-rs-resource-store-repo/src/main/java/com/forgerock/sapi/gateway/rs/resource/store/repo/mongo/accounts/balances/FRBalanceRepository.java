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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRBalanceType;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FRBalanceRepository extends MongoRepository<FRBalance, String>, FRBalanceRepositoryCustom {

    Page<FRBalance> findByAccountId(@Param("accountId") String accountId, Pageable pageable);

    FRBalance findByAccountId(@Param("accountId") String accountId);

    Page<FRBalance> findByAccountIdIn(@Param("accountIds") List<String> accountIds, Pageable pageable);

    Collection<FRBalance> findByAccountIdIn(@Param("accountIds") List<String> accountIds);

    Optional<FRBalance> findByAccountIdAndBalanceType(@Param("accountId") String accountId, @Param("type") FRBalanceType type);

    Long deleteBalanceByAccountId(@Param("accountId") String accountId);

    Long countByAccountIdIn(Set<String> accountIds);
}
