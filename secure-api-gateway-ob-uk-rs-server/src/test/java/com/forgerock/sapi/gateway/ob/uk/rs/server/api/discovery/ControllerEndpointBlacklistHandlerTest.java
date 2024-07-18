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

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import org.junit.jupiter.api.AfterEach;
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
import uk.org.openbanking.datamodel.v3.payment.*;


import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion.v3_1_5;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "rs.discovery.versions.v3.1.5=true",
        "rs.discovery.versions.v3.1.6=false",
        "rs.discovery.apis.GetDomesticPayment=false",
        "rs.discovery.versionApiOverrides.v3_1_5.GetAccount=false"})
public class ControllerEndpointBlacklistHandlerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final OBVersion DISABLED_ENDPOINT_OVERRIDE_VERSION = v3_1_5;
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DomesticPaymentSubmissionRepository domesticPaymentSubmissionRepository;

    @AfterEach
    void removeData() {
        domesticPaymentSubmissionRepository.deleteAll();
    }

    @Test
    public void shouldFailToCreateDomesticPaymentGivenApiVersionIsDisabled() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HttpHeadersTestDataFactory.requiredPaymentHttpHeaders("testClient"));


        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(OBVersion.v3_1_6), request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(paymentSubmitted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailToGetDomesticPaymentGivenApiEndpointIsDisabled() throws ExceptionClient {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HttpHeadersTestDataFactory.requiredPaymentHttpHeaders("testClient"));

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(OBVersion.v3_1_6), request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(paymentSubmitted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailToGetAccountGivenApiEndpointIsDisabledForVersion() {
        // Given
        String accountId = "1234";
        String url = accountIdUrl(DISABLED_ENDPOINT_OVERRIDE_VERSION, accountId);
        HttpHeaders httpHeaders = HttpHeadersTestDataFactory.requiredAccountApiHeaders("asdasd", "testClient");

        // When
        ResponseEntity<?> response = restTemplate.getForEntity(url, Void.class, httpHeaders);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String paymentsUrl(OBVersion version) {
        return BASE_URL + port + "/open-banking/" + version.getCanonicalName() + "/pisp/domestic-payments";
    }

    private String paymentsIdUrl(OBVersion version, String id) {
        return paymentsUrl(version) + "/" + id;
    }

    private String accountIdUrl(OBVersion version, String id) {
        return BASE_URL + port + "/open-banking/" + version.getCanonicalName() + "/aisp/accounts/" + id;
    }
}
