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

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.domesticscheduledpayments.DomesticScheduledPaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticscheduledpayments.DomesticScheduledPaymentConsentsApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsentResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsentResponse5Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsentResponse5Data.StatusEnum;
import uk.org.openbanking.testsupport.payment.OBWriteDomesticScheduledConsentTestDataFactory;

class OBWriteDomesticScheduledConsentResponse5FactoryTest {

    private final OBWriteDomesticScheduledConsentResponse5Factory factory = new OBWriteDomesticScheduledConsentResponse5Factory();

    @Test
    void testCreateConsentResponse() {
        final OBWriteDomesticScheduledConsent4 consentRequest = OBWriteDomesticScheduledConsentTestDataFactory.aValidOBWriteDomesticScheduledConsent4();
        final OBWriteDomesticScheduledConsent4Data requestData = consentRequest.getData();
        final DomesticScheduledPaymentConsent consent = DomesticScheduledPaymentConsentsApiControllerTest.buildAwaitingAuthorisationConsent(consentRequest);
        final OBWriteDomesticScheduledConsentResponse5 response = factory.buildConsentResponse(consent, DomesticScheduledPaymentConsentsApi.class);

        final OBWriteDomesticScheduledConsentResponse5Data responseData = response.getData();
        assertThat(responseData.getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION);
        assertThat(responseData.getConsentId()).isEqualTo(consent.getId());
        assertThat(responseData.getCreationDateTime()).isEqualTo(consent.getCreationDateTime());
        assertThat(responseData.getStatusUpdateDateTime()).isEqualTo(consent.getStatusUpdateDateTime());
        assertThat(responseData.getCharges()).isEqualTo(consent.getCharges());

        // Verify data against original Consent Request
        assertThat(responseData.getScASupportData()).isEqualTo(requestData.getScASupportData());
        assertThat(responseData.getReadRefundAccount()).isEqualTo(requestData.getReadRefundAccount());
        assertThat(responseData.getAuthorisation()).isEqualTo(requestData.getAuthorisation());
        assertThat(responseData.getInitiation()).isEqualTo(requestData.getInitiation());
        assertThat(responseData.getPermission()).isEqualTo(requestData.getPermission());

        // Not currently generating data for these optional response fields
        assertThat(responseData.getCutOffDateTime()).isNull();
        assertThat(responseData.getExpectedExecutionDateTime()).isNull();
        assertThat(responseData.getExpectedExecutionDateTime()).isNull();

        assertThat(response.getRisk()).isEqualTo(consentRequest.getRisk());
        assertThat(response.getLinks().getSelf().toString())
                .endsWith("/open-banking/v3.1.10/pisp/domestic-scheduled-payment-consents/" + consent.getId());
        assertThat(response.getMeta()).isNotNull();

        verifyBeanValidationIsSuccessful(response);
    }
}