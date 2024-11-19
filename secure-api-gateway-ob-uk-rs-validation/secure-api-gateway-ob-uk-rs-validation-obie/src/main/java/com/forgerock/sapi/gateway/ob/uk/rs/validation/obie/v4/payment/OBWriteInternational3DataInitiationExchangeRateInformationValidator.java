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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment;

import java.util.Objects;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.payment.OBExchangeRateType;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternational3DataInitiationExchangeRateInformation;

public class OBWriteInternational3DataInitiationExchangeRateInformationValidator extends BaseOBValidator<OBWriteInternational3DataInitiationExchangeRateInformation> {

    private final BaseOBValidator<String> currencyCodeValidator;

    public OBWriteInternational3DataInitiationExchangeRateInformationValidator(BaseOBValidator<String> currencyCodeValidator) {
        this.currencyCodeValidator = Objects.requireNonNull(currencyCodeValidator, "currencyCodeValidator must be supplied");
    }

    @Override
    protected void validate(OBWriteInternational3DataInitiationExchangeRateInformation exchangeRateInfo,
                            ValidationResult<OBError1> validationResult) {

        validationResult.mergeResults(currencyCodeValidator.validate(exchangeRateInfo.getUnitCurrency()));

        final OBExchangeRateType rateType = exchangeRateInfo.getRateType();
        if (rateType == OBExchangeRateType.AGREED) {
            if (exchangeRateInfo.getContractIdentification() == null || exchangeRateInfo.getExchangeRate() == null) {
                validationResult.addError(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        "ExchangeRate and ContractIdentification must be specify when requesting an Agreed RateType."));
            }
        } else {
            if (!(exchangeRateInfo.getExchangeRate() == null && exchangeRateInfo.getContractIdentification() == null)) {
                validationResult.addError(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an %s RateType.", rateType)));
            }
        }

    }

}
