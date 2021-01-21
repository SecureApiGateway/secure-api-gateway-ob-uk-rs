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
package com.forgerock.securebanking.openbanking.uk.rs.discovery;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticResponse5;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class ControllerEndpointBlacklistHandlerTest {

    private static HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String ENABLED_VERSION = "v3.1.5";
    private static final String DISABLED_VERSION = "v3.1.6";
    private static final String DISABLED_ENDPOINT_OVERRIDE_VERSION = "v3.1.5";

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
    public void shouldCreateDomesticPaymentGivenApiVersionIsEnabled() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);
        String url = paymentsUrl(ENABLED_VERSION);

        // When
        ResponseEntity<?> response = restTemplate.postForEntity(url, request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void shouldFailToCreateDomesticPaymentGivenApiVersionIsDisabled() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);
        String url = paymentsUrl(DISABLED_VERSION);

        // When
        ResponseEntity<?> response = restTemplate.postForEntity(url, request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailToGetDomesticPaymentGivenApiEndpointIsDisabled() {
        // Given
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(aValidOBWriteDomestic2(), HTTP_HEADERS);
        ResponseEntity<OBWriteDomesticResponse5> persistedPayment = restTemplate.postForEntity(
                paymentsUrl(ENABLED_VERSION),
                request,
                OBWriteDomesticResponse5.class);
        String url = paymentsIdUrl(ENABLED_VERSION, persistedPayment.getBody().getData().getDomesticPaymentId());

        // When
        ResponseEntity<?> response = restTemplate.exchange(url, GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailToGetDomesticPaymentFundsConfirmationGivenApiEndpointIsDisabledForVersion() {
        // Given
        OBWriteDomestic2 obWriteDomestic2 = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(obWriteDomestic2, HTTP_HEADERS);
        String url = fundsConfirmationUrl(
                DISABLED_ENDPOINT_OVERRIDE_VERSION,
                obWriteDomestic2.getData().getConsentId(),
                "1234",
                "10.00");

        // When
        ResponseEntity<?> response = restTemplate.postForEntity(url, request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String paymentsUrl(String version) {
        return BASE_URL + port + "/open-banking/" + version + "/pisp/domestic-payments";
    }

    private String paymentsIdUrl(String version, String id) {
        return paymentsUrl(version) + "/" + id;
    }

    private String paymentConsentsUrl(String version, String consentId) {
        return BASE_URL + port + "/open-banking/" + version + "/domestic-payment-consents/" + consentId;
    }

    private String fundsConfirmationUrl(String version, String consentId, String accountId, String amount) {
        return paymentConsentsUrl(version, consentId) + "?accountId=" + accountId + "&amount=" + amount;
    }
}