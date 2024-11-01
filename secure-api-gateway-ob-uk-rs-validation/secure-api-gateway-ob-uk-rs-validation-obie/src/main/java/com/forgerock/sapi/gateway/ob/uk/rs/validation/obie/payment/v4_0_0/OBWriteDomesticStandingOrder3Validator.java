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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.v4_0_0;

import java.util.function.Supplier;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.BasePaymentRequestValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.v4_0_0.OBWriteDomesticStandingOrder3Validator.OBWriteDomesticStandingOrder3ValidationContext;

import uk.org.openbanking.datamodel.v3.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrder3;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrder3DataInitiation;

/**
 * Validator of OBWriteDomesticStandingOrder3 objects (Domestic Standing Order Requests)
 */
public class OBWriteDomesticStandingOrder3Validator extends BasePaymentRequestValidator<OBWriteDomesticStandingOrder3ValidationContext, OBWriteDomesticStandingOrder3, OBWriteDomesticStandingOrder3DataInitiation> {

    public static class OBWriteDomesticStandingOrder3ValidationContext extends PaymentRequestValidationContext<OBWriteDomesticStandingOrder3, OBWriteDomesticStandingOrder3DataInitiation> {
/*        public OBWriteDomesticStandingOrder3ValidationContext(OBWriteDomesticStandingOrder3 paymentRequest, OBWriteDomesticStandingOrderConsent5 consentRequest, String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                    consentStatus, () -> consentRequest.getData().getInitiation(), consentRequest::getRisk);


        }*/

        public OBWriteDomesticStandingOrder3ValidationContext(OBWriteDomesticStandingOrder3 paymentRequest, Supplier<OBWriteDomesticStandingOrder3DataInitiation> paymentRequestInitiationSupplier, Supplier<OBRisk1> paymentRequestRiskSupplier, String consentStatus, Supplier<OBWriteDomesticStandingOrder3DataInitiation> consentInitiationSupplier, Supplier<OBRisk1> consentRiskSupplier) {
            super(paymentRequest, paymentRequestInitiationSupplier, paymentRequestRiskSupplier, consentStatus, consentInitiationSupplier, consentRiskSupplier);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteDomesticStandingOrder3ValidationContext paymentReqValidationCtxt,
            ValidationResult<OBError1> validationResult) {

        // Add validation rules as required.
    }
}
