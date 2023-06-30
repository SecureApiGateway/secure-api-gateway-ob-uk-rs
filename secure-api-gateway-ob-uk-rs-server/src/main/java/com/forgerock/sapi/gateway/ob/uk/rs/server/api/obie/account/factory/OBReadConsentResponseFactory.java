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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.factory;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRExternalPermissionsCodeConverter.toOBExternalPermissions1CodeList;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRReadConsentData;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import uk.org.openbanking.datamodel.account.OBReadConsentResponse1;
import uk.org.openbanking.datamodel.account.OBReadConsentResponse1Data;
import uk.org.openbanking.datamodel.account.OBRisk2;
import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;

@Component
public class OBReadConsentResponseFactory {

    public OBReadConsentResponse1 buildConsentResponse(AccountAccessConsent consent, Class<?> controllerClass) {
        final OBRisk2 obRisk2 = new OBRisk2();
        obRisk2.setData(consent.getRequestObj().getRisk().getData());
        final FRReadConsentData readConsentData = consent.getRequestObj().getData();
        return new OBReadConsentResponse1()
                .data(new OBReadConsentResponse1Data().consentId(consent.getId())
                                                      .creationDateTime(consent.getCreationDateTime())
                                                      .statusUpdateDateTime(consent.getStatusUpdateDateTime())
                                                      .status(OBExternalRequestStatus1Code.fromValue(consent.getStatus()))
                                                      .permissions(toOBExternalPermissions1CodeList(readConsentData.getPermissions()))
                                                      .expirationDateTime(readConsentData.getExpirationDateTime())
                                                      .transactionFromDateTime(readConsentData.getTransactionFromDateTime())
                                                      .transactionToDateTime(readConsentData.getTransactionToDateTime()))
                .risk(obRisk2)
                .meta(PaginationUtil.generateMetaData(1))
                .links(LinksHelper.createAccountAccessConsentsSelfLink(controllerClass, consent.getId()));
    }
}
