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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomestic2Validator.OBWriteDomestic2ValidationContext;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiation;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsent4;

/**
 * Validator of OBWriteDomestic2 objects (Domestic Payment Requests)
 */
public class OBWriteDomestic2Validator extends BasePaymentRequestValidator<OBWriteDomestic2ValidationContext,
        OBWriteDomestic2, OBWriteDomestic2DataInitiation> {

    public static class OBWriteDomestic2ValidationContext extends PaymentRequestValidationContext<OBWriteDomestic2,
            OBWriteDomestic2DataInitiation> {
        public OBWriteDomestic2ValidationContext(OBWriteDomestic2 paymentRequest,
                                                 OBWriteDomesticConsent4 consentRequest,
                                                 String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                  consentStatus, () -> consentRequest.getData().getInitiation(), consentRequest::getRisk);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteDomestic2ValidationContext paymentReqValidationCtxt,
                                               ValidationResult<OBError1> validationResult) {

        // No additional rules for OBWriteDomestic2 currently, add validation rules as required.
    }
}
