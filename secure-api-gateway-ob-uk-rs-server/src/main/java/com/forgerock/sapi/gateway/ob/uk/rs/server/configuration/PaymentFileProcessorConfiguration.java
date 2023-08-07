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
package com.forgerock.sapi.gateway.ob.uk.rs.server.configuration;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.file.PaymentFileProcessorRegistry;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.processor.PaymentFileProcessor;

/**
 * Configure the {@link PaymentFileProcessorRegistry}.
 *
 * The configuration registers all PaymentFileProcessor into the registry.
 *
 * To add new processors to the registry, create a PaymentFileProcessor impl class and create a Spring Bean for this impl
 * (ether via Component Scanning or creating the bean in another Spring Configuration class).
 *
 * Alternatively, this configuration file can be disabled by creating a PaymentFileProcessorRegistry bean in a
 * configuration file that is run before this one.
 */
@Configuration
@ConditionalOnMissingBean(value = PaymentFileProcessorRegistry.class)
public class PaymentFileProcessorConfiguration {

    /**
     * Create the PaymentFileProcessRegistry wiring in all available PaymentFileProcessor beans
     *
     * @param paymentFileProcessors List<PaymentFileProcessor> all PaymentFileProcessor beans
     * @return PaymentFileProcessorRegistry with all PaymentFileProcessors registered
     */
    @Bean
    public PaymentFileProcessorRegistry paymentFileProcessorRegistry(List<PaymentFileProcessor> paymentFileProcessors) {
        return new PaymentFileProcessorRegistry(paymentFileProcessors);
    }

}