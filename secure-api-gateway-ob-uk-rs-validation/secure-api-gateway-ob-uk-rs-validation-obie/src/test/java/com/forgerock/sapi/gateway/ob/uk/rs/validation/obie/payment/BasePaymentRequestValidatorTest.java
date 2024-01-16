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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.BasePaymentRequestValidator.PaymentRequestValidationContext;

import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentRequestData;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPInitiation;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPRequest;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPRequestData;

class BasePaymentRequestValidatorTest {

    @Test
    void failsToConstructPaymentRequestValidationContextWithMissingParams() {
        assertThrowsNullPointerException("paymentRequest must be supplied",
                () -> new PaymentRequestValidationContext(null, null, null, null, null, null));
        assertThrowsNullPointerException("paymentRequestInitiationSupplier must be supplied",
                () -> new PaymentRequestValidationContext(new Object(), null, null, null, null, null));
        assertThrowsNullPointerException("paymentRequestRiskSupplier must be supplied",
                () -> new PaymentRequestValidationContext(new Object(), Object::new, null, null, null, null));
        assertThrowsNullPointerException("consentStatus must be supplied",
                () -> new PaymentRequestValidationContext(new Object(), Object::new, OBRisk1::new, null, null, null));
        assertThrowsNullPointerException("consentInitiationSupplier must be supplied",
                () -> new PaymentRequestValidationContext(new Object(), Object::new, OBRisk1::new, "status", null, null));
        assertThrowsNullPointerException("consentRiskSupplier must be supplied",
                () -> new PaymentRequestValidationContext(new Object(), Object::new, OBRisk1::new, "status", Object::new, null));
    }

    @Test
    void testContextGetters() {
        final OBDomesticVRPRequest paymentRequest = new OBDomesticVRPRequest();
        final OBRisk1 paymentRequestRisk = new OBRisk1();
        paymentRequest.setRisk(paymentRequestRisk);

        final OBDomesticVRPRequestData paymentRequestData = new OBDomesticVRPRequestData();
        paymentRequest.setData(paymentRequestData);
        final OBDomesticVRPInitiation paymentRequestInitiation = new OBDomesticVRPInitiation();
        paymentRequestData.setInitiation(paymentRequestInitiation);

        final OBDomesticVRPConsentRequest consent = new OBDomesticVRPConsentRequest();
        final OBRisk1 consentRisk = new OBRisk1();
        consent.setRisk(consentRisk);

        final OBDomesticVRPConsentRequestData consentRequestData = new OBDomesticVRPConsentRequestData();
        consent.setData(consentRequestData);
        final OBDomesticVRPInitiation consentRequestInitiation = new OBDomesticVRPInitiation();
        consentRequestData.setInitiation(consentRequestInitiation);

        final PaymentRequestValidationContext<OBDomesticVRPRequest, OBDomesticVRPInitiation> validationContext = new PaymentRequestValidationContext<>(paymentRequest,
                () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk, "Authorised",
                () -> consent.getData().getInitiation(), consent::getRisk);

        assertEquals(paymentRequest, validationContext.getPaymentRequest());
        assertEquals(paymentRequestInitiation, validationContext.getPaymentRequestInitiation());
        assertEquals(paymentRequestRisk, validationContext.getPaymentRequestRisk());
        assertEquals(consentRequestInitiation, validationContext.getConsentInitiation());
        assertEquals(consentRisk, validationContext.getConsentRisk());
        assertEquals("Authorised", validationContext.getConsentStatus());
    }

    private static void assertThrowsNullPointerException(String errorMsg, Executable executable) {
        final NullPointerException npe = assertThrows(NullPointerException.class, executable);
        assertEquals(errorMsg, npe.getMessage());
    }

}