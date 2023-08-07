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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.funds;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.funds.FundsConfirmationValidator.FundsConfirmationValidationContext;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.account.OBReadData1;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmation1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentData1;

@Slf4j
public class FundsConfirmationValidator extends BaseOBValidator<FundsConfirmationValidationContext> {

    public static class FundsConfirmationValidationContext {
        private final OBFundsConfirmation1 obFundsConfirmation1;
        private final OBFundsConfirmationConsent1 obFundsConfirmationConsent1;
        private final String accountCurrency;

        public FundsConfirmationValidationContext(
                OBFundsConfirmation1 obFundsConfirmation1,
                OBFundsConfirmationConsent1 obFundsConfirmationConsent1,
                String accountCurrency
        ) {
            this.obFundsConfirmation1 = obFundsConfirmation1;
            this.accountCurrency = accountCurrency;
            this.obFundsConfirmationConsent1 = obFundsConfirmationConsent1;
        }

        public OBFundsConfirmation1 getObFundsConfirmation1() {
            return obFundsConfirmation1;
        }

        public String getAccountCurrency() {
            return accountCurrency;
        }

        public OBFundsConfirmationConsent1 getObFundsConfirmationConsent1() {
            return obFundsConfirmationConsent1;
        }
    }

    @Override
    protected void validate(
            FundsConfirmationValidationContext fundsConfirmationValidationContext,
            ValidationResult<OBError1> validationResult
    ) {
        final String accountCurrency = fundsConfirmationValidationContext.getAccountCurrency();
        final String requestCurrency = fundsConfirmationValidationContext.getObFundsConfirmation1().getData().getInstructedAmount().getCurrency();
        if(!accountCurrency.equals(requestCurrency)) {
            validationResult.addError(OBRIErrorType.FUNDS_CONFIRMATION_CURRENCY_MISMATCH.toOBError1(requestCurrency, accountCurrency));
        }
        validateExpirationDateTime(fundsConfirmationValidationContext.getObFundsConfirmationConsent1().getData(),  validationResult);
    }

    private void validateExpirationDateTime(OBFundsConfirmationConsentData1 consentData, ValidationResult<OBError1> validationResult) {
        final DateTime expirationDateTime = consentData.getExpirationDateTime();
        if (expirationDateTime != null) {
            if (expirationDateTime.isBeforeNow()) {
                validationResult.addError(OBRIErrorType.FUNDS_CONFIRMATION_EXPIRED.toOBError1(expirationDateTime.toString()));
            }
        }
    }
}
