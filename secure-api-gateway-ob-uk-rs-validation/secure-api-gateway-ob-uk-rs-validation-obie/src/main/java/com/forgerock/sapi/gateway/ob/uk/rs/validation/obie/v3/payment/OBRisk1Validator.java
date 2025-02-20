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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;

import uk.org.openbanking.datamodel.v3.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.error.OBError1;

/**
 * Validator of {@link OBRisk1} objects, these objects are sent as part of Payment Consents and Requests.
 */
public class OBRisk1Validator extends BaseOBValidator<OBRisk1> {

    private final boolean requirePaymentContextCode;

    /**
     * @param requirePaymentContextCode boolean flag - whether validation should require that risk's paymentContextCode field is set
     */
    public OBRisk1Validator(boolean requirePaymentContextCode) {
        this.requirePaymentContextCode = requirePaymentContextCode;
    }

    @Override
    protected void validate(OBRisk1 risk, ValidationResult<OBError1> validationResult) {
        if (requirePaymentContextCode) {
            if (risk.getPaymentContextCode() == null) {
                validationResult.addError(OBRIErrorType.PAYMENT_CODE_CONTEXT_INVALID.toOBError1());
            }
        }
    }
}
