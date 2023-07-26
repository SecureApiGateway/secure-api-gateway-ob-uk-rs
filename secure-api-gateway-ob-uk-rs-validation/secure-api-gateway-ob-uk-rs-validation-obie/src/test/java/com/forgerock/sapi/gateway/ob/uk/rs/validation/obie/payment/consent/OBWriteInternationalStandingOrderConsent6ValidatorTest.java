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

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.CurrencyCodeValidatorTest.createCurrencyCodeValidator;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomestic2DataInitiationInstructedAmountValidatorTest.createInstructedAmountValidator;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalStandingOrderConsentTestDataFactory;

class OBWriteInternationalStandingOrderConsent6ValidatorTest {

    private final String[] currencies = new String[]{"GBP", "USD", "EUR"};
    private final OBWriteInternationalStandingOrderConsent6Validator validator =
            new OBWriteInternationalStandingOrderConsent6Validator(createInstructedAmountValidator(currencies), createCurrencyCodeValidator(currencies));


    private static OBWriteInternationalStandingOrderConsent6 createValidConsent() {
        return OBWriteInternationalStandingOrderConsentTestDataFactory.aValidOBWriteInternationalStandingOrderConsent6MandatoryFields();
    }

    @Test
    void validConsent() {
        validateSuccessResult(validator.validate(createValidConsent()));
    }

    @Test
    void failsWhenInstructedAmountIsInvalid() {
        final OBWriteInternationalStandingOrderConsent6 consent = createValidConsent();
        consent.getData().getInitiation().getInstructedAmount().amount("0.00");

        validateErrorResult(validator.validate(consent), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "The amount 0.00 provided must be greater than 0")));
    }

    @Test
    void failsWhenCurrencyOfTransferIsInvalid() {
        final OBWriteInternationalStandingOrderConsent6 consent = createValidConsent();
        consent.getData().getInitiation().setCurrencyOfTransfer("ZZZ");

        validateErrorResult(validator.validate(consent), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "The currency ZZZ provided is not supported")));
    }

}