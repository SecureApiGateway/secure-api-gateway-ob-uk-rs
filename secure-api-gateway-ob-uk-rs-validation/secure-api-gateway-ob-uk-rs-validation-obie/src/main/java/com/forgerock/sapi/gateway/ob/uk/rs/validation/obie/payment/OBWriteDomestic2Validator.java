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
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomestic2Validator.OBWriteDomesticValidatorContext;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;

/**
 * Validator of OBWriteDomestic2 objects (Domestic Payment Requests)
 */
public class OBWriteDomestic2Validator extends BasePaymentRequestValidator<OBWriteDomesticValidatorContext, OBWriteDomestic2, OBWriteDomestic2DataInitiation> {

    public static class OBWriteDomesticValidatorContext extends PaymentRequestValidationContext<OBWriteDomestic2, OBWriteDomestic2DataInitiation> {
        public OBWriteDomesticValidatorContext(OBWriteDomestic2 paymentRequest, OBWriteDomesticConsent4 consent) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                    () -> consent.getData().getInitiation(), consent::getRisk);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteDomesticValidatorContext paymentReqValidationCtxt, ValidationResult<OBError1> validationResult) {
        final OBWriteDomestic2 paymentRequest = paymentReqValidationCtxt.getPaymentRequest();
        // TODO impl any OBWriteDomestic2 specific validation
    }

}
