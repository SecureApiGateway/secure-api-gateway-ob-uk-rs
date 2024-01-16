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
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternational3Validator.OBWriteInternational3ValidationContext;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;

/**
 * Validator of OBWriteInternational3 objects (International Payment Requests)
 */
public class OBWriteInternational3Validator extends BasePaymentRequestValidator<OBWriteInternational3ValidationContext, OBWriteInternational3, OBWriteInternational3DataInitiation> {

    public static class OBWriteInternational3ValidationContext extends PaymentRequestValidationContext<OBWriteInternational3, OBWriteInternational3DataInitiation> {
        public OBWriteInternational3ValidationContext(OBWriteInternational3 paymentRequest, OBWriteInternationalConsent5 consentRequest, String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                    consentStatus, () -> consentRequest.getData().getInitiation(), consentRequest::getRisk);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteInternational3ValidationContext paymentReqValidationCtxt,
                                               ValidationResult<OBError1> validationResult) {

        // Add any payment specific validation as required
    }
}
