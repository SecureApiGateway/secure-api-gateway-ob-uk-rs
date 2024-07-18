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

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalStandingOrderConsentConverter.toOBWriteInternationalStandingOrderConsent6;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalStandingOrderConsent;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.InternationalStandingOrderConsent;

import uk.org.openbanking.datamodel.v3.common.Meta;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsent6Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsentResponse7;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsentResponse7Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsentResponse7DataInitiation;

@Component
public class OBWriteInternationalStandingOrderConsentResponse7Factory {

    public OBWriteInternationalStandingOrderConsentResponse7 buildConsentResponse(InternationalStandingOrderConsent consent, Class<?> controllerClass) {
        final FRWriteInternationalStandingOrderConsent consentRequest = consent.getRequestObj();
        final OBWriteInternationalStandingOrderConsent6 obConsent = toOBWriteInternationalStandingOrderConsent6(consentRequest);
        final OBWriteInternationalStandingOrderConsent6Data obConsentData = obConsent.getData();

        final OBWriteInternationalStandingOrderConsentResponse7Data data = new OBWriteInternationalStandingOrderConsentResponse7Data();
        data.permission(obConsentData.getPermission());
        data.authorisation(obConsentData.getAuthorisation());
        data.readRefundAccount(obConsentData.getReadRefundAccount());
        data.scASupportData(obConsentData.getScASupportData());
        // Annoying quirk of the OB schema, consent request and response initiation types are different but produce identical json
        data.initiation(FRModelMapper.map(obConsentData.getInitiation(), OBWriteInternationalStandingOrderConsentResponse7DataInitiation.class));
        data.charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()));
        data.consentId(consent.getId());
        data.status(OBPaymentConsentStatus.fromValue(consent.getStatus()));
        data.creationDateTime(new DateTime(consent.getCreationDateTime()));
        data.statusUpdateDateTime(new DateTime(consent.getStatusUpdateDateTime()));

        return new OBWriteInternationalStandingOrderConsentResponse7()
                .data(data)
                .risk(obConsent.getRisk())
                .meta(new Meta())
                .links(LinksHelper.createInternationalStandingOrderConsentsLink(controllerClass, consent.getId()));
    }
}
