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
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.discovery.AvailableApisTestDataFactory;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBGroupName;
import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import uk.org.openbanking.datamodel.discovery.GenericOBDiscoveryAPILinks;
import uk.org.openbanking.datamodel.discovery.OBDiscoveryAPI;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DiscoveryApiService}.
 */
public class DiscoveryApiServiceTest {

    /**
     * The RequestContext baseUri produced by HATEOAS when running this with the MockHttpServletRequest
     */
    private static final String BASE_URI = "http://localhost";

    private static final String TEST_VERSION = "v3.1.1";

    private AvailableApiEndpointsResolver availableApisResolver = mock(AvailableApiEndpointsResolver.class);

    @BeforeEach
    void setup() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    /**
     * Helper method used in assertions. This builds URL we expect to see in the discovery doc for a particular
     * endpoint and version.
     */
    private static String buildDiscoveryDocumentUrl(String version, String endpointPath) {
        return BASE_URI + AvailableApisTestDataFactory.BASE_URL + version + endpointPath;
    }

    @Test
    public void shouldGetDiscoveryApisByVersionAndGroupName() {
        // Given
        DiscoveryApiConfigurationProperties discoveryProperties = new DiscoveryApiConfigurationProperties();
        DiscoveryApiService discoveryApiService = new DiscoveryApiService(discoveryProperties, availableApisResolver);
        when(availableApisResolver.getAvailableApiEndpoints()).thenReturn(AvailableApisTestDataFactory.getAvailableApiEndpoints());

        // When
        Map<OBGroupName, Map<String, OBDiscoveryAPI>> discoveryApis = discoveryApiService.getDiscoveryApis();

        // Then
        assertThat(discoveryApis).isNotNull();
        assertThat(containsAllVersions(discoveryApis.get(OBGroupName.AISP))).isTrue();
        assertThat(containsAllVersions(discoveryApis.get(OBGroupName.PISP))).isTrue();
        assertThat(containsAllVersions(discoveryApis.get(OBGroupName.EVENT))).isTrue();
        assertThat(containsAllVersions(discoveryApis.get(OBGroupName.CBPII))).isTrue();
    }

    @Test
    public void shouldGetDiscoveryApisWithoutSpecificVersion() {
        // Given
        DiscoveryApiConfigurationProperties discoveryProperties = new DiscoveryApiConfigurationProperties();
        discoveryProperties.setVersions(ImmutableMap.of(TEST_VERSION, false));
        DiscoveryApiService discoveryApiService = new DiscoveryApiService(discoveryProperties, availableApisResolver);
        when(availableApisResolver.getAvailableApiEndpoints()).thenReturn(AvailableApisTestDataFactory.getAvailableApiEndpoints());

        // When
        Map<OBGroupName, Map<String, OBDiscoveryAPI>> discoveryApis = discoveryApiService.getDiscoveryApis();

        // Then
        assertThat(discoveryApis).isNotNull();
        assertThat(containsAllVersionsExcept(discoveryApis.get(OBGroupName.AISP), 1)).isTrue();
        assertThat(containsAllVersionsExcept(discoveryApis.get(OBGroupName.PISP), 1)).isTrue();
        assertThat(containsAllVersionsExcept(discoveryApis.get(OBGroupName.EVENT), 1)).isTrue();
        assertThat(containsAllVersionsExcept(discoveryApis.get(OBGroupName.CBPII), 1)).isTrue();
    }

    @Test
    public void shouldGetDiscoveryApisWithoutSpecificEndpoint() {
        // Given
        DiscoveryApiConfigurationProperties discoveryProperties = new DiscoveryApiConfigurationProperties();
        discoveryProperties.setApis(ImmutableMap.of(OBApiReference.GET_ACCOUNT, false));
        DiscoveryApiService discoveryApiService = new DiscoveryApiService(discoveryProperties, availableApisResolver);
        when(availableApisResolver.getAvailableApiEndpoints()).thenReturn(AvailableApisTestDataFactory.getAvailableApiEndpoints());

        // When
        Map<OBGroupName, Map<String, OBDiscoveryAPI>> discoveryApis = discoveryApiService.getDiscoveryApis();

        // Then
        Map<String, OBDiscoveryAPI> accountApis = discoveryApis.get(OBGroupName.AISP);
        assertThat(containsAllVersions(accountApis)).isTrue();
        Map<String, String> links = ((GenericOBDiscoveryAPILinks) accountApis.get(TEST_VERSION).getLinks()).getLinks();
        // assert GET_ACCOUNT is excluded but GET_ACCOUNTS is present
        assertThat(links.containsKey(OBApiReference.GET_ACCOUNT.getReference())).isFalse();
        assertThat(links.containsValue(buildDiscoveryDocumentUrl(TEST_VERSION, "/aisp/accounts/{AccountId}"))).isFalse();
        assertThat(links.containsKey(OBApiReference.GET_ACCOUNTS.getReference())).isTrue();
        assertThat(links.containsValue(buildDiscoveryDocumentUrl(TEST_VERSION, "/aisp/accounts"))).isTrue();
    }

