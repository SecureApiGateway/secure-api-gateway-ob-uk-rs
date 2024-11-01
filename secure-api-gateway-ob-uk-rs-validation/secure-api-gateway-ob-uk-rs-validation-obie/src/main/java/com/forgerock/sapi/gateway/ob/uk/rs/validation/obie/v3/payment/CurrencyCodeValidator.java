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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment;

import java.util.Objects;
import java.util.Set;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;

import uk.org.openbanking.datamodel.v3.error.OBError1;

public class CurrencyCodeValidator extends BaseOBValidator<String> {

    private final Set<String> validCurrencyCodes;

    public CurrencyCodeValidator(Set<String> validCurrencyCodes) {
        this.validCurrencyCodes = Objects.requireNonNull(validCurrencyCodes, "validCurrencyCodes must be supplied");
    }

    @Override
    protected void validate(String currencyCode, ValidationResult<OBError1> validationResult) {
        if (!validCurrencyCodes.contains(currencyCode)) {
            validationResult.addError(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    String.format("The currency %s provided is not supported", currencyCode)));
        }
    }
}
