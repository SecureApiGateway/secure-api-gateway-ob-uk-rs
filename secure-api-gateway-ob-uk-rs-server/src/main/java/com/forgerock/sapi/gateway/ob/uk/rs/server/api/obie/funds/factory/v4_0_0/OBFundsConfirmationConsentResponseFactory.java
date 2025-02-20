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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.factory.v4_0_0;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationConsentData;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import uk.org.openbanking.datamodel.v4.common.OBStatusReason;
import uk.org.openbanking.datamodel.v4.fund.OBFundsConfirmationConsentResponse1;
import uk.org.openbanking.datamodel.v4.fund.OBFundsConfirmationConsentResponse1Data;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRAccountIdentifierConverter.toOBFundsConfirmationConsent1DataDebtorAccount;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRFundsConfirmationConsentStatusConverter.toOBFundsConfirmationConsentStatusV4;

import java.util.Collections;

@Component("V4.0.0OBFundsConfirmationConsentResponseFactory")
public class OBFundsConfirmationConsentResponseFactory {

    public OBFundsConfirmationConsentResponse1 buildConsentResponse(FundsConfirmationConsent consent, Class<?> controllerClass) {
        final FRFundsConfirmationConsentData frFundsConfirmationConsentData = consent.getRequestObj().getData();
        final OBFundsConfirmationConsentResponse1Data data = new OBFundsConfirmationConsentResponse1Data();
        return new OBFundsConfirmationConsentResponse1()
                .data(
                        new OBFundsConfirmationConsentResponse1Data()
                                .consentId(consent.getId())
                                .creationDateTime(new DateTime(consent.getCreationDateTime()))
                                .statusUpdateDateTime(new DateTime(consent.getStatusUpdateDateTime()))
                                .status(toOBFundsConfirmationConsentStatusV4(consent.getStatus()))
                                .expirationDateTime(frFundsConfirmationConsentData.getExpirationDateTime())
                                .debtorAccount(toOBFundsConfirmationConsent1DataDebtorAccount(frFundsConfirmationConsentData.getDebtorAccount()))
                                .statusReason(Collections.singletonList(FRModelMapper.map(data.getStatusReason(), OBStatusReason.class)))
                )
                .meta(PaginationUtil.generateMetaData(1))
                .links(LinksHelper.createFundsConfirmationConsentSelfLink(controllerClass, consent.getId()));
    }
}
