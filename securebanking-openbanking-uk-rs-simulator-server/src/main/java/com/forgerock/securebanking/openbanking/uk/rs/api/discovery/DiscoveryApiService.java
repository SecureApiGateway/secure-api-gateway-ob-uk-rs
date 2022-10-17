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
package com.forgerock.securebanking.openbanking.uk.rs.api.discovery;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBGroupName;
import com.forgerock.securebanking.openbanking.uk.rs.common.OBApiReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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

    private final Map<OBGroupName, Map<String, OBDiscoveryAPI>> discoveryApis = new HashMap<>();

    private boolean blacklistInitialized = false;

    public DiscoveryApiService(DiscoveryApiConfigurationProperties discoveryProperties,
                               AvailableApiEndpointsResolver availableApiEndpointsResolver,
                               ControllerEndpointBlacklistHandler blacklistHandler) {
        this.discoveryProperties = discoveryProperties;
        this.availableApiEndpointsResolver = availableApiEndpointsResolver;
        this.blacklistHandler = blacklistHandler;
    }

    /**
     * Builds a {@link Map} of Open Banking APIs that both the application and customer supports. The APIs are grouped
     * by "group" name (e.g. AISP, PISP) and listed by version.
     *
     * @return a {@link Map} of supported Open Banking APIs.
     */
    public Map<OBGroupName, Map<String, OBDiscoveryAPI>> getDiscoveryApis() {
        List<AvailableApiEndpoint> availableEndpoints = availableApiEndpointsResolver.getAvailableApiEndpoints();

        // iterate over each API endpoint
        for (AvailableApiEndpoint availableEndpoint : availableEndpoints) {
            String version = availableEndpoint.getVersion();
            OBApiReference endpointReference = availableEndpoint.getApiReference();
            String endpointUrl = availableEndpoint.getUrl();
            log.error("endpointUrl: " + endpointUrl);
            if (isVersionEnabled(version)
                    && isApiEnabled(endpointReference)
                    && isVersionOverrideEnabled(version, endpointReference)) {
                log.error("Version is enabled for: {}, {}", endpointUrl, availableEndpoint.getGroupName());

                // Init map
                if (!discoveryApis.containsKey(availableEndpoint.getGroupName())) {
                    discoveryApis.put(availableEndpoint.getGroupName(), new HashMap<>());
                    log.error("Putting group in available endpoints: {}, {}", endpointUrl, availableEndpoint.getGroupName());
                }

                if (!discoveryApis.get(availableEndpoint.getGroupName()).containsKey(availableEndpoint.getVersion())) {
                    discoveryApis.get(availableEndpoint.getGroupName())
                            .put(availableEndpoint.getVersion(), new OBDiscoveryAPI<GenericOBDiscoveryAPILinks>()
                                    .version(availableEndpoint.getVersion())
                                    .links(new GenericOBDiscoveryAPILinks()));
                    log.error("Putting endpoints because no version was found for current endpoint: {}, {}", endpointUrl, availableEndpoint.getGroupName());
                }
                GenericOBDiscoveryAPILinks links = (GenericOBDiscoveryAPILinks) discoveryApis
                        .get(availableEndpoint.getGroupName())
                        .get(availableEndpoint.getVersion())
                        .getLinks();
                log.error("Adding URL: " + endpointUrl);
                links.addLink(endpointReference.getReference(), endpointUrl);
            } else {
                log.error("Version is not enabled for: {}, {}", endpointUrl, availableEndpoint.getGroupName());
            }
        }

        return discoveryApis;
    }

    /**
     * Returns a {@link List} of disabled endpoints for the {@link ControllerEndpointBlacklistHandler}.
     * Lazy initialises the {@link List} since it requires a servlet request context to determine the base URL (e.g.
     * using X-Forwarded headers).
     */
    public ControllerEndpointBlacklistHandler getControllerBlackListHandler() {
        if (!blacklistInitialized) {
            initBlacklist();
            blacklistInitialized = true;
        }

        return blacklistHandler;
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
