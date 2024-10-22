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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("V4.0.0FRProductRepositoryImpl")
public class FRProductRepositoryImpl implements FRProductRepositoryCustom {
    private static final Logger LOGGER = LoggerFactory.getLogger(FRProductRepositoryImpl.class);

    @Autowired
    @Lazy
    private FRProductRepository productRepository;

    @Override
    public Page<FRProduct> byAccountIdWithPermissions(String accountId, List<FRExternalPermissionsCode> permissions, Pageable pageable) {
        return filter(productRepository.findByAccountId(accountId, pageable), permissions);
    }

    @Override
    public Page<FRProduct> byAccountIdInWithPermissions(List<String> accountIds, List<FRExternalPermissionsCode> permissions, Pageable pageable) {
        return filter(productRepository.findByAccountIdIn(accountIds, pageable), permissions);
    }

    private Page<FRProduct> filter(Page<FRProduct> products, List<FRExternalPermissionsCode> permissions) {
        return products;
    }
}
