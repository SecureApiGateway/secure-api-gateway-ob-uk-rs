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

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

public class CurrencyCodeValidatorTest {

    public static CurrencyCodeValidator createCurrencyCodeValidator(String... currencies) {
        return new CurrencyCodeValidator(Set.of(currencies));
    }

    @Test
    public void testValidation() {
        final String[] validCurrencies = new String[] {
                "GBP", "EUR", "USD", "CHF", "AUD"
        };

        final CurrencyCodeValidator validator = createCurrencyCodeValidator(validCurrencies);
        for (String currency : validCurrencies) {
            validateSuccessResult(validator.validate(currency));
        }

        final String[] invalidCurrencies = new String[] {
                "what is this?", "zzz", "ZZZ", "023232323"
        };
        for (String currency : invalidCurrencies) {
            validateErrorResult(validator.validate(currency), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    "The currency " + currency + " provided is not supported")));
        }
    }

}