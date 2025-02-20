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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.currency;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRExchangeRateInformation.FRRateType;

import uk.org.openbanking.datamodel.v3.payment.OBExchangeRateType;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternational3DataInitiationExchangeRateInformation;

class DefaultExchangeRateServiceTest {

    private final BigDecimal defaultExchangeRate = new BigDecimal("1.99");
    private final DefaultExchangeRateService exchangeRateService = new DefaultExchangeRateService(Map.of("GBPUSD", new BigDecimal("1.32"),
                                                                                                         "GBPEUR", new BigDecimal("1.16")),
                                                                                                  defaultExchangeRate);

    @Test
    void shouldGetIndicativeQuoteIfNoExchangeRateInfoInRequest() {
        final FRExchangeRateInformation exchangeRateInformation = exchangeRateService.calculateExchangeRateInfo("EUR",
                new FRAmount("100.00", "GBP"), null);

        final BigDecimal expectedRate = new BigDecimal("1.16");
        validateIndicativeQuote(exchangeRateInformation, expectedRate, "GBP");
    }

    @Test
    void shouldGetIndicativeQuoteWhenRequested() {
        final FRExchangeRateInformation exchangeRateInformation = exchangeRateService.calculateExchangeRateInfo("NZD",
                new FRAmount("100.00", "GBP"),
                new OBWriteInternational3DataInitiationExchangeRateInformation().rateType(OBExchangeRateType.INDICATIVE).unitCurrency("GBP"));

        final BigDecimal expectedRate = new BigDecimal("1.99");
        validateIndicativeQuote(exchangeRateInformation, expectedRate, "GBP");
    }

    @Test
    void shouldGetActualQuote() {
        final FRExchangeRateInformation exchangeRateInformation = exchangeRateService.calculateExchangeRateInfo("GBP",
                new FRAmount("100.00", "EUR"), new OBWriteInternational3DataInitiationExchangeRateInformation().rateType(OBExchangeRateType.ACTUAL).unitCurrency("EUR"));

        final BigDecimal expectedRate = new BigDecimal("0.8621");
        validateActualQuote(exchangeRateInformation, expectedRate, "EUR");
    }

    @Test
    void shouldGetAgreedQuote() {
        final FRExchangeRateInformation exchangeRateInformation = exchangeRateService.calculateExchangeRateInfo("USD", new FRAmount("100.00", "GBP"),
                new OBWriteInternational3DataInitiationExchangeRateInformation()
                        .rateType(OBExchangeRateType.AGREED)
                        .exchangeRate(new BigDecimal("1.35"))
                        .unitCurrency("GBP")
                        .contractIdentification("contract12"));

        validateAgreedQuote(exchangeRateInformation, new BigDecimal("1.35"), "GBP", "contract12");
    }

    private static void validateIndicativeQuote(FRExchangeRateInformation exchangeRateInformation, BigDecimal expectedRate, String expectedUnitCcy) {
        assertThat(exchangeRateInformation.getRateType()).isEqualTo(FRRateType.INDICATIVE);
        assertThat(exchangeRateInformation.getExchangeRate()).isEqualByComparingTo(expectedRate);
        assertThat(exchangeRateInformation.getUnitCurrency()).isEqualTo(expectedUnitCcy);
        assertThat(exchangeRateInformation.getContractIdentification()).isNull();
        assertThat(exchangeRateInformation.getExpirationDateTime()).isNull();
    }

    private static void validateActualQuote(FRExchangeRateInformation exchangeRateInformation, BigDecimal expectedRate, String expectedUnitCcy) {
        assertThat(exchangeRateInformation.getRateType()).isEqualTo(FRRateType.ACTUAL);
        assertThat(exchangeRateInformation.getExchangeRate()).isEqualByComparingTo(expectedRate);
        assertThat(exchangeRateInformation.getUnitCurrency()).isEqualTo(expectedUnitCcy);
        assertThat(exchangeRateInformation.getContractIdentification()).isNull();
        assertThat(exchangeRateInformation.getExpirationDateTime()).isNotNull().isGreaterThan(DateTime.now());
    }

    private static void validateAgreedQuote(FRExchangeRateInformation exchangeRateInformation, BigDecimal expectedRate, String expectedUnitCcy, String expectedContractId) {
        assertThat(exchangeRateInformation.getRateType()).isEqualTo(FRRateType.AGREED);
        assertThat(exchangeRateInformation.getExchangeRate()).isEqualByComparingTo(expectedRate);
        assertThat(exchangeRateInformation.getUnitCurrency()).isEqualTo(expectedUnitCcy);
        assertThat(exchangeRateInformation.getContractIdentification()).isEqualTo(expectedContractId);
        assertThat(exchangeRateInformation.getExpirationDateTime()).isNull();
    }

}