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

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomesticStandingOrder3Validator.OBWriteDomesticStandingOrder3ValidationContext;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrder3;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrder3DataInitiation;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsent5;

/**
 * Validator of OBWriteDomesticStandingOrder3 objects (Domestic Standing Order Requests)
 */
public class OBWriteDomesticStandingOrder3Validator extends BasePaymentRequestValidator<OBWriteDomesticStandingOrder3ValidationContext, OBWriteDomesticStandingOrder3, OBWriteDomesticStandingOrder3DataInitiation> {

    public static class OBWriteDomesticStandingOrder3ValidationContext extends PaymentRequestValidationContext<OBWriteDomesticStandingOrder3, OBWriteDomesticStandingOrder3DataInitiation> {
        public OBWriteDomesticStandingOrder3ValidationContext(OBWriteDomesticStandingOrder3 paymentRequest, OBWriteDomesticStandingOrderConsent5 consentRequest, String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                  consentStatus, () -> consentRequest.getData().getInitiation(), consentRequest::getRisk);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteDomesticStandingOrder3ValidationContext paymentReqValidationCtxt,
                                               ValidationResult<OBError1> validationResult) {

        // No additional rules for OBWriteDomestic2 currently, add validation rules as required.
    }
}
