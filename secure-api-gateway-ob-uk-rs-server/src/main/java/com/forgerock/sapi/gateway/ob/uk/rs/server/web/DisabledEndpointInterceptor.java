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
package com.forgerock.sapi.gateway.ob.uk.rs.server.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery.ControllerEndpointBlacklistHandler;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery.DiscoveryApiService;

import lombok.extern.slf4j.Slf4j;

/**
 * Returns a 404 error response if a request is for an API endpoint that has been disabled in the configuration.
 *
 * The blackListHandler is provided by the DiscoveryApiService, this bean cannot depend on this service directly as we
 * then get into a cyclic dependency. This is due to this interceptor needing to be created before the WebMvc can be
 * created and the Discovery data is determined by processing the HandlerMappings created by the WebMvc
 *
 * The {@link com.forgerock.sapi.gateway.ob.uk.rs.server.configuration.ApplicationStartupListener} is doing the late
 * binding of the beans.
 */
@Component
@Slf4j
public class DisabledEndpointInterceptor implements HandlerInterceptor {

    private ControllerEndpointBlacklistHandler blacklistHandler;

    public DisabledEndpointInterceptor() {
    }

    public void setDiscoveryApiService(DiscoveryApiService discoveryApiService) {
        this.blacklistHandler = discoveryApiService.getBlacklistHandler();
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
