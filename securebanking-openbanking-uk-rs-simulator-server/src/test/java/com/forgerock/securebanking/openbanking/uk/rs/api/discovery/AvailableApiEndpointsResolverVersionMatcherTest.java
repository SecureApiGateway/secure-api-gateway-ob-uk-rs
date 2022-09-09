package com.forgerock.securebanking.openbanking.uk.rs.api.discovery;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;

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
            assertTrue(resolver.matchUrlPattern(url).matches(), "url must be accepted: " + url);
        }
    }
}
