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
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomesticScheduled2Validator.OBWriteDomesticScheduled2ValidationContext;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduled2;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduled2DataInitiation;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduledConsent4;

/**
 * Validator of OBWriteDomesticScheduled2 objects (Domestic Scheduled Payment Requests)
 */
public class OBWriteDomesticScheduled2Validator extends BasePaymentRequestValidator<OBWriteDomesticScheduled2ValidationContext, OBWriteDomesticScheduled2, OBWriteDomesticScheduled2DataInitiation> {

    public static class OBWriteDomesticScheduled2ValidationContext extends PaymentRequestValidationContext<OBWriteDomesticScheduled2, OBWriteDomesticScheduled2DataInitiation> {
        public OBWriteDomesticScheduled2ValidationContext(OBWriteDomesticScheduled2 paymentRequest, OBWriteDomesticScheduledConsent4 consentRequest, String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                    consentStatus, () -> consentRequest.getData().getInitiation(), consentRequest::getRisk);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteDomesticScheduled2ValidationContext paymentReqValidationCtxt,
                                               ValidationResult<OBError1> validationResult) {

        // Add validation rules as required.
    }
}
