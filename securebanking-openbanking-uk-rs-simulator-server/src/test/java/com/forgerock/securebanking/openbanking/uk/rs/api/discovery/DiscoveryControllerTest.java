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
package com.forgerock.securebanking.openbanking.uk.rs.api.discovery;

import com.forgerock.securebanking.openbanking.uk.rs.common.OBApiReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.org.openbanking.datamodel.discovery.*;

import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rs.common.OBApiReference.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * A SpringBoot test for the {@link DiscoveryController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "rs.discovery.versions.v3.1.5=true",
        "rs.discovery.versions.v3.1.6=false",
        "rs.discovery.apis.GetDomesticPayment=false",
        "rs.discovery.versionApiOverrides.v3_1_5.GetAccount=false"})
public class DiscoveryControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String DISCOVERY_PATH = "/open-banking/discovery";
    private static final String FINANCIAL_ID = "0015800001041REAAY";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldGetDiscoveryUrlsFilteredByVersion() {
        // Given
        String[] enabledVersions = {"v3.1.5"};
        String[] disabledVersions = {"v3.1.6"};

        // When
        ResponseEntity<OBDiscoveryResponse> response = restTemplate.getForEntity(discoveryUrl(), OBDiscoveryResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDiscovery data = response.getBody().getData();
        assertThat(data.getFinancialId()).isEqualTo(FINANCIAL_ID);
        List<OBDiscoveryAPI<OBDiscoveryAPILinks>> paymentApis = data.getPaymentInitiationAPIs();
        assertThat(containsVersions(paymentApis, enabledVersions)).isTrue();
        // assert others are filtered
        assertThat(containsVersions(paymentApis, disabledVersions)).isFalse();
    }

    @Test
    public void shouldGetDiscoveryUrlsFilteredByApiEndpoint() {
        // Given
        OBApiReference[] disabledEndpoints = {GET_DOMESTIC_PAYMENT};
        OBApiReference[] enabledEndpoints = {CREATE_DOMESTIC_PAYMENT};

        // When
        ResponseEntity<OBDiscoveryResponse> response = restTemplate.getForEntity(discoveryUrl(), OBDiscoveryResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDiscovery data = response.getBody().getData();
        List<OBDiscoveryAPI<OBDiscoveryAPILinks>> paymentApis = data.getPaymentInitiationAPIs();
        assertThat(isEndpointDisabled(paymentApis, disabledEndpoints)).isTrue();
        // assert others are not filtered
        assertThat(isEndpointDisabled(paymentApis, enabledEndpoints)).isFalse();
    }

    @Test
    public void shouldGetDiscoveryUrlsFilteredByVersionAndApiEndpoint() {
        // Given
        String version = "v3.1.5";
        OBApiReference disabledEndpoint = GET_ACCOUNT;

        // When
        ResponseEntity<OBDiscoveryResponse> response = restTemplate.getForEntity(discoveryUrl(), OBDiscoveryResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDiscovery data = response.getBody().getData();
        List<OBDiscoveryAPI<OBDiscoveryAPILinks>> accountApis = data.getAccountAndTransactionAPIs();
        assertThat(isEndpointDisabledByVersion(accountApis, version, disabledEndpoint)).isTrue();
        // assert others are not filtered
        assertThat(isEndpointDisabledByVersion(accountApis, "v3.1.4", disabledEndpoint)).isFalse();
        assertThat(isEndpointDisabledByVersion(accountApis, version, GET_ACCOUNTS)).isFalse();
    }

    private boolean containsVersions(List<OBDiscoveryAPI<OBDiscoveryAPILinks>> api, String... versions) {
        boolean contains = true;
        for (String version : versions) {
            contains = api.stream().anyMatch(a -> a.getVersion().equals(version));
        }
        return contains;
    }

    private boolean isEndpointDisabled(List<OBDiscoveryAPI<OBDiscoveryAPILinks>> api, OBApiReference... endpoints) {
        boolean isDisabled = false;
        for (OBApiReference endpoint : endpoints) {
            isDisabled = api.stream()
                    .map(a -> ((GenericOBDiscoveryAPILinks) a.getLinks()))
                    .flatMap(l -> l.getLinks().entrySet().stream())
                    .noneMatch(l -> l.getKey().equals(endpoint.getReference()));
        }
        return isDisabled;
    }

    private boolean isEndpointDisabledByVersion(List<OBDiscoveryAPI<OBDiscoveryAPILinks>> api, String version, OBApiReference endpoint) {
        return api.stream()
                .filter(a -> a.getVersion().equals(version))
                .map(a -> ((GenericOBDiscoveryAPILinks) a.getLinks()))
                .flatMap(l -> l.getLinks().entrySet().stream())
                .noneMatch(l -> l.getKey().equals(endpoint.getReference()));
    }

    private String discoveryUrl() {
        return BASE_URL + port + DISCOVERY_PATH;
    }
}