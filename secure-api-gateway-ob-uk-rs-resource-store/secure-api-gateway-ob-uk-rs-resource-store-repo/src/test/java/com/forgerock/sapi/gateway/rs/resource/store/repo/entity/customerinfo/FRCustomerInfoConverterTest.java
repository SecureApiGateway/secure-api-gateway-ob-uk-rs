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
package com.forgerock.sapi.gateway.rs.resource.store.repo.entity.customerinfo;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRAddressTypeCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfoAddress;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FRCustomerInfoConverterTest {

    @Test
    public void shouldConvertEntityToDto() {
        // Given
        FRCustomerInfoEntity customerInfoEntity = aValidFRCustomerInfoEntity();
        // When
        FRCustomerInfo customerInfo = FRCustomerInfoConverter.entityToDto(customerInfoEntity);
        // Then
        assertThat(customerInfoEntity.getUserID()).isEqualTo(customerInfo.getUserID());
        assertThat(customerInfoEntity.getUserName()).isEqualTo(customerInfo.getUserName());
        assertThat(customerInfoEntity.getFamilyName()).isEqualTo(customerInfo.getFamilyName());
        assertThat(customerInfoEntity.getGivenName()).isEqualTo(customerInfo.getGivenName());
        assertThat(customerInfoEntity.getInitials()).isEqualTo(customerInfo.getInitials());
        assertThat(customerInfoEntity.getPartyId()).isEqualTo(customerInfo.getPartyId());
        assertThat(customerInfoEntity.getPhoneNumber()).isEqualTo(customerInfo.getPhoneNumber());
        assertThat(customerInfoEntity.getEmail()).isEqualTo(customerInfo.getEmail());
        assertThat(customerInfoEntity.getPhoneNumber()).isEqualTo(customerInfo.getPhoneNumber());
        assertThat(customerInfoEntity.getBirthdate()).isEqualTo(customerInfo.getBirthdate());
        assertThat(customerInfoEntity.getAddress()).isEqualTo(customerInfo.getAddress());
    }

    @Test
    public void shouldConvertDtoToEntity() {
        // Given
        FRCustomerInfo customerInfo = aValidFRCustomerInfo();
        // When
        FRCustomerInfoEntity customerInfoEntity = FRCustomerInfoConverter.dtoToEntity(customerInfo);
        // Then
        assertThat(customerInfo.getUserID()).isEqualTo(customerInfoEntity.getUserID());
        assertThat(customerInfo.getUserName()).isEqualTo(customerInfoEntity.getUserName());
        assertThat(customerInfo.getFamilyName()).isEqualTo(customerInfoEntity.getFamilyName());
        assertThat(customerInfo.getGivenName()).isEqualTo(customerInfoEntity.getGivenName());
        assertThat(customerInfo.getInitials()).isEqualTo(customerInfoEntity.getInitials());
        assertThat(customerInfo.getPartyId()).isEqualTo(customerInfoEntity.getPartyId());
        assertThat(customerInfo.getPhoneNumber()).isEqualTo(customerInfoEntity.getPhoneNumber());
        assertThat(customerInfo.getEmail()).isEqualTo(customerInfoEntity.getEmail());
        assertThat(customerInfo.getPhoneNumber()).isEqualTo(customerInfoEntity.getPhoneNumber());
        assertThat(customerInfo.getBirthdate()).isEqualTo(customerInfoEntity.getBirthdate());
        assertThat(customerInfo.getAddress()).isEqualTo(customerInfoEntity.getAddress());
    }

    public static FRCustomerInfoEntity aValidFRCustomerInfoEntity() {
        return FRCustomerInfoEntity.builder()
                .id(UUID.randomUUID().toString())
                .userID(UUID.randomUUID().toString())
                .userName("Joe.Doe")
                .address(aValidFRCustomerInfoAddress())
                .birthdate(new DateTime().minusYears(19).toLocalDate())
                .email("joe.doe@acme.com")
                .familyName("Joe")
                .givenName("Doe")
                .initials("JD")
                .title("Mr")
                .partyId("party-Id")
                .phoneNumber("+44 7777 777777").build();
    }

    public static FRCustomerInfo aValidFRCustomerInfo() {
        return FRCustomerInfo.builder()
                .id(UUID.randomUUID().toString())
                .userID(UUID.randomUUID().toString())
                .userName("Joe.Doe")
                .address(aValidFRCustomerInfoAddress())
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
