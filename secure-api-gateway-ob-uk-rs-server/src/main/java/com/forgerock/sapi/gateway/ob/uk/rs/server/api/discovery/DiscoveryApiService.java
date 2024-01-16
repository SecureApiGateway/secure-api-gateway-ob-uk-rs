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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.OBApiReference;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBGroupName;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import uk.org.openbanking.datamodel.discovery.GenericOBDiscoveryAPILinks;
import uk.org.openbanking.datamodel.discovery.OBDiscoveryAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Examines customer specific config (if applicable) and determines which of the available APIs should be
 * enabled/disabled. The resulting list of supported APIs is provided by the Discovery API.
 */
@Component
@Slf4j
public class DiscoveryApiService {

    private final DiscoveryApiConfigurationProperties discoveryProperties;

    private final AvailableApiEndpointsResolver availableApiEndpointsResolver;

    private final ControllerEndpointBlacklistHandler blacklistHandler;


    public DiscoveryApiService(DiscoveryApiConfigurationProperties discoveryProperties,
                               AvailableApiEndpointsResolver availableApiEndpointsResolver) {
        this.discoveryProperties = discoveryProperties;
        this.availableApiEndpointsResolver = availableApiEndpointsResolver;
        this.blacklistHandler = new ControllerEndpointBlacklistHandler();
        initBlacklist();
    }

    private void initBlacklist() {
        List<AvailableApiEndpoint> availableEndpoints = availableApiEndpointsResolver.getAvailableApiEndpoints();

        // iterate over each API endpoint
        for (AvailableApiEndpoint availableEndpoint : availableEndpoints) {
            String version = availableEndpoint.getVersion();
            OBApiReference endpointReference = availableEndpoint.getApiReference();

            if (!isVersionEnabled(version)
                    || !isApiEnabled(endpointReference)
                    || !isVersionOverrideEnabled(version, endpointReference)) {
                log.warn("Disabling endpoint: [{}], for version: [{}]", endpointReference.getReference(), version);
                blacklistHandler.blacklistEndpoint(availableEndpoint.getControllerMethod());
            }
        }
    }

    public ControllerEndpointBlacklistHandler getBlacklistHandler() {
        return blacklistHandler;
    }

    /**
     * Builds a {@link Map} of Open Banking APIs that both the application and customer supports. The APIs are grouped
     * by "group" name (e.g. AISP, PISP) and listed by version.
     *
     * For enabled APIs, the AvailableApiEndpoint.uriPaths are combined with the baseUri which is determined using
     * ServletUriComponentsBuilder (which requires there to be an active request context). This produces a fully qualified
     * uri that external clients can use to call the API endpoint.
     *
     * @return a {@link Map} of supported Open Banking APIs.
     */
    public Map<OBGroupName, Map<String, OBDiscoveryAPI>> getDiscoveryApis() {
        final Map<OBGroupName, Map<String, OBDiscoveryAPI>> discoveryApis = new HashMap<>();
        final String baseUri = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        final StringBuilder fullyQualifiedEndpointUriBuilder = new StringBuilder(baseUri);
        // iterate over each API endpoint
        for (AvailableApiEndpoint availableEndpoint : availableApiEndpointsResolver.getAvailableApiEndpoints()) {
            fullyQualifiedEndpointUriBuilder.setLength(baseUri.length());
            String version = availableEndpoint.getVersion();
            OBApiReference endpointReference = availableEndpoint.getApiReference();
            String endpointUrl = availableEndpoint.getUriPath();

            if (isVersionEnabled(version)
                    && isApiEnabled(endpointReference)
                    && isVersionOverrideEnabled(version, endpointReference)) {

                // Init map
                if (!discoveryApis.containsKey(availableEndpoint.getGroupName())) {
                    discoveryApis.put(availableEndpoint.getGroupName(), new HashMap<>());
                }
                if (!discoveryApis.get(availableEndpoint.getGroupName()).containsKey(availableEndpoint.getVersion())) {
                    discoveryApis.get(availableEndpoint.getGroupName())
                            .put(availableEndpoint.getVersion(), new OBDiscoveryAPI<GenericOBDiscoveryAPILinks>()
                                    .version(availableEndpoint.getVersion())
                                    .links(new GenericOBDiscoveryAPILinks()));
                }
                GenericOBDiscoveryAPILinks links = (GenericOBDiscoveryAPILinks) discoveryApis
                        .get(availableEndpoint.getGroupName())
                        .get(availableEndpoint.getVersion())
                        .getLinks();
                links.addLink(endpointReference.getReference(), fullyQualifiedEndpointUriBuilder.append(endpointUrl).toString());
            }
        }

        return discoveryApis;
    }

    private boolean isVersionEnabled(String version) {
        return !discoveryProperties.getVersions().containsKey(version) ||
                discoveryProperties.getVersions().get(version);
    }

    private boolean isApiEnabled(OBApiReference obApiReference) {
        return !discoveryProperties.getApis().containsKey(obApiReference) ||
                discoveryProperties.getApis().get(obApiReference);
    }

    private boolean isVersionOverrideEnabled(String version, OBApiReference obApiReference) {
        // Use _ instead of . in yaml to reduce ambiguity over yml separator vs version
        String yamlVersion = version.replace(".", "_");
        Map<String, Map<OBApiReference, Boolean>> versionApiOverrides = discoveryProperties.getVersionApiOverrides();
        Map<OBApiReference, Boolean> referenceApiOverrides = versionApiOverrides.get(yamlVersion);

        if (versionApiOverrides.containsKey(yamlVersion) && referenceApiOverrides.containsKey(obApiReference)) {
            return referenceApiOverrides.get(obApiReference);
        }
        return true;
    }
}
