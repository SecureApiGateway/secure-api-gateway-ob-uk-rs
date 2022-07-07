/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.cors;

import com.forgerock.securebanking.common.cors.CorsConstants;
import com.forgerock.securebanking.common.cors.configuration.CorsConfigurationProperties;
import com.forgerock.securebanking.common.cors.filter.CorsFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.ServletException;
import java.io.IOException;

import static com.forgerock.securebanking.common.cors.CorsConstants.*;
import static javax.servlet.http.HttpServletResponse.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link CorsFilter}
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = {CorsFilter.class}, initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = CorsConfigurationProperties.class)
@ActiveProfiles("test")
public class CorsFilterTest {
    private static final String ORIGIN_DOMAIN = "https://domain4test.com";

    @Autowired
    private CorsFilter corsFilter;

    @Test
    public void when_GET_domainIsAllowed() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(CorsConstants.HEADER_ORIGIN, ORIGIN_DOMAIN);
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        corsFilter.doFilter(req, res, chain);
        assertThat(res.getStatus()).isEqualTo(SC_OK);
        assertThat(res.getHeaderNames()).containsExactlyInAnyOrder(
                HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS,
                HEADER_ACCESS_CONTROL_ALLOW_HEADERS,
                HEADER_ACCESS_CONTROL_ALLOW_METHODS,
                HEADER_ACCESS_CONTROL_MAX_AGE
        );
    }

    @Test
    public void when_OPTIONS_domainIsAllowed() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(CorsConstants.HEADER_ORIGIN, ORIGIN_DOMAIN);
        req.setMethod(HttpMethod.OPTIONS.name());
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        corsFilter.doFilter(req, res, chain);
        assertThat(res.getStatus()).isEqualTo(SC_ACCEPTED);
        assertThat(res.getHeaderNames()).containsExactlyInAnyOrder(
                HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS,
                HEADER_ACCESS_CONTROL_ALLOW_HEADERS,
                HEADER_ACCESS_CONTROL_ALLOW_METHODS,
                HEADER_ACCESS_CONTROL_MAX_AGE
        );
    }

    @Test
    public void domainIsNotAllowed() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(CorsConstants.HEADER_ORIGIN, "https://notallowed.domain");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        corsFilter.doFilter(req, res, chain);
        assertThat(res.getStatus()).isEqualTo(SC_UNAUTHORIZED);
    }
}
