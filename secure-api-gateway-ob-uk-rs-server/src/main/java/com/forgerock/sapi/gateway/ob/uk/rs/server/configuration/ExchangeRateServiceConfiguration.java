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
package com.forgerock.sapi.gateway.ob.uk.rs.server.configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.forgerock.sapi.gateway.ob.uk.rs.server.service.currency.DefaultExchangeRateService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.currency.ExchangeRateService;

/**
 * Configuration for the ExchangeRateService, loads exchange rate test data from config.
 *
 * Example config:
 *   exchange:
 *     rates:
 *       default: "1.5123"
 *       pairs:
 *         GBPUSD: "1.3211"
 *         GBPEUR: "1.1634"
 *
 * The default rate is used when no rate is defined for a particular currency pair.
 */
@Configuration
public class ExchangeRateServiceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "rs.exchange.rates.pairs")
    public Map<String, BigDecimal> exchangeRateConfig() {
        return new HashMap<>();
    }

    @Bean
    public ExchangeRateService exchangeRateService(Map<String, BigDecimal> exchangeRateConfig,
                                                   @Value("${rs.exchange.rates.default:1.5}") BigDecimal defaultExchangeRate) {

        return new DefaultExchangeRateService(exchangeRateConfig, defaultExchangeRate);
    }
}