    @Test
    public void shouldGetDiscoveryApisWithoutSpecificEndpointVersion() {
        // Given
        DiscoveryApiConfigurationProperties discoveryProperties = new DiscoveryApiConfigurationProperties();
        discoveryProperties.setVersionApiOverrides(ImmutableMap.of("v3_1_2", ImmutableMap.of(OBApiReference.GET_ACCOUNTS, false)));
        DiscoveryApiService discoveryApiService = new DiscoveryApiService(discoveryProperties, availableApisResolver);
        when(availableApisResolver.getAvailableApiEndpoints()).thenReturn(AvailableApisTestDataFactory.getAvailableApiEndpoints());

        // When
        Map<OBGroupName, Map<String, OBDiscoveryAPI>> discoveryApis = discoveryApiService.getDiscoveryApis();

        // Then
        Map<String, OBDiscoveryAPI> accountApis = discoveryApis.get(OBGroupName.AISP);
        String version = "v3.1.2";
        Map<String, String> links = ((GenericOBDiscoveryAPILinks) accountApis.get(version).getLinks()).getLinks();
        assertThat(links.containsKey(OBApiReference.GET_ACCOUNT.getReference())).isTrue();
        assertThat(links.containsValue(buildDiscoveryDocumentUrl(version, "/aisp/accounts/{AccountId}"))).isTrue();
        // assert GET_ACCOUNTS is excluded
        assertThat(links.containsKey(OBApiReference.GET_ACCOUNTS.getReference())).isFalse();
        assertThat(links.containsValue(buildDiscoveryDocumentUrl( version, "/aisp/accounts"))).isFalse();

        // check another version and assert both links are included
        links = ((GenericOBDiscoveryAPILinks) accountApis.get(TEST_VERSION).getLinks()).getLinks();
        assertThat(links.containsKey(OBApiReference.GET_ACCOUNT.getReference())).isTrue();
        assertThat(links.containsValue(buildDiscoveryDocumentUrl( TEST_VERSION, "/aisp/accounts/{AccountId}"))).isTrue();
        assertThat(links.containsKey(OBApiReference.GET_ACCOUNTS.getReference())).isTrue();
        assertThat(links.containsValue(buildDiscoveryDocumentUrl(TEST_VERSION, "/aisp/accounts"))).isTrue();
    }

    @Test
    public void shouldGetEmptyDiscoveryApisGivenNoAvailableEndpoints() {
        // Given
        DiscoveryApiConfigurationProperties discoveryProperties = new DiscoveryApiConfigurationProperties();
        DiscoveryApiService discoveryApiService = new DiscoveryApiService(discoveryProperties, availableApisResolver);
        when(availableApisResolver.getAvailableApiEndpoints()).thenReturn(emptyList());

        // When
        Map<OBGroupName, Map<String, OBDiscoveryAPI>> discoveryApis = discoveryApiService.getDiscoveryApis();

        // Then
        assertThat(discoveryApis).isEmpty();
    }

    @Test
    public void shouldGetEmptyDiscoveryApisGivenAllVersionsDisabled() {
        // Given
        DiscoveryApiConfigurationProperties discoveryProperties = new DiscoveryApiConfigurationProperties();
        discoveryProperties.setVersions(allVersionsDisabled());
        DiscoveryApiService discoveryApiService = new DiscoveryApiService(discoveryProperties, availableApisResolver);
        when(availableApisResolver.getAvailableApiEndpoints()).thenReturn(AvailableApisTestDataFactory.getAvailableApiEndpoints());

        // When
        Map<OBGroupName, Map<String, OBDiscoveryAPI>> discoveryApis = discoveryApiService.getDiscoveryApis();

        // Then
        assertThat(discoveryApis).isEmpty();
    }

    private boolean containsAllVersions(Map<String, OBDiscoveryAPI> apiVersions) {
        boolean isValid = true;
        for (int patch = 1; patch <= AvailableApisTestDataFactory.PATCHES; patch++) {
            if (!apiVersions.containsKey(AvailableApisTestDataFactory.VERSION_PREFIX + patch)) {
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean containsAllVersionsExcept(Map<String, OBDiscoveryAPI> apiVersions, int excludedPatch) {
        boolean isValid = true;
        for (int patch = 1; patch <= AvailableApisTestDataFactory.PATCHES; patch++) {
            if (patch != excludedPatch && !apiVersions.containsKey(AvailableApisTestDataFactory.VERSION_PREFIX + patch)) {
                isValid = false;
            }
        }
        return isValid;
    }

    private Map<String, Boolean> allVersionsDisabled() {
        Map<String, Boolean> versions = new HashMap<>();
        for (int patch = 1; patch <= AvailableApisTestDataFactory.PATCHES; patch++) {
            versions.put(AvailableApisTestDataFactory.VERSION_PREFIX + patch, false);
        }
        return versions;
    }
}