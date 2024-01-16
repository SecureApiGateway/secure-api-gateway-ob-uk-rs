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
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternationalStandingOrder4Validator.OBWriteInternationalStandingOrder4ValidationContext;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder4;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder4DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent6;

/**
 * Validator of OBWriteInternationalStandingOrder4 objects (International Standing Order Requests)
 */
public class OBWriteInternationalStandingOrder4Validator extends BasePaymentRequestValidator<OBWriteInternationalStandingOrder4ValidationContext, OBWriteInternationalStandingOrder4, OBWriteInternationalStandingOrder4DataInitiation> {

    public static class OBWriteInternationalStandingOrder4ValidationContext extends PaymentRequestValidationContext<OBWriteInternationalStandingOrder4, OBWriteInternationalStandingOrder4DataInitiation> {
        public OBWriteInternationalStandingOrder4ValidationContext(OBWriteInternationalStandingOrder4 paymentRequest, OBWriteInternationalStandingOrderConsent6 consentRequest, String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                  consentStatus, () -> consentRequest.getData().getInitiation(), consentRequest::getRisk);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteInternationalStandingOrder4ValidationContext paymentReqValidationCtxt, ValidationResult<OBError1> validationResult) {

        // Add any payment specific validation as required
    }
}