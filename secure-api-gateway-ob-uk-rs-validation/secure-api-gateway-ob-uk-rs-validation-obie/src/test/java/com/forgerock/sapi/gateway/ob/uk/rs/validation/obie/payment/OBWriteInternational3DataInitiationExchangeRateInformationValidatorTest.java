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

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import uk.org.openbanking.datamodel.payment.OBExchangeRateType;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiationExchangeRateInformation;


public class OBWriteInternational3DataInitiationExchangeRateInformationValidatorTest {

    public static OBWriteInternational3DataInitiationExchangeRateInformationValidator createExchangeRateInfoValidator(String... currencies) {
        return new OBWriteInternational3DataInitiationExchangeRateInformationValidator(CurrencyCodeValidatorTest.createCurrencyCodeValidator(currencies));
    }

    private final OBWriteInternational3DataInitiationExchangeRateInformationValidator validator = createExchangeRateInfoValidator("USD", "GBP", "EUR");

    private static OBWriteInternational3DataInitiationExchangeRateInformation createValidateExchangeRateInfo() {
        return aValidOBWriteInternationalConsent5().getData().getInitiation().getExchangeRateInformation();
    }

    @Test
    void validationRulesPass() {
        validateSuccessResult(validator.validate(createValidateExchangeRateInfo()));
    }

    @Test
    void failsValidationWhenUnitCurrencyInvalid() {
        final OBWriteInternational3DataInitiationExchangeRateInformation exchangeRateInfo = createValidateExchangeRateInfo();
        exchangeRateInfo.setUnitCurrency("ZZZ");
        validateErrorResult(validator.validate(exchangeRateInfo), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "The currency ZZZ provided is not supported")));
    }

    @Test
    void failsWhenAgreedRateTypeFieldsAreMissing() {
        final OBWriteInternational3DataInitiationExchangeRateInformation invalidExchangeRateInfo = new OBWriteInternational3DataInitiationExchangeRateInformation()
                .unitCurrency("GBP")
                .rateType(OBExchangeRateType.AGREED);

        validateErrorResult(validator.validate(invalidExchangeRateInfo), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "ExchangeRate and ContractIdentification must be specify when requesting an Agreed RateType.")));

        invalidExchangeRateInfo.exchangeRate(new BigDecimal("1.23456"));
        validateErrorResult(validator.validate(invalidExchangeRateInfo), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "ExchangeRate and ContractIdentification must be specify when requesting an Agreed RateType.")));

        invalidExchangeRateInfo.exchangeRate(null).contractIdentification("some contract id");
        validateErrorResult(validator.validate(invalidExchangeRateInfo), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                "ExchangeRate and ContractIdentification must be specify when requesting an Agreed RateType.")));
    }

    @Test
    void failsWhenAgreedRateTypeFieldsArePresentForNonAgreedRate() {
        final OBExchangeRateType[] nonAgreedRateTypes = new OBExchangeRateType[]{
                OBExchangeRateType.ACTUAL, OBExchangeRateType.INDICATIVE
        };

        for (OBExchangeRateType nonAgreedRateType : nonAgreedRateTypes) {
            final OBWriteInternational3DataInitiationExchangeRateInformation invalidExchangeRateInfo = new OBWriteInternational3DataInitiationExchangeRateInformation()
                    .unitCurrency("GBP")
                    .contractIdentification("contract id")
                    .exchangeRate(new BigDecimal("1.2343"))
                    .rateType(nonAgreedRateType);

            validateErrorResult(validator.validate(invalidExchangeRateInfo), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    "A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an " + nonAgreedRateType + " RateType.")));

            invalidExchangeRateInfo.contractIdentification(null);
            validateErrorResult(validator.validate(invalidExchangeRateInfo), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    "A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an " + nonAgreedRateType + " RateType.")));

            invalidExchangeRateInfo.contractIdentification("some contract id").exchangeRate(null);
            validateErrorResult(validator.validate(invalidExchangeRateInfo), List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    "A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an " + nonAgreedRateType + " RateType.")));
        }
    }

}