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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.org.openbanking.datamodel.discovery.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;

/**
 * A SpringBoot test for the {@link DiscoveryController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "rs.discovery.versions.v3.1.10=true",
        "rs.discovery.apis.GetDomesticPayment=false",
        "rs.discovery.versionApiOverrides.v3_1_10.GetAccount=false"})
public class DiscoveryControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String DISCOVERY_PATH = "/open-banking/discovery";
    private static final String FINANCIAL_ID = "0015800001041REAAY";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldGetDiscoveryUrlsFilteredByApiEndpoint() {
        // Given
        OBApiReference[] disabledEndpoints = {OBApiReference.GET_DOMESTIC_PAYMENT};
        OBApiReference[] enabledEndpoints = {OBApiReference.CREATE_DOMESTIC_PAYMENT};

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
    public void shouldGetDiscoveryUrlsForGivenDomain() {
        // Given
        String version = "v3.1.10";
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-Host", "forgerock.com");
        headers.add("X-Forwarded-Proto", "https");

        // When
        ResponseEntity<OBDiscoveryResponse> response = restTemplate.exchange(discoveryUrl(), GET, new HttpEntity<>(headers),
                OBDiscoveryResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDiscovery data = response.getBody().getData();
        OBDiscoveryAPI<OBDiscoveryAPILinks> accountEndpointsForVersion = data.getAccountAndTransactionAPIs().stream()
                .filter(a -> a.getVersion().equals(version))
                .findFirst()
                .orElseThrow();
        String transactionsUrl = ((GenericOBDiscoveryAPILinks) accountEndpointsForVersion.getLinks())
                .getLinks()
                .get(OBApiReference.GET_TRANSACTIONS.getReference());
        String expectedUrl = "https://forgerock.com/open-banking/" + version + OBApiReference.GET_TRANSACTIONS.getRelativePath();
        assertThat(transactionsUrl).isEqualTo(expectedUrl);
    }

    @Test
    public void shouldGetDiscoveryUrlsForDefaultDomain() {
        // Given
        String version = "v3.1.10";

        // When
        ResponseEntity<OBDiscoveryResponse> response = restTemplate.getForEntity(discoveryUrl(), OBDiscoveryResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDiscovery data = response.getBody().getData();
        OBDiscoveryAPI<OBDiscoveryAPILinks> accountEndpointsForVersion = data.getAccountAndTransactionAPIs().stream()
                .filter(a -> a.getVersion().equals(version))
                .findFirst()
                .orElseThrow();
        String transactionsUrl = ((GenericOBDiscoveryAPILinks) accountEndpointsForVersion.getLinks())
                .getLinks()
                .get(OBApiReference.GET_TRANSACTIONS.getReference());
        String expectedUrl = BASE_URL + port + "/open-banking/" + version + OBApiReference.GET_TRANSACTIONS.getRelativePath();
        assertThat(transactionsUrl).isEqualTo(expectedUrl);
    }

    @Test
    public void shouldHandleConcurrentRequests() throws Exception {
        final int numConcurrentRequests = 16;
        final ExecutorService executorService = Executors.newFixedThreadPool(numConcurrentRequests);
        final List<Callable<ResponseEntity<OBDiscoveryResponse>>> tasks = Collections.nCopies(numConcurrentRequests, () ->  restTemplate.getForEntity(discoveryUrl(), OBDiscoveryResponse.class));
        final List<Future<ResponseEntity<OBDiscoveryResponse>>> futures = executorService.invokeAll(tasks);
        final List<ResponseEntity<OBDiscoveryResponse>> responses = new ArrayList<>();
        for (final Future<ResponseEntity<OBDiscoveryResponse>> future : futures) {
            responses.add(future.get(2, TimeUnit.SECONDS));
        }

        // Verify that all requests succeeded and got the same response body
        final Set<OBDiscoveryResponse> discoveryResponses = new HashSet<>();
        for (ResponseEntity<OBDiscoveryResponse> response : responses) {
            Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            discoveryResponses.add(response.getBody());
        }
        assertThat(discoveryResponses.size()).isEqualTo(1);
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