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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.utils;

import com.forgerock.securebanking.openbanking.uk.rs.common.Currencies;

import static com.forgerock.securebanking.openbanking.uk.rs.common.Currencies.*;

public enum ExchangeRate {
    GBP_TO_EUR(GBP, EUR, 1.2),
    GBP_TO_USD(GBP, USD, 1.22),
    GBP_TO_RON(GBP, RON, 5.89),
    EUR_TO_USD(EUR, USD, 1.02),
    EUR_TO_RON(EUR, RON, 4.93),
    RON_TO_USD(RON, USD, 0.21);

    private final Currencies fromCurrency;
    private final Currencies toCurrency;
    private final double conversionRate;

    ExchangeRate(Currencies fromCurrency, Currencies toCurrency, double conversionRate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.conversionRate = conversionRate;
    }

    public Currencies getFromCurrency() {
        return fromCurrency;
    }

    public Currencies getToCurrency() {
        return toCurrency;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public static double getConversionRateForCurrency(Currencies fromCurrency, Currencies toCurrency) {
        for (ExchangeRate exchangeRate : ExchangeRate.values()) {
            if (exchangeRate.fromCurrency.equals(fromCurrency) && exchangeRate.toCurrency.equals(toCurrency)) {
                return exchangeRate.getConversionRate();
            }
            if (exchangeRate.fromCurrency.equals(toCurrency) && exchangeRate.toCurrency.equals(fromCurrency)) {
                return 1 / exchangeRate.getConversionRate();
            }
        }
        throw new UnsupportedOperationException("Unsupported exchange from " + fromCurrency + " to " + toCurrency);
    }
}
