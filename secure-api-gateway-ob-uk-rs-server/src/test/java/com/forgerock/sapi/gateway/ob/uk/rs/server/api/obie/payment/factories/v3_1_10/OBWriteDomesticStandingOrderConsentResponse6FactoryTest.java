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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v3_1_10;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.util.BeanValidationTestUtils.verifyBeanValidationIsSuccessful;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.domesticstandingorders.DomesticStandingOrderConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticstandingorders.DomesticStandingOrderConsentsApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;

import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsent5Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsentResponse6;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsentResponse6Data;
import uk.org.openbanking.testsupport.v3.payment.OBWriteDomesticStandingOrderConsentTestDataFactory;

class OBWriteDomesticStandingOrderConsentResponse6FactoryTest {

    private final OBWriteDomesticStandingOrderConsentResponse6Factory factory = new OBWriteDomesticStandingOrderConsentResponse6Factory();

    @Test
    void testCreateConsentResponse() {
        final OBWriteDomesticStandingOrderConsent5 consentRequest = OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBWriteDomesticStandingOrderConsent5();
        final OBWriteDomesticStandingOrderConsent5Data requestData = consentRequest.getData();
        final DomesticStandingOrderConsent consent = DomesticStandingOrderConsentsApiControllerTest.buildAwaitingAuthorisationConsent(consentRequest);
        final OBWriteDomesticStandingOrderConsentResponse6 response = factory.buildConsentResponse(consent, DomesticStandingOrderConsentsApi.class);

        final OBWriteDomesticStandingOrderConsentResponse6Data responseData = response.getData();
        assertThat(responseData.getStatus()).isEqualTo(OBPaymentConsentStatus.AWAITINGAUTHORISATION);
        assertThat(responseData.getConsentId()).isEqualTo(consent.getId());
        assertThat(responseData.getCreationDateTime()).isEqualTo(new DateTime(consent.getCreationDateTime()));
        assertThat(responseData.getStatusUpdateDateTime()).isEqualTo(new DateTime(consent.getStatusUpdateDateTime()));
        assertThat(responseData.getCharges()).isEqualTo(consent.getCharges());

        // Verify data against original Consent Request
        assertThat(responseData.getScASupportData()).isEqualTo(requestData.getScASupportData());
        assertThat(responseData.getReadRefundAccount()).isEqualTo(requestData.getReadRefundAccount());
        assertThat(responseData.getAuthorisation()).isEqualTo(requestData.getAuthorisation());
        assertThat(responseData.getInitiation()).usingRecursiveComparison().isEqualTo(requestData.getInitiation());
        assertThat(responseData.getPermission()).isEqualTo(requestData.getPermission());

        // Not currently generating data for these optional response fields
        assertThat(responseData.getCutOffDateTime()).isNull();

        assertThat(response.getRisk()).isEqualTo(consentRequest.getRisk());
        assertThat(response.getLinks().getSelf().toString())
                .endsWith("/open-banking/v3.1.10/pisp/domestic-standing-order-consents/" + consent.getId());
        assertThat(response.getMeta()).isNotNull();

        verifyBeanValidationIsSuccessful(response);
    }
    
}