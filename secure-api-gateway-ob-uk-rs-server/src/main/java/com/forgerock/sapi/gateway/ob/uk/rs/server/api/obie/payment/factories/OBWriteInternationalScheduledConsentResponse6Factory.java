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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRExchangeRateConverter.toOBWriteInternationalConsentResponse6DataExchangeRateInformation;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper.createInternationalScheduledPaymentConsentsLink;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsent;

import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsent5Data;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsentResponse6;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsentResponse6Data;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsentResponse6DataInitiation;

@Component
public class OBWriteInternationalScheduledConsentResponse6Factory {

    public OBWriteInternationalScheduledConsentResponse6 buildConsentResponse(InternationalScheduledPaymentConsent consent, Class<?> controllerClass) {
        final FRWriteInternationalScheduledConsent consentRequest = consent.getRequestObj();
        final OBWriteInternationalScheduledConsent5 obConsent = FRWriteInternationalScheduledConsentConverter.toOBWriteInternationalScheduledConsent5(consentRequest);
        final OBWriteInternationalScheduledConsent5Data obConsentData = obConsent.getData();

        final OBWriteInternationalScheduledConsentResponse6Data data = new OBWriteInternationalScheduledConsentResponse6Data();
        data.permission(obConsentData.getPermission());
        data.authorisation(obConsentData.getAuthorisation());
        data.readRefundAccount(obConsentData.getReadRefundAccount());
        data.scASupportData(obConsentData.getScASupportData());
        // Annoying quirk of the OB schema, consent request and response initiation types are different but produce identical json
        data.initiation(FRModelMapper.map(obConsentData.getInitiation(), OBWriteInternationalScheduledConsentResponse6DataInitiation.class));
        data.charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()));
        data.consentId(consent.getId());
        data.exchangeRateInformation(toOBWriteInternationalConsentResponse6DataExchangeRateInformation(consent.getExchangeRateInformation()));
        data.status(OBPaymentConsentStatus.fromValue(consent.getStatus()));
        data.creationDateTime(new DateTime(consent.getCreationDateTime()));
        data.statusUpdateDateTime(new DateTime(consent.getStatusUpdateDateTime()));

        return new OBWriteInternationalScheduledConsentResponse6()
                .data(data)
                .risk(obConsent.getRisk())
                .links(createInternationalScheduledPaymentConsentsLink(controllerClass, consent.getId()))
                .meta(new Meta());
    }
}
