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
///*
// * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v3_1_10;
//
//import static com.forgerock.sapi.gateway.ob.uk.rs.server.util.BeanValidationTestUtils.verifyBeanValidationIsSuccessful;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import org.joda.time.DateTime;
//import org.junit.jupiter.api.Test;
//
//import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticpayments.DomesticPaymentConsentsApiController;
//import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticpayments.DomesticPaymentConsentsApiControllerTest;
//import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
//
//import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
//import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4;
//import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4Data;
//import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsentResponse5;
//import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsentResponse5Data;
//import uk.org.openbanking.testsupport.v3.payment.OBWriteDomesticConsentTestDataFactory;
//
//class OBWriteDomesticConsentResponse5FactoryTest {
//
//    private final OBWriteDomesticConsentResponse5Factory factory = new OBWriteDomesticConsentResponse5Factory();
//
//    @Test
//    void testCreateOBWriteDomesticConsentResponse5() {
//        final OBWriteDomesticConsent4 consentRequest = OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4();
//        final OBWriteDomesticConsent4Data requestData = consentRequest.getData();
//        final DomesticPaymentConsent domesticPaymentConsent = DomesticPaymentConsentsApiControllerTest.buildAwaitingAuthorisationConsent(consentRequest);
//        final OBWriteDomesticConsentResponse5 response = factory.buildConsentResponse(domesticPaymentConsent, DomesticPaymentConsentsApiController.class);
//
//        final OBWriteDomesticConsentResponse5Data responseData = response.getData();
//        assertThat(responseData.getStatus()).isEqualTo(OBPaymentConsentStatus.AWAITINGAUTHORISATION);
//        assertThat(responseData.getConsentId()).isEqualTo(domesticPaymentConsent.getId());
//        assertThat(responseData.getCreationDateTime()).isEqualTo(new DateTime(domesticPaymentConsent.getCreationDateTime()));
//        assertThat(responseData.getStatusUpdateDateTime()).isEqualTo(new DateTime(domesticPaymentConsent.getStatusUpdateDateTime()));
//        assertThat(responseData.getCharges()).isEqualTo(domesticPaymentConsent.getCharges());
//
//        // Verify data against original Consent Request
//        assertThat(responseData.getScASupportData()).isEqualTo(requestData.getScASupportData());
//        assertThat(responseData.getReadRefundAccount()).isEqualTo(requestData.getReadRefundAccount());
//        assertThat(responseData.getAuthorisation()).isEqualTo(requestData.getAuthorisation());
//        assertThat(responseData.getInitiation()).isEqualTo(requestData.getInitiation());
//
//        // Not currently generating data for these optional response fields
//        assertThat(responseData.getCutOffDateTime()).isNull();
//        assertThat(responseData.getExpectedExecutionDateTime()).isNull();
//        assertThat(responseData.getExpectedExecutionDateTime()).isNull();
//
//        assertThat(response.getRisk()).isEqualTo(consentRequest.getRisk());
//        assertThat(response.getLinks().getSelf().toString())
//                .endsWith("/open-banking/v3.1.10/pisp/domestic-payment-consents/" + domesticPaymentConsent.getId());
//        assertThat(response.getMeta()).isNotNull();
//
//        verifyBeanValidationIsSuccessful(response);
//    }
//
//}