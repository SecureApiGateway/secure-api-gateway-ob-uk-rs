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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomestic2Validator.OBWriteDomestic2ValidatorContext;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5;

/**
 * Validator of OBWriteDomestic2 objects (Domestic Payment Requests)
 */
public class OBWriteDomestic2Validator extends BasePaymentRequestValidator<OBWriteDomestic2ValidatorContext, OBWriteDomestic2, OBWriteDomestic2DataInitiation> {

    public static class OBWriteDomestic2ValidatorContext extends PaymentRequestValidationContext<OBWriteDomestic2, OBWriteDomestic2DataInitiation> {
        public OBWriteDomestic2ValidatorContext(OBWriteDomestic2 paymentRequest, OBWriteDomesticConsent4 consent) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                    () -> consent.getData().getInitiation(), consent::getRisk);
        }

        public OBWriteDomestic2ValidatorContext(OBWriteDomestic2 paymentRequest, OBWriteDomesticConsentResponse5 consent) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                    () -> consent.getData().getInitiation(), consent::getRisk);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteDomestic2ValidatorContext paymentReqValidationCtxt,
                                               ValidationResult<OBError1> validationResult) {

        // No additional rules for OBWriteDomestic2 currently, add validation rules as required.
    }

}
