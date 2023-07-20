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
package com.forgerock.sapi.gateway.rs.resource.store.api.resource.customerinfo;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.customerinfo.FRCustomerInfoConverter;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.customerinfo.FRCustomerInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller("CustomerInfoResourceApi")
@Slf4j
public class CustomerInfoResourceApiController implements CustomerInfoResourceApi {

    private final FRCustomerInfoRepository customerInfoRepository;

    public CustomerInfoResourceApiController(FRCustomerInfoRepository customerInfoRepository) {
        this.customerInfoRepository = customerInfoRepository;
    }

    @Override
    public ResponseEntity<FRCustomerInfo> getCustomerInformation(String userId) {
        log.info("Attempting to retrieve customer information by user Id: {}", userId);
        FRCustomerInfo customerInfo = FRCustomerInfoConverter.entityToDto(
                customerInfoRepository.findByUserID(userId)
        );

        if (customerInfo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                customerInfo
        );
    }
}
