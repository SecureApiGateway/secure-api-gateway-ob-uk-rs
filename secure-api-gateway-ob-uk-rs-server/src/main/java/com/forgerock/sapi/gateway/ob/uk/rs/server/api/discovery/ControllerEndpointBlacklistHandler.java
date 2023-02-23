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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores controller methods that have been disabled in the configuration (i.e. endpoints that are not supported by
 * the Discovery endpoint). This blacklist can be used to accurately reject requests to controller methods, without
 * needing to use regex matches (which may match the same URL, but the wrong HTTP method).
 */
@Slf4j
@Component
public class ControllerEndpointBlacklistHandler {
    /**
     * Set of all controller methods that are not provided by the Discovery endpoint.
     */
    private final Set<ControllerMethod> blackList = new HashSet<>();

    /**
     * Adds the provided API endpoint to the list of ones to block.
     *
     * @param controllerMethod Represents the endpoint's corresponding Spring controller method.
     */
    public void blacklistEndpoint(ControllerMethod controllerMethod) {
        if (controllerMethod != null) {
            blackList.add(controllerMethod);
            log.info("Disabled Controller method: {}", controllerMethod);
        }
    }

    /**
     * Determines if the provided API endpoint is blacklisted.
     *
     * @param clazz The API endpoint's corresponding Spring controller class.
     * @param method The API endpoint's implementing method in the corresponding Spring controller class.
     * @return <code>true</code> if the endpoint is blacklisted.
     */
    public boolean isBlacklisted(Class clazz, Method method) {
        if (clazz == null || method == null) {
            return false;
        }
        ControllerMethod controllerMethod = ControllerMethod.of(clazz, method);
        return blackList.contains(controllerMethod);
    }
}
