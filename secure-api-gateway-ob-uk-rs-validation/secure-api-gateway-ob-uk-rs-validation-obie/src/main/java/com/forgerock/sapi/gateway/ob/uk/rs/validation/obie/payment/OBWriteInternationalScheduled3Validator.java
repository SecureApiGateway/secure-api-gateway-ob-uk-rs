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
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternationalScheduled3Validator.OBWriteInternationalScheduled3ValidationContext;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduled3;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduled3DataInitiation;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduledConsent5;

/**
 * Validator of OBWriteInternationalScheduled3 objects (International Scheduled Payment Requests)
 */
public class OBWriteInternationalScheduled3Validator extends BasePaymentRequestValidator<OBWriteInternationalScheduled3ValidationContext, OBWriteInternationalScheduled3, OBWriteInternationalScheduled3DataInitiation> {

    public static class OBWriteInternationalScheduled3ValidationContext extends PaymentRequestValidationContext<OBWriteInternationalScheduled3, OBWriteInternationalScheduled3DataInitiation> {
        public OBWriteInternationalScheduled3ValidationContext(OBWriteInternationalScheduled3 paymentRequest, OBWriteInternationalScheduledConsent5 consentRequest, String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                  consentStatus, () -> consentRequest.getData().getInitiation(), consentRequest::getRisk);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteInternationalScheduled3ValidationContext paymentReqValidationCtxt,
                                               ValidationResult<OBError1> validationResult) {

        // Add any payment specific validation as required
    }
}
