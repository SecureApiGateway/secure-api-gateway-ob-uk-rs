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

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.vrp.FRDomesticVRPConsentConverters.toOBDomesticVRPConsentRequest;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;

import uk.org.openbanking.datamodel.v3.common.Meta;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentRequestData;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentResponse;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentResponseData;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentResponseDataStatus;

@Component
public class OBDomesticVRPConsentResponseFactory {

    public OBDomesticVRPConsentResponse buildConsentResponse(DomesticVRPConsent consent, Class<?> controllerClass) {
        final OBDomesticVRPConsentRequest obDomesticVRPConsentRequest = toOBDomesticVRPConsentRequest(consent.getRequestObj());
        final OBDomesticVRPConsentRequestData consentRequestData = obDomesticVRPConsentRequest.getData();

        return new OBDomesticVRPConsentResponse()
                .data(new OBDomesticVRPConsentResponseData().consentId(consent.getId())
                                                            .readRefundAccount(consentRequestData.getReadRefundAccount())
                                                            .controlParameters(consentRequestData.getControlParameters())
                                                            .initiation(consentRequestData.getInitiation())
                                                            .creationDateTime(new DateTime(consent.getCreationDateTime()))
                                                            .status(OBDomesticVRPConsentResponseDataStatus.fromValue(consent.getStatus()))
                                                            .statusUpdateDateTime(new DateTime(consent.getStatusUpdateDateTime())))
                .risk(obDomesticVRPConsentRequest.getRisk())
                .links(LinksHelper.createDomesticVrpConsentLink(controllerClass, consent.getId()))
                .meta(new Meta());
    }

}
