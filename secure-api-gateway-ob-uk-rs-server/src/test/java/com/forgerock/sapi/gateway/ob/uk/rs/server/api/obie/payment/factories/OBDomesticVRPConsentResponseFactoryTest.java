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

import static com.forgerock.sapi.gateway.ob.uk.rs.server.util.BeanValidationTestUtils.verifyBeanValidationIsSuccessful;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp.DomesticVrpConsentsApiController;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp.DomesticVrpConsentsApiControllerTest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;

import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponse;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponseData;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponseData.StatusEnum;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;

class OBDomesticVRPConsentResponseFactoryTest {

    private final OBDomesticVRPConsentResponseFactory responseFactory = new OBDomesticVRPConsentResponseFactory();

    @Test
    void buildDomesticVRPConsentResponse() {
        final OBDomesticVRPConsentRequest consentRequest = OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest();
        final DomesticVRPConsent consent = DomesticVrpConsentsApiControllerTest.buildAwaitingAuthorisationConsent(consentRequest);
        final OBDomesticVRPConsentResponse response = responseFactory.buildConsentResponse(consent, DomesticVrpConsentsApiController.class);

        final OBDomesticVRPConsentResponseData responseData = response.getData();
        assertThat(responseData.getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION);
        assertThat(responseData.getConsentId()).isEqualTo(consent.getId());
        assertThat(responseData.getCreationDateTime()).isEqualTo(consent.getCreationDateTime());
        assertThat(responseData.getStatusUpdateDateTime()).isEqualTo(consent.getStatusUpdateDateTime());

        // Verify data against original Consent Request
        assertThat(responseData.getReadRefundAccount()).isEqualTo(consentRequest.getData().getReadRefundAccount());
        assertThat(responseData.getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(responseData.getControlParameters()).isEqualTo(consentRequest.getData().getControlParameters());

        assertThat(response.getRisk()).isEqualTo(consentRequest.getRisk());
        assertThat(response.getLinks().getSelf().toString())
                .endsWith("/open-banking/v3.1.10/pisp/domestic-vrp-consents/" + consent.getId());
        assertThat(response.getMeta()).isNotNull();

        verifyBeanValidationIsSuccessful(response);

    }
}