/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.web;

import com.forgerock.securebanking.openbanking.uk.rs.discovery.ControllerEndpointBlacklistHandler;
import com.forgerock.securebanking.openbanking.uk.rs.discovery.DiscoveryApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Returns a 404 error response if a request is for an API endpoint that has been disabled in the configuration.
 */
@Component
@Slf4j
public class DisabledEndpointInterceptor implements HandlerInterceptor {

    private final DiscoveryApiService discoveryApiService;

    // @Lazy due to spring wiring circular dependency issue
    public DisabledEndpointInterceptor(@Lazy DiscoveryApiService discoveryApiService) {
        this.discoveryApiService = discoveryApiService;
    }

    /**
     * Finds the controller method that this request will be mapped to (if any) and blocks it with a 404 error response
     * if the method has been disabled in application's configuration.
     *
     * @param request the current {@link HttpServletRequest}.
     * @param response the current {@link HttpServletResponse}.
     * @param handler the chosen handler to execute.
     * @return <code>true</code> if the execution chain should continue or stop with this handler.
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            ControllerEndpointBlacklistHandler blacklistHandler = discoveryApiService.getControllerBlackListHandler();
            if (blacklistHandler.isBlacklisted(handlerMethod.getBeanType(), handlerMethod.getMethod())) {
                log.warn("Request URI {} was BLOCKED due to RS configuration settings. " +
                        "Handler method: {}", request.getRequestURI(), handlerMethod.getMethod());
                try {
                    response.sendError(HttpStatus.NOT_FOUND.value());
                } catch (IOException e) {
                    log.error("Failed to write error response: {}", "", e);
                }
                return false;
            }
        }

        log.debug("Request URI {} is not disabled in RS config. Allowed to proceed.", request.getRequestURI());
        return true;
    }
}
