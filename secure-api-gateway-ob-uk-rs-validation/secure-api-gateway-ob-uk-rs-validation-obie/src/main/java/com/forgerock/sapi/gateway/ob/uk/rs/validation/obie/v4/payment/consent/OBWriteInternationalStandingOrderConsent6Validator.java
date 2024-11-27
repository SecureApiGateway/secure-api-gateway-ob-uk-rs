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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent;

import java.util.Objects;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.common.OBRisk1;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalStandingOrder4DataInitiation;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalStandingOrderConsent6;

/**
 * Validator of OBWriteInternationalStandingOrderConsent6V objects (OBIE International Standing Order Consents)
 */
public class OBWriteInternationalStandingOrderConsent6Validator extends BaseOBValidator<OBWriteInternationalStandingOrderConsent6> {

    private final BaseOBValidator<OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidator;
    private final BaseOBValidator<String> currencyCodeValidator;
    private final BaseOBValidator<OBRisk1> riskValidator;

    public OBWriteInternationalStandingOrderConsent6Validator(BaseOBValidator<OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidator,
            BaseOBValidator<String> currencyCodeValidator, BaseOBValidator<OBRisk1> riskValidator) {
        this.instructedAmountValidator = Objects.requireNonNull(instructedAmountValidator, "instructedAmountValidator must be supplied");
        this.currencyCodeValidator = Objects.requireNonNull(currencyCodeValidator, "currencyCodeValidator must be supplied");
        this.riskValidator = Objects.requireNonNull(riskValidator, "riskValidator must be supplied");
    }

    @Override
    protected void validate(OBWriteInternationalStandingOrderConsent6 consent, ValidationResult<OBError1> validationResult) {
        validationResult.mergeResults(riskValidator.validate(consent.getRisk()));

        final OBWriteInternationalStandingOrder4DataInitiation initiation = consent.getData().getInitiation();
        validationResult.mergeResults(currencyCodeValidator.validate(initiation.getCurrencyOfTransfer()));
        validationResult.mergeResults(instructedAmountValidator.validate(initiation.getInstructedAmount()));
    }
}
