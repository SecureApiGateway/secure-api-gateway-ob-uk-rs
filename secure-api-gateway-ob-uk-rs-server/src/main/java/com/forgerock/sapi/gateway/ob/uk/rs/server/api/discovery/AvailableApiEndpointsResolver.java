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

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.OBApiReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a list of {@link AvailableApiEndpoint} instances, which represent the Open Banking API endpoints this
 * application has implemented.
 */
@Component
public class AvailableApiEndpointsResolver {

    /**
     * Matches /open-banking/{version}/{endpoint} where {version} is prefixed with 'v' and has either 2 or 3 digits
     * separated by fullstops - e.g. 'v3.0' or 'v3.1.1'.
     */
    private final Pattern OB_URI_PATTERN = Pattern.compile("^\\/open-banking\\/(v\\d+\\.\\d+(?:.\\d+)?)(\\/.+?)$");

    private final List<AvailableApiEndpoint> availableApis;

    public AvailableApiEndpointsResolver(RequestMappingHandlerMapping requestHandlerMapping) {
        this.availableApis = findAvailableApiEndpoints(requestHandlerMapping);
    }

    public List<AvailableApiEndpoint> getAvailableApiEndpoints() {
        return availableApis;
    }

    /**
     * Provides a list of {@link AvailableApiEndpoint} instances, which represent the Open Banking API endpoints this
     * application has implemented.
     *
     * <p>Note that the list of endpoints is derived using Spring's {@link RequestMappingHandlerMapping}, which means
     * each endpoint must have a corresponding controller method, with a <code>@RequestMapping</code> annotation.
     *
     * @return the {@link List} of {@link AvailableApiEndpoint} instances.
     */
    private List<AvailableApiEndpoint> findAvailableApiEndpoints(RequestMappingHandlerMapping requestHandlerMapping) {
        final List<AvailableApiEndpoint> availableApiEndpoints = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : requestHandlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo requestMapping = entry.getKey();
            HandlerMethod method = entry.getValue();

            OBApiReference apiReference = getMatchingApiReference(requestMapping);

            if (apiReference != null) {
                String version = getVersion(requestMapping);
                String uriPath = requestMapping.getPatternsCondition().getPatterns().iterator().next();

                AvailableApiEndpoint availableApi = AvailableApiEndpoint.builder()
                        .groupName(apiReference.getGroupName())
                        .version(version)
                        .controllerMethod(ControllerMethod.of(method.getBeanType(), method.getMethod()))
                        .apiReference(apiReference)
                        .uriPath(uriPath)
                        .build();
                availableApiEndpoints.add(availableApi);
            }
        }
        return Collections.unmodifiableList(availableApiEndpoints);
    }

    private OBApiReference getMatchingApiReference(RequestMappingInfo requestMapping) {
        Set<String> patterns = requestMapping.getPatternsCondition().getPatterns();
        if (!patterns.isEmpty()) {
            String urlPattern = patterns.iterator().next();
            Matcher matcher = matchUrlPattern(urlPattern);
            if (matcher.matches()) {
                String relativePath = matcher.group(2);
                RequestMethod method = requestMapping.getMethodsCondition().getMethods().iterator().next();
                return OBApiReference.fromMethodAndPath(HttpMethod.resolve(method.name()), relativePath);
            }
        }
        return null;
    }

    private String getVersion(RequestMappingInfo requestMapping) {
        Set<String> patterns = requestMapping.getPatternsCondition().getPatterns();
        if (!patterns.isEmpty()) {
            String urlPattern = patterns.iterator().next();
            Matcher matcher = matchUrlPattern(urlPattern);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    Matcher matchUrlPattern(String urlPattern) {
        Matcher matcher = OB_URI_PATTERN.matcher(urlPattern);
        return matcher;
    }
}
