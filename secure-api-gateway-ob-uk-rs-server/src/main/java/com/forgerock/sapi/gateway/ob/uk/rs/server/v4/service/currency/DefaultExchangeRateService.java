/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.v4.service.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation.FRRateType;

import uk.org.openbanking.datamodel.v4.payment.OBWriteInternational3DataInitiationExchangeRateInformation;

/**
 * Default implementation of the ExchangeRateService.
 *
 * Uses a Map of currency pairs to exchange rate values, falling back on to a default value if no rate can be found
 * for a particular pair.
 */
public class DefaultExchangeRateService implements ExchangeRateService {

    /**
     * Map of currencyPair to exchangeRate value
     *
     * currencyPair expected to be of the form: GBPUSD
     */
    private final Map<String, BigDecimal> exchangeRates;

    /**
     * Default rate to use when we do not have a rate defined in the exchangeRates map
     */
    private final BigDecimal defaultExchangeRate;

    public DefaultExchangeRateService(Map<String, BigDecimal> exchangeRates, BigDecimal defaultExchangeRate) {
        this.exchangeRates = Objects.requireNonNull(exchangeRates);
        this.defaultExchangeRate = Objects.requireNonNull(defaultExchangeRate);
    }

    @Override
    public FRExchangeRateInformation calculateExchangeRateInfo(String currencyOfTransfer, FRAmount instructedAmount,
            OBWriteInternational3DataInitiationExchangeRateInformation consentRequestExchangeRateInfo) {

        if (consentRequestExchangeRateInfo == null) {
            return generateIndicativeQuote(currencyOfTransfer, instructedAmount.getCurrency());
        } else {
            switch (consentRequestExchangeRateInfo.getRateType()) {
                case ACTUAL -> {
                    return generateActualQuote(currencyOfTransfer, consentRequestExchangeRateInfo);
                }
                case AGREED -> {
                    return generateAgreedQuote(consentRequestExchangeRateInfo);
                }
                case INDICATIVE -> {
                    return generateIndicativeQuote(currencyOfTransfer, consentRequestExchangeRateInfo.getUnitCurrency());
                }
            }
            throw new IllegalStateException("RateType: " + consentRequestExchangeRateInfo.getRateType() + " not supported");
        }
    }

    private FRExchangeRateInformation generateActualQuote(String currencyOfTransfer, OBWriteInternational3DataInitiationExchangeRateInformation exchangeRateInformation) {
        return FRExchangeRateInformation.builder().rateType(FRRateType.ACTUAL)
                .exchangeRate(getExchangeRate(exchangeRateInformation.getUnitCurrency(), currencyOfTransfer))
                .unitCurrency(exchangeRateInformation.getUnitCurrency())
                .expirationDateTime(DateTime.now().plusMinutes(5))
                .build();
    }

    private FRExchangeRateInformation generateAgreedQuote(OBWriteInternational3DataInitiationExchangeRateInformation exchangeRateInformation) {
        return FRExchangeRateInformation.builder().rateType(FRRateType.AGREED)
                .unitCurrency(exchangeRateInformation.getUnitCurrency())
                .exchangeRate(exchangeRateInformation.getExchangeRate())
                .contractIdentification(exchangeRateInformation.getContractIdentification())
                .build();
    }

    private FRExchangeRateInformation generateIndicativeQuote(String currencyOfTransfer, String unitCurrency) {
        return FRExchangeRateInformation.builder().rateType(FRRateType.INDICATIVE)
                .exchangeRate(getExchangeRate(unitCurrency, currencyOfTransfer))
                .unitCurrency(unitCurrency)
                .build();
    }

    private BigDecimal getExchangeRate(String unitCurrency, String currencyOfTransfer) {
        if (unitCurrency.equals(currencyOfTransfer)) {
            return BigDecimal.ONE;
        }

        final String currencyPairKey = unitCurrency + currencyOfTransfer;
        if (exchangeRates.containsKey(currencyPairKey)) {
            return exchangeRates.get(currencyPairKey);
        } else {
            // See if we have the inverse rate configured and compute the inverse
            final String inversePairKey = currencyOfTransfer + unitCurrency;
            if (exchangeRates.containsKey(inversePairKey)) {
                final BigDecimal inverseRate = exchangeRates.get(inversePairKey);
                return BigDecimal.ONE.setScale(4).divide(inverseRate, RoundingMode.HALF_EVEN);
            }
            return defaultExchangeRate;
        }
    }
}
