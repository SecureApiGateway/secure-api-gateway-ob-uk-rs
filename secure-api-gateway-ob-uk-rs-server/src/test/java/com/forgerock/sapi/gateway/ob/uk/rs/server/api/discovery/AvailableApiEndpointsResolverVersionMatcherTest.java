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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvailableApiEndpointsResolverVersionMatcherTest {

    /**
     * Test to verify the regex in AvailableApiEndpointsResolver supports all the OBVersions
     */
    @Test
    void mustMatchAllObVersions() {
        final AvailableApiEndpointsResolver resolver = new AvailableApiEndpointsResolver(Mockito.mock(RequestMappingHandlerMapping.class));
        final String urlTemplate = "/open-banking/%s/pisp/domestic-payments";
        for (OBVersion version : OBVersion.values()) {
            final String url = String.format(urlTemplate, version.getCanonicalName());
            assertTrue(resolver.matchUrlPattern(url).matches(), "uriPath must be accepted: " + url);
        }
    }
}
