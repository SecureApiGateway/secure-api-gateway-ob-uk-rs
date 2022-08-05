/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.utils;

import uk.org.openbanking.datamodel.payment.OBExchangeRateType2Code;
import uk.org.openbanking.datamodel.payment.OBWriteInternational2DataInitiationExchangeRateInformation;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3DataInitiationExchangeRateInformation;

import java.math.BigDecimal;

import static com.forgerock.securebanking.openbanking.uk.rs.common.Currencies.fromCode;

public class DefaultData {
    private static final String CONTRACT_IDENTIFICATION = "/tbill/2018/T102993";

    public static OBWriteInternational2DataInitiationExchangeRateInformation defaultOBWriteInternational2DataInitiationExchangeRateInformation(String instructedAmountCurrency, String currencyOfTransfer)
    {
        return new OBWriteInternational2DataInitiationExchangeRateInformation()
                .rateType(OBExchangeRateType2Code.AGREED)
                .exchangeRate(new BigDecimal(ExchangeRate.getConversionRateForCurrency(fromCode(instructedAmountCurrency), fromCode(currencyOfTransfer))))
                .contractIdentification(CONTRACT_IDENTIFICATION)
                .unitCurrency(currencyOfTransfer);
    }

    public static OBWriteInternational3DataInitiationExchangeRateInformation defaultOBWriteInternational3DataInitiationExchangeRateInformation(String instructedAmountCurrency, String currencyOfTransfer)
    {
        return new OBWriteInternational3DataInitiationExchangeRateInformation()
                .rateType(OBExchangeRateType2Code.AGREED)
                .exchangeRate(new BigDecimal(ExchangeRate.getConversionRateForCurrency(fromCode(instructedAmountCurrency), fromCode(currencyOfTransfer))))
                .contractIdentification(CONTRACT_IDENTIFICATION)
                .unitCurrency(currencyOfTransfer);
    }
}
