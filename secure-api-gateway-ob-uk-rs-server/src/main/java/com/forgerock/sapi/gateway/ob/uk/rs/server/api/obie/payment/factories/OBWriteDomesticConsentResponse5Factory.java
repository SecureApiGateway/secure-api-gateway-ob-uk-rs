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

import static com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper.createDomesticPaymentConsentsLink;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;

import uk.org.openbanking.datamodel.v3.common.Meta;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsentResponse5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsentResponse5Data;

/**
 * Factory to create {@link OBWriteDomesticConsentResponse5}
 *
 * The response is built from a {@link DomesticPaymentConsent}
 */
@Component
public class OBWriteDomesticConsentResponse5Factory {

    public OBWriteDomesticConsentResponse5 buildConsentResponse(DomesticPaymentConsent domesticPaymentConsent, Class<?> controllerClass) {
        final OBWriteDomesticConsentResponse5Data data = new OBWriteDomesticConsentResponse5Data();

        final OBWriteDomesticConsent4 obWriteDomesticConsent4 = FRWriteDomesticConsentConverter.toOBWriteDomesticConsent4(domesticPaymentConsent.getRequestObj());
        final OBWriteDomesticConsent4Data obConsentData = obWriteDomesticConsent4.getData();
        data.authorisation(obConsentData.getAuthorisation());
        data.readRefundAccount(obConsentData.getReadRefundAccount());
        data.scASupportData(obConsentData.getScASupportData());
        data.initiation(obConsentData.getInitiation());
        data.charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(domesticPaymentConsent.getCharges()));
        data.consentId(domesticPaymentConsent.getId());
        data.status(OBPaymentConsentStatus.fromValue(domesticPaymentConsent.getStatus()));
        data.creationDateTime(new DateTime(domesticPaymentConsent.getCreationDateTime()));
        data.statusUpdateDateTime(new DateTime(domesticPaymentConsent.getStatusUpdateDateTime()));

        return new OBWriteDomesticConsentResponse5().data(data)
                                                    .risk(obWriteDomesticConsent4.getRisk())
                                                    .links(createDomesticPaymentConsentsLink(controllerClass, domesticPaymentConsent.getId()))
                                                    .meta(new Meta());
    }
}
