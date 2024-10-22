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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.v4.accounts.products;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("V4.0.0FRProductRepository")
public interface FRProductRepository extends MongoRepository<FRProduct, String>, FRProductRepositoryCustom {

    Page<FRProduct> findByAccountId(@Param("accountId") String accountId, Pageable pageable);

    Page<FRProduct> findByAccountIdIn(@Param("accountIds") List<String> accountIds, Pageable pageable);

    Long deleteProductByAccountId(@Param("accountId") String accountId);

}
