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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRReadDataResponse;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRReadResponse;
import uk.org.openbanking.datamodel.account.OBReadDataResponse1;
import uk.org.openbanking.datamodel.account.OBReadResponse1;
import uk.org.openbanking.datamodel.account.OBRisk2;

public class FRReadResponseConverter {

    // OB to FR
    public static FRReadResponse toFRReadResponse(OBReadResponse1 response) {
        return response == null ? null : FRReadResponse.builder()
                .data(toFRReadDataResponse(response.getData()))
                .risk(FRAccountRiskConverter.toFRAccountRisk((OBRisk2)response.getRisk()))
                .links(FRLinksConverter.toFRLinks(response.getLinks()))
                .meta(FRMetaConverter.toFRMeta(response.getMeta()))
                .build();
    }

    public static FRReadDataResponse toFRReadDataResponse(OBReadDataResponse1 data) {
        return data == null ? null : FRReadDataResponse.builder()
                .accountRequestId(data.getAccountRequestId())
                .status(FRExternalRequestStatusCodeConverter.toFRExternalRequestStatusCode(data.getStatus()))
                .creationDateTime(data.getCreationDateTime())
                .permissions(FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList(data.getPermissions()))
                .expirationDateTime(data.getExpirationDateTime())
                .transactionFromDateTime(data.getTransactionFromDateTime())
                .transactionToDateTime(data.getTransactionToDateTime())
                .build();
    }

    // FR to OB
    public static OBReadResponse1 toOBReadResponse1(FRReadResponse response) {
        return response == null ? null : new OBReadResponse1()
                .data(toOBReadDataResponse1(response.getData()))
                .risk(FRAccountRiskConverter.toOBRisk2(response.getRisk()))
                .links(FRLinksConverter.toLinks(response.getLinks()))
                .meta(FRMetaConverter.toMeta(response.getMeta()));
    }

    public static OBReadDataResponse1 toOBReadDataResponse1(FRReadDataResponse data) {
        return data == null ? null : new OBReadDataResponse1()
                .accountRequestId(data.getAccountRequestId())
                .status(FRExternalRequestStatusCodeConverter.toOBExternalRequestStatus1Code(data.getStatus()))
                .creationDateTime(data.getCreationDateTime())
                .permissions(FRExternalPermissionsCodeConverter.toOBExternalPermissions1CodeList(data.getPermissions()))
                .expirationDateTime(data.getExpirationDateTime())
                .transactionFromDateTime(data.getTransactionFromDateTime())
                .transactionToDateTime(data.getTransactionToDateTime());
    }
}
