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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.common.Currencies;
import com.google.common.base.Strings;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBExchangeRateType2Code;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.payment.OBWriteInternational2DataInitiationExchangeRateInformation;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiationExchangeRateInformation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;

public abstract class PaymentConsentValidation {

    protected List<OBError1> errors = new ArrayList<>();

    /**
     * @param version {@link OBVersion} is the api version to identify the request object to be validated
     * @return the request consent class by version
     */
    public abstract Class getRequestClass(OBVersion version);

    /**
     * @param consent the consent request object
     * @param <T>     dealing generic type
     */
    public abstract <T> void validate(T consent);

    /**
     * Get the error events list to build the error response
     *
     * @return list of {@link OBError1}
     */
    public List<OBError1> getErrors() {
        return errors;
    }

    /**
     * Clear error event list
     * @return this
     */
    public PaymentConsentValidation clearErrors() {
        this.errors.clear();
        return this;
    }

    protected void validateInstructedAmount(OBWriteDomestic2DataInitiationInstructedAmount instructedAmount) {
        if (isNull(instructedAmount, "InstructedAmount")) {
            return;
        }
        validateAmount(instructedAmount.getAmount());
        validateCurrency(instructedAmount.getCurrency());
    }


    protected void validateExchangeRateInformation(OBWriteInternational2DataInitiationExchangeRateInformation exchangeRateInformation, String currencyOfTransfer) {
        if (Objects.isNull(exchangeRateInformation)) {
            return;
        }

        if (isNull(currencyOfTransfer, "CurrencyOfTransfer")) {
            return;
        }
        validateCurrency(currencyOfTransfer);

        if (isNull(exchangeRateInformation.getUnitCurrency(), "UnitCurrency")) {
            return;
        }
        validateCurrency(exchangeRateInformation.getUnitCurrency());

        if (isNull(exchangeRateInformation.getRateType(), "RateType")) {
            return;
        }
        OBExchangeRateType2Code rateType = exchangeRateInformation.getRateType();
        switch (rateType) {
            case AGREED -> {
                // validate only mandatory fields for agreed rate type
                if (Objects.isNull(exchangeRateInformation.getContractIdentification()) || Objects.isNull(exchangeRateInformation.getExchangeRate())) {
                    errors.add(
                            OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                                    "ExchangeRate and ContractIdentification must be specify when requesting an Agreed RateType."
                            )
                    );
                }
            }
            case ACTUAL, INDICATIVE -> {
                if (!(exchangeRateInformation.getExchangeRate() == null && exchangeRateInformation.getContractIdentification() == null)) {
                    errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                            String.format("A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an %s RateType.", rateType)
                    ));
                }
            }
            default -> errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    String.format("The rate type %s provided isn't valid", rateType)
            ));
        }
    }

    protected void validateExchangeRateInformation(OBWriteInternational3DataInitiationExchangeRateInformation exchangeRateInformation, String currencyOfTransfer) {
        if (Objects.isNull(exchangeRateInformation)) {
            return;
        }

        if (isNull(currencyOfTransfer, "CurrencyOfTransfer")) {
            return;
        }
        validateCurrency(currencyOfTransfer);

        if (isNull(exchangeRateInformation.getUnitCurrency(), "UnitCurrency")) {
            return;
        }
        validateCurrency(exchangeRateInformation.getUnitCurrency());

        if (isNull(exchangeRateInformation.getRateType(), "RateType")) {
            return;
        }
        OBExchangeRateType2Code rateType = exchangeRateInformation.getRateType();
        switch (rateType) {
            case AGREED -> {
                if (!exchangeRateInformation.getUnitCurrency().equals(currencyOfTransfer)) {
                    errors.add(
                            OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                                    "The currency of transfer should be the same with the exchange unit currency."
                            )
                    );
                }
            }
            case ACTUAL, INDICATIVE -> {
                if (!(exchangeRateInformation.getExchangeRate() == null && exchangeRateInformation.getContractIdentification() == null)) {
                    errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                            String.format("A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an %s RateType.", rateType)
                    ));
                }
            }
            default -> errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    String.format("The rate type %s provided isn't valid", rateType)
            ));
        }
    }

    protected void validateAmount(String amount) {
        if (isNull(amount, "Amount")) {
            return;
        }
        if (new BigDecimal(amount).compareTo(ZERO) <= 0) {
            errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    String.format("The amount %s provided must be greater than 0", amount)
            ));
        }
    }

    protected void validateCurrency(String currency) {
        if (isNull(currency, "Currency")) {
            return;
        }
        if (!Currencies.contains(currency)) {
            errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    String.format("The currency %s provided is not supported", currency)
            ));
        }
    }

    public boolean isNull(String value, String valueName) {
        if (Strings.isNullOrEmpty(value)) {
            errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    String.format("'%s' cannot be null to be validate", valueName)
            ));
            return true;
        }
        return false;
    }

    public boolean isNull(Object object, String objectName) {
        if (Objects.isNull(object)) {
            errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    String.format("'%s' cannot be null to be validate", objectName)
            ));
            return true;
        }
        return false;
    }

}
