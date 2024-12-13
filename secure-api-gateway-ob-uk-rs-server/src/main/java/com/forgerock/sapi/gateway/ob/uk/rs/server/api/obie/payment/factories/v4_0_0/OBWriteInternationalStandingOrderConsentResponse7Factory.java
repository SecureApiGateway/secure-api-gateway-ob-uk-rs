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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalStandingOrderConsentConverter.toOBWriteInternationalStandingOrderConsent6;

import java.util.Collections;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRInternationalConsentStatusConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalStandingOrderConsent;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.InternationalStandingOrderConsent;

import uk.org.openbanking.datamodel.v4.common.Meta;
import uk.org.openbanking.datamodel.v4.common.OBStatusReason;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalStandingOrder4DataInitiation;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalStandingOrderConsent6Data;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalStandingOrderConsentResponse7;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalStandingOrderConsentResponse7Data;

@Component("ISOCRv4.0.0Factory")
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
        // consent request and response initiation types are different but produce identical json
        data.initiation(FRModelMapper.map(obConsentData.getInitiation(), OBWriteInternationalStandingOrder4DataInitiation.class));
        data.charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()));
        data.consentId(consent.getId());
        data.status(FRInternationalConsentStatusConverter.toOBPaymentConsentStatusV4(consent.getStatus()));
        data.creationDateTime(new DateTime(consent.getCreationDateTime()));
        data.statusUpdateDateTime(new DateTime(consent.getStatusUpdateDateTime()));
        data.statusReason(Collections.singletonList(FRModelMapper.map(data.getStatusReason(), OBStatusReason.class)));

        return new OBWriteInternationalStandingOrderConsentResponse7()
                .data(data)
                .risk(obConsent.getRisk())
                .meta(new Meta())
                .links(LinksHelper.createInternationalStandingOrderConsentsLink(controllerClass, consent.getId()));
    }
}
