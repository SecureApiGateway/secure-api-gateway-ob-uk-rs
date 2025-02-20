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
package com.forgerock.sapi.gateway.rs.resource.store.repo.entity.customerinfo;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;

public class FRCustomerInfoConverter {

    public static FRCustomerInfo entityToDto(FRCustomerInfoEntity customerInfoEntity) {
        if (customerInfoEntity != null) {
            return FRCustomerInfo.builder()
                    .id(customerInfoEntity.getId())
                    .userID(customerInfoEntity.getUserID())
                    .userName(customerInfoEntity.getUserName())
                    .partyId(customerInfoEntity.getPartyId())
                    .title(customerInfoEntity.getTitle())
                    .initials(customerInfoEntity.getInitials())
                    .familyName(customerInfoEntity.getFamilyName())
                    .givenName(customerInfoEntity.getGivenName())
                    .email(customerInfoEntity.getEmail())
                    .phoneNumber(customerInfoEntity.getPhoneNumber())
                    .birthdate(customerInfoEntity.getBirthdate())
                    .address(customerInfoEntity.getAddress())
                    .build();
        }
        return null;
    }

    public static FRCustomerInfoEntity dtoToEntity(FRCustomerInfo customerInfo) {
        if (customerInfo != null) {
            return FRCustomerInfoEntity.builder()
                    .id(customerInfo.getId())
                    .userID(customerInfo.getUserID())
                    .userName(customerInfo.getUserName())
                    .partyId(customerInfo.getPartyId())
                    .title(customerInfo.getTitle())
                    .initials(customerInfo.getInitials())
                    .familyName(customerInfo.getFamilyName())
                    .givenName(customerInfo.getGivenName())
                    .email(customerInfo.getEmail())
                    .phoneNumber(customerInfo.getPhoneNumber())
                    .birthdate(customerInfo.getBirthdate())
                    .address(customerInfo.getAddress())
                    .build();
        }
        return null;
    }
}
