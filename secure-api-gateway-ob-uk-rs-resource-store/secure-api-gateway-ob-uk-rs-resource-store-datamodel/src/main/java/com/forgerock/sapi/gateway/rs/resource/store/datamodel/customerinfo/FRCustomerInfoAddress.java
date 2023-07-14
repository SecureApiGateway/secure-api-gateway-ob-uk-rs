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
package com.forgerock.sapi.gateway.rs.resource.store.datamodel.customerinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.org.openbanking.datamodel.customerinfo.CustomerInfoAddress;

import javax.validation.Valid;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FRCustomerInfoAddress {

    public FRCustomerInfoAddress(CustomerInfoAddress customerInfoAddress) {
        this.addressType = FRAddressTypeCode.fromValue(customerInfoAddress.getAddressType().toString());
        this.streetAddress = customerInfoAddress.getStreetAddress();
        this.postalCode = customerInfoAddress.getPostalCode();
        this.country = customerInfoAddress.getCountry();
    }

    private FRAddressTypeCode addressType;
    @Valid
    private List<String> streetAddress = null;
    private String postalCode;
    private String country;
}
