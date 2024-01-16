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

import java.util.function.Supplier;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteFile2Validator.OBWriteFile2ValidationContext;

import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteFile2;
import uk.org.openbanking.datamodel.payment.OBWriteFile2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;

/**
 * Validator of OBWriteFile2 objects (File Payment Requests)
 */
public class OBWriteFile2Validator extends BasePaymentRequestValidator<OBWriteFile2ValidationContext, OBWriteFile2, OBWriteFile2DataInitiation> {

    public static class OBWriteFile2ValidationContext extends PaymentRequestValidationContext<OBWriteFile2, OBWriteFile2DataInitiation> {
        // File Payments do not have a risk object
        private static final Supplier<OBRisk1> NULL_RISK_SUPPLIER = () -> null;

        public OBWriteFile2ValidationContext(OBWriteFile2 paymentRequest, OBWriteFileConsent3 consentRequest, String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), NULL_RISK_SUPPLIER,
                  consentStatus,  () -> consentRequest.getData().getInitiation(), NULL_RISK_SUPPLIER);
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBWriteFile2ValidationContext paymentReqValidationCtxt, ValidationResult<OBError1> validationResult) {
    }

}
