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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.funds;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.funds.FundsConfirmationValidator.FundsConfirmationValidationContext;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsentResponse1Data;
import uk.org.openbanking.datamodel.v4.fund.OBFundsConfirmation1;

import java.util.Objects;

@Slf4j
public class FundsConfirmationValidator extends BaseOBValidator<FundsConfirmationValidationContext> {

    public static class FundsConfirmationValidationContext {
        /**
         * The funds confirmation availability request
         */
        private final OBFundsConfirmation1 fundsConfirmationRequest;
        /**
         * The consent expiration date time
         */
        private final DateTime expirationDateTime;
        /**
         * The Consent Status
         */
        private final String consentStatus;
        /**
         * The currency of the account associated with the consent
         */
        private final String accountCurrency;

        public FundsConfirmationValidationContext(
                OBFundsConfirmation1 fundsConfirmationRequest,
                DateTime expirationDateTime,
                String consentStatus,
                String accountCurrency
        ) {
            this.fundsConfirmationRequest = Objects.requireNonNull(fundsConfirmationRequest, "fundsConfirmationRequest must be supplied");
            this.accountCurrency = Objects.requireNonNull(accountCurrency, "accountCurrency must be supplied");
            this.consentStatus = Objects.requireNonNull(consentStatus, "consentStatus must be supplied");
            this.expirationDateTime = Objects.requireNonNull(expirationDateTime, "expirationDateTime must be supplied");
        }

        public OBFundsConfirmation1 getFundsConfirmationRequest() {
            return fundsConfirmationRequest;
        }

        public String getAccountCurrency() {
            return accountCurrency;
        }

        public DateTime getExpirationDateTime() {
            return expirationDateTime;
        }

        public String getConsentStatus() {
            return consentStatus;
        }
    }

    @Override
    protected void validate(
            FundsConfirmationValidationContext fundsConfirmationValidationContext,
            ValidationResult<OBError1> validationResult
    ) {
        final String accountCurrency = fundsConfirmationValidationContext.getAccountCurrency();
        final String requestCurrency = fundsConfirmationValidationContext.getFundsConfirmationRequest().getData().getInstructedAmount().getCurrency();
        if(!accountCurrency.equals(requestCurrency)) {
            validationResult.addError(OBRIErrorType.FUNDS_CONFIRMATION_CURRENCY_MISMATCH.toOBError1(requestCurrency, accountCurrency));
        }
        validateExpirationDateTime(fundsConfirmationValidationContext.getExpirationDateTime(),  validationResult);
        validateConsentStatus(fundsConfirmationValidationContext.getConsentStatus(), validationResult);
    }

    private void validateExpirationDateTime(DateTime expirationDateTime, ValidationResult<OBError1> validationResult) {
        if (expirationDateTime != null) {
            if (expirationDateTime.isBeforeNow()) {
                validationResult.addError(OBRIErrorType.FUNDS_CONFIRMATION_EXPIRED.toOBError1(expirationDateTime.toString()));
            }
        }
    }

    private void validateConsentStatus(String consentStatus, ValidationResult<OBError1> validationResult){
        if(!Objects.equals(consentStatus, OBFundsConfirmationConsentResponse1Data.StatusEnum.AUTHORISED.toString())) {
            validationResult.addError(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(consentStatus));
        }
    }
}
