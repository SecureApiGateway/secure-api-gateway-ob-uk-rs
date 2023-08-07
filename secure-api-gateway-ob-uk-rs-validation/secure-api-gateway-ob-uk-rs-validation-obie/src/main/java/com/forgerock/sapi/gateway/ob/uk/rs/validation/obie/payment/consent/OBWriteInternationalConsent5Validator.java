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

import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiationExchangeRateInformation;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;

/**
 * Validator of OBWriteInternationalConsent5 objects (OBIE International Payment Consents)
 */
public class OBWriteInternationalConsent5Validator extends BaseOBValidator<OBWriteInternationalConsent5> {

    private final BaseOBValidator<OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidator;
    private final BaseOBValidator<String> currencyCodeValidator;
    private final BaseOBValidator<OBWriteInternational3DataInitiationExchangeRateInformation> exchangeRateInfoValidator;
    private final BaseOBValidator<OBRisk1> riskValidator;

    public OBWriteInternationalConsent5Validator(BaseOBValidator<OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidator,
                                                 BaseOBValidator<String> currencyCodeValidator,
                                                 BaseOBValidator<OBWriteInternational3DataInitiationExchangeRateInformation> exchangeRateInfoValidator,
                                                 BaseOBValidator<OBRisk1> riskValidator) {
        this.instructedAmountValidator = Objects.requireNonNull(instructedAmountValidator);
        this.currencyCodeValidator     = Objects.requireNonNull(currencyCodeValidator);
        this.exchangeRateInfoValidator = Objects.requireNonNull(exchangeRateInfoValidator);
        this.riskValidator             = Objects.requireNonNull(riskValidator);
    }

    @Override
    protected void validate(OBWriteInternationalConsent5 consent, ValidationResult<OBError1> validationResult) {
        validationResult.mergeResults(riskValidator.validate(consent.getRisk()));

        final OBWriteInternational3DataInitiation initiation = consent.getData().getInitiation();
        validationResult.mergeResults(instructedAmountValidator.validate(initiation.getInstructedAmount()));
        validationResult.mergeResults(currencyCodeValidator.validate(initiation.getCurrencyOfTransfer()));

        if (initiation.getExchangeRateInformation() != null) {
            validationResult.mergeResults(exchangeRateInfoValidator.validate(initiation.getExchangeRateInformation()));
        }
    }
}
