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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v3_1_10;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper.createInternationalPaymentConsentsLink;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRExchangeRateConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRWriteInternationalConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;

import uk.org.openbanking.datamodel.v3.common.Meta;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalConsent5Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalConsentResponse6;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalConsentResponse6Data;

@Component
public class OBWriteInternationalConsentResponse6Factory {

    public OBWriteInternationalConsentResponse6 buildConsentResponse(InternationalPaymentConsent consent, Class<?> controllerClass) {
        final FRWriteInternationalConsent consentRequest = consent.getRequestObj();
        final OBWriteInternationalConsent5 obConsent = FRWriteInternationalConsentConverter.toOBWriteInternationalConsent5(consentRequest);
        final OBWriteInternationalConsent5Data obConsentData = obConsent.getData();

        final OBWriteInternationalConsentResponse6Data data = new OBWriteInternationalConsentResponse6Data();
        data.authorisation(obConsentData.getAuthorisation());
        data.readRefundAccount(obConsentData.getReadRefundAccount());
        data.scASupportData(obConsentData.getScASupportData());
        data.initiation(obConsentData.getInitiation());
        data.charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()));
        data.consentId(consent.getId());
        data.exchangeRateInformation(FRExchangeRateConverter.toOBWriteInternationalConsentResponse6DataExchangeRateInformation(consent.getExchangeRateInformation()));
        data.status(OBPaymentConsentStatus.fromValue(consent.getStatus()));
        data.creationDateTime(new DateTime(consent.getCreationDateTime()));
        data.statusUpdateDateTime(new DateTime(consent.getStatusUpdateDateTime()));

        return new OBWriteInternationalConsentResponse6()
                .data(data)
                .risk(obConsent.getRisk())
                .links(createInternationalPaymentConsentsLink(controllerClass, consent.getId()))
                .meta(new Meta());
    }
}
