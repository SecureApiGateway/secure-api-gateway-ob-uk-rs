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
package com.forgerock.sapi.gateway.rs.resource.store.api.testsupport;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRAddressTypeCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfoAddress;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class FRCustomerInfoTestHelper {

    public static FRCustomerInfo aValidFRCustomerInfo() {
        return aValidFRCustomerInfo(UUID.randomUUID().toString());
    }

    public static FRCustomerInfo aValidFRCustomerInfo(String userId) {
        return FRCustomerInfo.builder()
                .userID(userId)
                .userName("Joe.Doe")
                .address(FRCustomerInfoTestHelper.aValidFRCustomerInfoAddress())
                .birthdate(new DateTime().minusYears(19).toLocalDate())
                .email("joe.doe@acme.com")
                .familyName("Joe")
                .givenName("Doe")
                .initials("JD")
                .title("Mr")
                .partyId("party-Id")
                .phoneNumber("+44 7777 777777").build();
    }

    public static FRCustomerInfo aValidFRCustomerInfo(String userId, String userName) {
        return FRCustomerInfo.builder()
                .userID(userId)
                .userName(userName)
                .address(FRCustomerInfoTestHelper.aValidFRCustomerInfoAddress())
                .birthdate(new DateTime().minusYears(19).toLocalDate())
                .email("joe.doe@acme.com")
                .familyName("Joe")
                .givenName("Doe")
                .initials("JD")
                .title("Mr")
                .partyId("party-Id")
                .phoneNumber("+44 7777 777777").build();
    }

    public static FRCustomerInfoAddress aValidFRCustomerInfoAddress() {
        return FRCustomerInfoAddress.builder()
                .streetAddress(List.of("999", "Letsbe Avenue", "Chelmsford", "Essex"))
                .addressType(FRAddressTypeCode.RESIDENTIAL)
                .country("UK")
                .postalCode("ES12 3RR").build();
    }
}
