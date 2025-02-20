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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteFileConsentConverter;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import uk.org.openbanking.datamodel.v4.common.Meta;
import uk.org.openbanking.datamodel.v4.common.OBStatusReason;
import uk.org.openbanking.datamodel.v4.payment.*;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRConsentStatusConverter.toOBFilePaymentConsentStatusV4;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper.createFilePaymentConsentsLink;

import java.util.Collections;

@Component("OBWriteFileConsentResponse4FactoryV4.0.0")
public class OBWriteFileConsentResponse4Factory {

    public OBWriteFileConsentResponse4 buildConsentResponse(FilePaymentConsent consent, Class<?> controllerClass) {
        final OBWriteFileConsentResponse4Data data = new OBWriteFileConsentResponse4Data();

        final OBWriteFileConsent3 oBWriteDomesticStandingOrderConsent5 = FRWriteFileConsentConverter.toOBWriteFileConsent3(consent.getRequestObj());
        final OBWriteFileConsent3Data obConsentData = oBWriteDomesticStandingOrderConsent5.getData();
        data.authorisation(obConsentData.getAuthorisation());
        data.scASupportData(obConsentData.getScASupportData());
        data.initiation(obConsentData.getInitiation());
        data.charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()));
        data.consentId(consent.getId());
        data.status(toOBFilePaymentConsentStatusV4(consent.getStatus()));
        data.creationDateTime(new DateTime(consent.getCreationDateTime()));
        data.statusUpdateDateTime(new DateTime(consent.getStatusUpdateDateTime()));
        data.statusReason(Collections.singletonList(FRModelMapper.map(data.getStatusReason(), OBStatusReason.class)));

        return new OBWriteFileConsentResponse4().data(data)
                .links(createFilePaymentConsentsLink(controllerClass, consent.getId()))
                .meta(new Meta());
    }

}
