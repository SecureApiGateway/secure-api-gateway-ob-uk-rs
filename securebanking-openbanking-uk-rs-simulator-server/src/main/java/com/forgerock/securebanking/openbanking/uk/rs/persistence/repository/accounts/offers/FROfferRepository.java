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
package com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.offers;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FROffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;


public interface FROfferRepository extends MongoRepository<FROffer, String>, FROfferRepositoryCustom {

    Page<FROffer> findByAccountId(@Param("accountId") String accountId, Pageable pageable);

    Page<FROffer> findByAccountIdIn(@Param("accountIds") List<String> accountIds, Pageable pageable);

    Long deleteFROfferByAccountId(@Param("accountId") String accountId);

    Long countByAccountIdIn(Set<String> accountIds);
}
