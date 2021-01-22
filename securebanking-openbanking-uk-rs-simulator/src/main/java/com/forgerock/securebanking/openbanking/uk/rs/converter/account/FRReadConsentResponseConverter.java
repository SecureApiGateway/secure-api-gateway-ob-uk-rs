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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRReadConsentResponse;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRReadConsentResponseData;
import uk.org.openbanking.datamodel.account.OBReadConsentResponse1;
import uk.org.openbanking.datamodel.account.OBReadConsentResponse1Data;

public class FRReadConsentResponseConverter {

    public static FRReadConsentResponse toFRReadConsentResponse(OBReadConsentResponse1 response) {
        return response == null ? null : FRReadConsentResponse.builder()
                .data(toFRReadConsentResponseData(response.getData()))
                .risk(FRAccountRiskConverter.toFRAccountRisk(response.getRisk()))
                .links(FRLinksConverter.toFRLinks(response.getLinks()))
                .meta(FRMetaConverter.toFRMeta(response.getMeta()))
                .build();
    }

    public static FRReadConsentResponseData toFRReadConsentResponseData(OBReadConsentResponse1Data data) {
        return data == null ? null : FRReadConsentResponseData.builder()
                .consentId(data.getConsentId())
                .creationDateTime(data.getCreationDateTime())
                .status(FRExternalRequestStatusCodeConverter.toFRExternalRequestStatusCode(data.getStatus()))
                .statusUpdateDateTime(data.getStatusUpdateDateTime())
                .permissions(FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList(data.getPermissions()))
                .expirationDateTime(data.getExpirationDateTime())
                .transactionFromDateTime(data.getTransactionFromDateTime())
                .transactionToDateTime(data.getTransactionToDateTime())
                .build();
    }

    // FR to OB
    public static OBReadConsentResponse1 toOBReadConsentResponse1(FRReadConsentResponse accountAccessConsent) {
        return accountAccessConsent == null ? null : new OBReadConsentResponse1()
                .data(toOBReadConsentResponse1Data(accountAccessConsent.getData()))
                .risk(FRAccountRiskConverter.toOBRisk2(accountAccessConsent.getRisk()))
                .links(FRLinksConverter.toLinks(accountAccessConsent.getLinks()))
                .meta(FRMetaConverter.toMeta(accountAccessConsent.getMeta()));
    }

    public static OBReadConsentResponse1Data toOBReadConsentResponse1Data(FRReadConsentResponseData data) {
        return data == null ? null : new OBReadConsentResponse1Data()
        .consentId(data.getConsentId())
        .creationDateTime(data.getCreationDateTime())
        .status(FRExternalRequestStatusCodeConverter.toOBExternalRequestStatus1Code(data.getStatus()))
        .statusUpdateDateTime(data.getStatusUpdateDateTime())
        .permissions(FRExternalPermissionsCodeConverter.toOBExternalPermissions1CodeList(data.getPermissions()))
        .expirationDateTime(data.getExpirationDateTime())
        .transactionFromDateTime(data.getTransactionFromDateTime())
        .transactionToDateTime(data.getTransactionToDateTime());
    }
}
