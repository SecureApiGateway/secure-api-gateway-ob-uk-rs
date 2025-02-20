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
package com.forgerock.sapi.gateway.ob.uk.rs.server.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery.DiscoveryApiService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.web.DisabledEndpointInterceptor;

/**
 * Listener which processes {@link ApplicationReadyEvent} events. This event is fired just before the application is
 * ready to start servicing requests.
 *
 * The listener is used to do late binding in order to resolve circular dependency issues in our Spring Beans
 */
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        final ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        configureDisableEndpointInterceptorServices(applicationContext);

        logger.info("Application ready to receive requests");
    }

    /**
     * See {@link DisabledEndpointInterceptor} documentation relating to the circular dependency for this component
     */
    private void configureDisableEndpointInterceptorServices(ConfigurableApplicationContext applicationContext) {
        applicationContext.getBean(DisabledEndpointInterceptor.class).setDiscoveryApiService(applicationContext.getBean(DiscoveryApiService.class));
    }
}
