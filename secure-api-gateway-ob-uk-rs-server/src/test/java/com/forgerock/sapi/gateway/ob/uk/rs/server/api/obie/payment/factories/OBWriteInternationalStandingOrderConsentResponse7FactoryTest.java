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

import static com.forgerock.sapi.gateway.ob.uk.rs.server.util.BeanValidationTestUtils.verifyBeanValidationIsSuccessful;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.internationalstandingorders.InternationalStandingOrderConsentsApiController;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.internationalstandingorders.InternationalStandingOrderConsentsApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.InternationalStandingOrderConsent;

import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent6Data;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsentResponse7;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsentResponse7Data;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsentResponse7Data.StatusEnum;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalStandingOrderConsentTestDataFactory;

class OBWriteInternationalStandingOrderConsentResponse7FactoryTest {

    private final OBWriteInternationalStandingOrderConsentResponse7Factory factory = new OBWriteInternationalStandingOrderConsentResponse7Factory();

    @Test
    void testCreateOBWriteInternationalStandingOrderConsentResponse7() {
        final OBWriteInternationalStandingOrderConsent6 consentRequest = OBWriteInternationalStandingOrderConsentTestDataFactory.aValidOBWriteInternationalStandingOrderConsent6();
        final OBWriteInternationalStandingOrderConsent6Data requestData = consentRequest.getData();
        final InternationalStandingOrderConsent consent = InternationalStandingOrderConsentsApiControllerTest.buildAwaitingAuthorisationConsent(consentRequest);
        final OBWriteInternationalStandingOrderConsentResponse7 response = factory.buildConsentResponse(consent, InternationalStandingOrderConsentsApiController.class);

        final OBWriteInternationalStandingOrderConsentResponse7Data responseData = response.getData();
        assertThat(responseData.getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION);
        assertThat(responseData.getConsentId()).isEqualTo(consent.getId());
        assertThat(responseData.getCreationDateTime()).isEqualTo(new DateTime(consent.getCreationDateTime()));
        assertThat(responseData.getStatusUpdateDateTime()).isEqualTo(new DateTime(consent.getStatusUpdateDateTime()));
        assertThat(responseData.getCharges()).isEqualTo(consent.getCharges());

        // Verify data against original Consent Request
        assertThat(responseData.getScASupportData()).isEqualTo(requestData.getScASupportData());
        assertThat(responseData.getReadRefundAccount()).isEqualTo(requestData.getReadRefundAccount());
        assertThat(responseData.getAuthorisation()).isEqualTo(requestData.getAuthorisation());
        assertThat(responseData.getInitiation()).usingRecursiveComparison().isEqualTo(requestData.getInitiation());

        // Not currently generating data for these optional response fields
        assertThat(responseData.getCutOffDateTime()).isNull();

        assertThat(response.getRisk()).isEqualTo(consentRequest.getRisk());
        assertThat(response.getLinks().getSelf().toString())
                .endsWith("/open-banking/v3.1.10/pisp/international-standing-order-consents/" + consent.getId());
        assertThat(response.getMeta()).isNotNull();

        verifyBeanValidationIsSuccessful(response);
    }
}