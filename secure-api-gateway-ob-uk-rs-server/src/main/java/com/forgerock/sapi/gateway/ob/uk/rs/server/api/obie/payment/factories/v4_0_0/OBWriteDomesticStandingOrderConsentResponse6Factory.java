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

import static com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper.createDomesticStandingOrderConsentsLink;

import java.util.Collections;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRConsentStatusConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteDomesticStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;

import uk.org.openbanking.datamodel.v4.common.Meta;
import uk.org.openbanking.datamodel.v4.common.OBStatusReason;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsent5Data;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsentResponse6;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsentResponse6Data;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsentResponse6DataInitiation;

@Component("DSOCRv4.0.0Factory")
public class OBWriteDomesticStandingOrderConsentResponse6Factory {
    public OBWriteDomesticStandingOrderConsentResponse6 buildConsentResponse(DomesticStandingOrderConsent consent, Class<?> controllerClass) {
        final OBWriteDomesticStandingOrderConsentResponse6Data data = new OBWriteDomesticStandingOrderConsentResponse6Data();
        final OBWriteDomesticStandingOrderConsent5 oBWriteDomesticStandingOrderConsent5 = FRWriteDomesticStandingOrderConsentConverter.toOBWriteDomesticStandingOrderConsent5(consent.getRequestObj());
        final OBWriteDomesticStandingOrderConsent5Data obConsentData = oBWriteDomesticStandingOrderConsent5.getData();
        data.authorisation(obConsentData.getAuthorisation());
        data.permission(obConsentData.getPermission());
        data.readRefundAccount(obConsentData.getReadRefundAccount());
        data.scASupportData(obConsentData.getScASupportData());
        // consent request and response initiation types are different but produce identical json
        data.initiation(FRModelMapper.map(obConsentData.getInitiation(), OBWriteDomesticStandingOrderConsentResponse6DataInitiation.class));
        data.charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()));
        data.consentId(consent.getId());
        data.status(FRConsentStatusConverter.toOBPaymentConsentStatusV4(consent.getStatus()));
        data.creationDateTime(new DateTime(consent.getCreationDateTime()));
        data.statusUpdateDateTime(new DateTime(consent.getStatusUpdateDateTime()));
        data.statusReason(Collections.singletonList(FRModelMapper.map(data.getStatusReason(), OBStatusReason.class)));

        return new OBWriteDomesticStandingOrderConsentResponse6().data(data)
                .risk(oBWriteDomesticStandingOrderConsent5.getRisk())
                .links(createDomesticStandingOrderConsentsLink(controllerClass, consent.getId()))
                .meta(new Meta());
    }
}