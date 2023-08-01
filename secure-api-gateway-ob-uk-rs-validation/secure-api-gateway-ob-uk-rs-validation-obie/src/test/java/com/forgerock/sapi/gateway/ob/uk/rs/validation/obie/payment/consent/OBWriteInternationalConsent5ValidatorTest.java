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
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternational3DataInitiationExchangeRateInformationValidatorTest.createExchangeRateInfoValidator;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import uk.org.openbanking.datamodel.payment.OBExchangeRateType2Code;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory;

class OBWriteInternationalConsent5ValidatorTest {

    private final String[] currencies = new String[]{"GBP", "USD", "EUR"};
    private final OBWriteInternationalConsent5Validator validator = new OBWriteInternationalConsent5Validator(
            createInstructedAmountValidator(currencies), createCurrencyCodeValidator(currencies), createExchangeRateInfoValidator(currencies));

    @Test
    void validationRulesPass() {
        validateSuccessResult(validator.validate(OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5()));
        validateSuccessResult(validator.validate(OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5MandatoryFields()));
    }

    @Test
    void failsWhenCurrencyOfTransferInvalid() {
        final OBWriteInternationalConsent5 consent = OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5();
        consent.getData().getInitiation().setCurrencyOfTransfer("ZZZ");

        validateErrorResult(validator.validate(consent), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "The currency ZZZ provided is not supported")));
    }

    @Test
    void failsWhenInstructedAmountInvalid() {
        final OBWriteInternationalConsent5 consent = OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5();
        consent.getData().getInitiation().getInstructedAmount().amount("0.00");

        validateErrorResult(validator.validate(consent), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "The amount 0.00 provided must be greater than 0")));
    }

    @Test
    void failsWhenExchangeRateInformationInvalid() {
        final OBWriteInternationalConsent5 consent = OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5();
        consent.getData().getInitiation().getExchangeRateInformation().rateType(OBExchangeRateType2Code.INDICATIVE).exchangeRate(new BigDecimal("1.2"));

        validateErrorResult(validator.validate(consent), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an Indicative RateType.")));
    }

}