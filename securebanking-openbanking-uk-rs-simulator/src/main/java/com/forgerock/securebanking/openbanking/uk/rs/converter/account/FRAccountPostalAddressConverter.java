/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.converter.account;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRPostalAddress;
import uk.org.openbanking.datamodel.account.OBAddressTypeCode;
import uk.org.openbanking.datamodel.account.OBPostalAddress6;
import uk.org.openbanking.datamodel.account.OBPostalAddress8;

import java.util.List;
import java.util.stream.Collectors;

public class FRAccountPostalAddressConverter {

    // OB to FR
    public static List<FRPostalAddress> toFRPostalAddressList(List<OBPostalAddress8> addresses) {
        return addresses == null ? null : addresses.stream()
                .map(a -> toFRPostalAddress(a))
                .collect(Collectors.toList());
    }

    public static FRPostalAddress toFRPostalAddress(OBPostalAddress8 address) {
        return address == null ? null : FRPostalAddress.builder()
                .addressType(toAddressTypeCode(address.getAddressType()))
                .streetName(address.getStreetName())
                .buildingNumber(address.getBuildingNumber())
                .postCode(address.getPostCode())
                .townName(address.getTownName())
                .countrySubDivision(address.getCountrySubDivision())
                .country(address.getCountry())
                .addressLine(address.getAddressLine())
                .build();
    }

    public static FRPostalAddress toFRPostalAddress(OBPostalAddress6 address) {
        return address == null ? null : FRPostalAddress.builder()
                .addressType(toAddressTypeCode(address.getAddressType()))
                .department(address.getDepartment())
                .subDepartment(address.getSubDepartment())
                .streetName(address.getStreetName())
                .buildingNumber(address.getBuildingNumber())
                .postCode(address.getPostCode())
                .townName(address.getTownName())
                .countrySubDivision(address.getCountrySubDivision())
                .country(address.getCountry())
                .addressLine(address.getAddressLine())
                .build();
    }

    public static FRPostalAddress.AddressTypeCode toAddressTypeCode(OBAddressTypeCode addressType) {
        return addressType == null ? null : FRPostalAddress.AddressTypeCode.valueOf(addressType.name());
    }

    // FR to OB
    public static List<OBPostalAddress8> toOBPostalAddress8List(List<FRPostalAddress> addresses) {
        return addresses == null ? null : addresses.stream()
                .map(a -> toOBPostalAddress8(a))
                .collect(Collectors.toList());
    }

    public static OBPostalAddress6 toOBPostalAddress6(FRPostalAddress address) {
        return address == null ? null : new OBPostalAddress6()
                .addressType(toOBAddressTypeCode(address.getAddressType()))
                .department(address.getDepartment())
                .subDepartment(address.getSubDepartment())
                .streetName(address.getStreetName())
                .buildingNumber(address.getBuildingNumber())
                .postCode(address.getPostCode())
                .townName(address.getTownName())
                .countrySubDivision(address.getCountrySubDivision())
                .country(address.getCountry())
                .addressLine(address.getAddressLine());
    }

    public static OBPostalAddress8 toOBPostalAddress8(FRPostalAddress address) {
        return address == null ? null : new OBPostalAddress8()
                .addressType(toOBAddressTypeCode(address.getAddressType()))
                .addressLine(address.getAddressLine())
                .streetName(address.getStreetName())
                .buildingNumber(address.getBuildingNumber())
                .postCode(address.getPostCode())
                .townName(address.getTownName())
                .countrySubDivision(address.getCountrySubDivision())
                .country(address.getCountry());
    }

    public static OBAddressTypeCode toOBAddressTypeCode(FRPostalAddress.AddressTypeCode addressType) {
        return addressType == null ? null : OBAddressTypeCode.valueOf(addressType.name());
    }
}
