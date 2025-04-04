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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// TODO #25 - this approach is a starter for ten to get us up and running. We need to configure the logging as per the
// requirements in https://github.com/SecureBankingAcceleratorToolkit/SecureBankingAcceleratorToolkit/issues/25
/**
 * Outputs the occurrence of an event/activity to the configured default logger. Each output has a recognised prefix
 * so that is it easy to identify the analytics events in the logs.
 */
@Component
@Slf4j
public class AnalyticsLogService implements AnalyticsService {

    // TODO #25 - we may not need this prefix
    private static final String PREFIX = "ANALYTICS: ";

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordActivity(String text, Object... arguments) {
        log.info(PREFIX + text, arguments);
    }
}
