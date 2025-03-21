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
package com.forgerock.sapi.gateway.rs.resource.store.datamodel.shared.currency;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;
import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.v3.payment.OBExchangeRateType;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
public final class CurrencyRateService {

    // Currency rate is hardcoded for sandbox for simplicity - if required in future we can implement different exchange rates for currencies / datetimes
    private static final String FIXED_RATE = "1.5";

    // Actual Currency rate expiration time - hardcoded period should be good enough for sandbox testing.
    private static final int ACTUAL_RATE_EXPIRATION_MINUTES = 60;

    /**
     * This method gets the exchange rate requested in the initiation with the correct calculated fields.
     * <ul>
     * <li/>For Actual Rate: the rate value and expiration date are populated with test values.
     * <li/>For Indicative Rate: the rate value only is populated with test value.
     * <li/>For Agreed Rate: the exchange rate is returned with no modification
     * </ul>
     * <p>
     * Provided <code>submittedExchangeRate</code> should already be validated using com.forgerock.openbanking.aspsp.rs.api.payment.verifier.ExchangeRateVerifier
     *
     * @param submittedExchangeRate The {@link FRExchangeRateInformation} exchange rate submitted by the user
     * @param createdDateTime       Date request was submitted - used to calculate how long an agreed rate will be valid for.
     * @return Exchange rate with populated fields
     */
    public static FRExchangeRateInformation getCalculatedExchangeRate(FRExchangeRateInformation submittedExchangeRate,
                                                                      Date createdDateTime) {
        if (submittedExchangeRate == null) {
            return null; // Exchange rate is optional in international payment requests
        }
        if (submittedExchangeRate.getRateType() == null) {
            throw new IllegalArgumentException("Missing the exchange rate type");
        }
        return FRExchangeRateInformation.builder()
                .expirationDateTime(getExpirationDate(submittedExchangeRate, createdDateTime))
                .rateType(submittedExchangeRate.getRateType())
                .contractIdentification(submittedExchangeRate.getContractIdentification())
                .exchangeRate(getExchangeRate(submittedExchangeRate))
                .unitCurrency(submittedExchangeRate.getUnitCurrency())
                .build();
    }

    private static DateTime getExpirationDate(FRExchangeRateInformation submittedRate, Date createdDateTime) {
        if (submittedRate == null) {
            return null;
        }
        switch (submittedRate.getRateType()) {
            case ACTUAL:
                return ((createdDateTime == null) ? DateTime.now() : new DateTime(createdDateTime.getTime())).plusMinutes(ACTUAL_RATE_EXPIRATION_MINUTES);
            case INDICATIVE:
            case AGREED:
                return null;
            default:
                throw new UnsupportedOperationException("Unsupported currency rate type: " + submittedRate.getRateType());
        }
    }

    private static BigDecimal getExchangeRate(FRExchangeRateInformation submittedRate) {
        switch (submittedRate.getRateType()) {
            case INDICATIVE:
            case ACTUAL:
                return new BigDecimal(FIXED_RATE);
            case AGREED:
                if (submittedRate.getExchangeRate() == null) {
                    throw new IllegalArgumentException("Missing the exchange rate value which is mandatory for exchange rate type: '" + OBExchangeRateType.AGREED + "'");
                }
                return submittedRate.getExchangeRate();
            default:
                throw new UnsupportedOperationException("Unsupported currency rate type: " + submittedRate.getRateType());
        }
    }
}
