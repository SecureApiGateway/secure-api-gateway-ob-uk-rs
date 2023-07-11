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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories;


import static com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper.createDomesticScheduledPaymentConsentsLink;

import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConsentConverter;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;

import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsentResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsentResponse5Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsentResponse5Data.StatusEnum;

@Component
public class OBWriteDomesticScheduledConsentResponse5Factory {

    public OBWriteDomesticScheduledConsentResponse5 buildConsentResponse(DomesticScheduledPaymentConsent consent, Class<?> controllerClass) {
        final OBWriteDomesticScheduledConsentResponse5Data data = new OBWriteDomesticScheduledConsentResponse5Data();

        final OBWriteDomesticScheduledConsent4 obWriteDomesticScheduledConsent4 = FRWriteDomesticScheduledConsentConverter.toOBWriteDomesticScheduledConsent4(consent.getRequestObj());
        final OBWriteDomesticScheduledConsent4Data obConsentData = obWriteDomesticScheduledConsent4.getData();
        data.authorisation(obConsentData.getAuthorisation());
        data.permission(obConsentData.getPermission());
        data.readRefundAccount(obConsentData.getReadRefundAccount());
        data.scASupportData(obConsentData.getScASupportData());
        data.initiation(obConsentData.getInitiation());
        data.charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()));
        data.consentId(consent.getId());
        data.status(StatusEnum.fromValue(consent.getStatus()));
        data.creationDateTime(consent.getCreationDateTime());
        data.statusUpdateDateTime(consent.getStatusUpdateDateTime());

        return new OBWriteDomesticScheduledConsentResponse5().data(data)
                .risk(obWriteDomesticScheduledConsent4.getRisk())
                .links(createDomesticScheduledPaymentConsentsLink(controllerClass, consent.getId()))
                .meta(new Meta());
    }
}
