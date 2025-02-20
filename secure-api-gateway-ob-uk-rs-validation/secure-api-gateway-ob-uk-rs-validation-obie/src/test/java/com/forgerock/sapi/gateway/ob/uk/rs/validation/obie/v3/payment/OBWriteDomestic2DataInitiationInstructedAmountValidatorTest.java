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

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.util.List;

import org.junit.jupiter.api.Test;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationInstructedAmount;

public class OBWriteDomestic2DataInitiationInstructedAmountValidatorTest {

    public static OBWriteDomestic2DataInitiationInstructedAmountValidator createInstructedAmountValidator(String... currencies) {
        return new OBWriteDomestic2DataInitiationInstructedAmountValidator(CurrencyCodeValidatorTest.createCurrencyCodeValidator(currencies));
    }


    private final OBWriteDomestic2DataInitiationInstructedAmountValidator validator = createInstructedAmountValidator("USD", "GBP", "EUR", "CHF");

    @Test
    public void validationRulesPass() {
        validateSuccessResult(validator.validate(
                new OBWriteDomestic2DataInitiationInstructedAmount().amount("0.01").currency("GBP")));
    }

    @Test
    public void failsValidationWhenInstructedAmountCurrencyInvalid() {
        final String invalidCcy = "ZZZ";
        final OBWriteDomestic2DataInitiationInstructedAmount amount = new OBWriteDomestic2DataInitiationInstructedAmount();
        amount.currency(invalidCcy).amount("12.22");

        validateErrorResult(validator.validate(amount), List.of(new OBError1().errorCode("OBRI.Data.Request.Invalid")
                .message("Your data request is invalid: reason The currency " + invalidCcy + " provided is not supported")));
    }

    @Test
    public void failsValidationWhenInstructedAmountLessThanOrEqualToZero() {
        final String[] invalidAmounts = new String[] {
                "-1000.12", "-0.01", "0", "0.0"
        };

        for (String invalidAmount : invalidAmounts) {
            final OBWriteDomestic2DataInitiationInstructedAmount amount = new OBWriteDomestic2DataInitiationInstructedAmount();
            amount.currency("GBP").amount(invalidAmount);
            validateErrorResult(validator.validate(amount), List.of(new OBError1().errorCode("OBRI.Data.Request.Invalid")
                    .message("Your data request is invalid: reason The amount " + invalidAmount + " provided must be greater than 0")));
        }
    }
}