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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.factory;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAccountIdentifierConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationConsentData;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import org.springframework.stereotype.Component;
import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentDataResponse1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentResponse1;

@Component
public class OBFundsConfirmationConsentResponseFactory {

    public OBFundsConfirmationConsentResponse1 buildConsentResponse(FundsConfirmationConsent consent, Class<?> controllerClass) {
        final FRFundsConfirmationConsentData frFundsConfirmationConsentData = consent.getRequestObj().getData();
        return new OBFundsConfirmationConsentResponse1()
                .data(
                        new OBFundsConfirmationConsentDataResponse1()
                                .consentId(consent.getId())
                                .creationDateTime(consent.getCreationDateTime())
                                .statusUpdateDateTime(consent.getStatusUpdateDateTime())
                                .status(OBExternalRequestStatus1Code.fromValue(consent.getStatus()))
                                .expirationDateTime(frFundsConfirmationConsentData.getExpirationDateTime())
                                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(frFundsConfirmationConsentData.getDebtorAccount()))
                )
                .meta(PaginationUtil.generateMetaData(1))
                .links(LinksHelper.createFundsConfirmationConsentSelfLink(controllerClass, consent.getId()));
    }
}
