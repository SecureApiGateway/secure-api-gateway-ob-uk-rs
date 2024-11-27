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

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.file.FilePaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.file.FilePaymentConsentsApiControllerTest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import uk.org.openbanking.datamodel.v4.payment.*;
import uk.org.openbanking.testsupport.v4.payment.OBWriteFileConsentTestDataFactory;

import java.math.BigDecimal;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.util.BeanValidationTestUtils.verifyBeanValidationIsSuccessful;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.openbanking.datamodel.v4.payment.OBWriteFileConsentResponse4DataStatus.AWUP;

class OBWriteFileConsentResponse4FactoryTest {

    private final OBWriteFileConsentResponse4Factory factory = new OBWriteFileConsentResponse4Factory();

    @Test
    void testCreateConsentResponse() {
        final OBWriteFileConsent3 consentRequest = OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3(
                "test-file-type", "test-hash", "8", new BigDecimal("1234567890.19"));
        final OBWriteFileConsent3Data requestData = consentRequest.getData();
        final FilePaymentConsent consent = FilePaymentConsentsApiControllerTest.buildAwaitingUploadConsent(consentRequest);
        final OBWriteFileConsentResponse4 response = factory.buildConsentResponse(consent, FilePaymentConsentsApi.class);

        final OBWriteFileConsentResponse4Data responseData = response.getData();
        assertThat(responseData.getStatus()).isEqualTo(AWUP);
        assertThat(responseData.getConsentId()).isEqualTo(consent.getId());
        assertThat(responseData.getCreationDateTime()).isEqualTo(new DateTime(consent.getCreationDateTime()));
        assertThat(responseData.getStatusUpdateDateTime()).isEqualTo(new DateTime(consent.getStatusUpdateDateTime()));
        assertThat(responseData.getCharges()).isEqualTo(consent.getCharges());

        // Verify data against original Consent Request
        assertThat(responseData.getScASupportData()).isEqualTo(requestData.getScASupportData());
        assertThat(responseData.getAuthorisation()).isEqualTo(requestData.getAuthorisation());
        assertThat(responseData.getInitiation()).usingRecursiveComparison().isEqualTo(requestData.getInitiation());

        // Not currently generating data for these optional response fields
        assertThat(responseData.getCutOffDateTime()).isNull();

        assertThat(response.getLinks().getSelf().toString())
                .endsWith("/open-banking/v4.0.0/pisp/file-payment-consents/" + consent.getId());
        assertThat(response.getMeta()).isNotNull();

        verifyBeanValidationIsSuccessful(response);
    }
}