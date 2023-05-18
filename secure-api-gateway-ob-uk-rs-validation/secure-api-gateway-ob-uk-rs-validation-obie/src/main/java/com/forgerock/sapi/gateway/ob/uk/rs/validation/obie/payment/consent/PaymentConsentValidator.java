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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent;

import java.util.Objects;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidator;

import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;

public abstract class PaymentConsentValidator<T> extends BaseOBValidator<T> {

    private final OBValidator<OBRisk1> riskValidator;

    protected PaymentConsentValidator(OBValidator<OBRisk1> riskValidator) {
        Objects.requireNonNull(riskValidator, "riskValidator must be supplied");
        this.riskValidator = riskValidator;
    }

    protected void validateOBRisk1(OBRisk1 risk, ValidationResult<OBError1> validationResult) {
        validationResult.mergeResults(riskValidator.validate(risk));
    }
}