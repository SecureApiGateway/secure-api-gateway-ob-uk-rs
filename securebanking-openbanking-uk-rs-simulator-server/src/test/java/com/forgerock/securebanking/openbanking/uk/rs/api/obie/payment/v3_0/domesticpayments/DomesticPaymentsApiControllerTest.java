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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.domesticpayments;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomestic1;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticResponse1;

import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBDomestic1;

/**
 * A SpringBoot test for the {@link DomesticPaymentsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticPaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String DOMESTIC_PAYMENTS_URI = "/open-banking/v3.0/pisp/domestic-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticPaymentSubmissionRepository domesticPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        domesticPaymentRepository.deleteAll();
    }

    @Test
    public void shouldCreateDomesticPayment() {
        // Given
        OBWriteDomestic1 payment = aValidOBWriteDomestic1();
        HttpEntity<OBWriteDomestic1> request = new HttpEntity<>(payment, HTTP_HEADERS);

        // When
        ResponseEntity<OBWriteDomesticResponse1> response = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDataDomesticResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();
    }

    @Test
    public void shouldGetDomesticPaymentById() {
        // Given
        OBWriteDomestic1 payment = aValidOBWriteDomestic1();
        HttpEntity<OBWriteDomestic1> request = new HttpEntity<>(payment, HTTP_HEADERS);
        ResponseEntity<OBWriteDomesticResponse1> persistedPayment = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse1.class);
        String url = paymentIdUrl(persistedPayment.getBody().getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWriteDomesticResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDataDomesticResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();
    }

    @Test
    public void shouldAvoidCreatingNewPaymentWhenPaymentWithSameConsentIdAlreadyExists() {
        // Given
        OBWriteDomestic1 payment = aValidOBWriteDomestic1();
        HttpEntity<OBWriteDomestic1> request = new HttpEntity<>(payment, HTTP_HEADERS);
        ResponseEntity<OBWriteDomesticResponse1> initialResponse = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse1.class);
        String paymentId = initialResponse.getBody().getData().getDomesticPaymentId();

        // When
        ResponseEntity<OBWriteDomesticResponse1> subsequentResponse = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse1.class);

        // Then
        assertThat(subsequentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDataDomesticResponse1 responseData = subsequentResponse.getBody().getData();
        // ensure idempotentSave works
        assertThat(responseData.getDomesticPaymentId()).isEqualTo(paymentId);
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
    }

    @Test
    public void shouldCreateNewPaymentWhenPaymentWithDifferentConsentIdAlreadyExists() {
        // Given
        OBWriteDomestic1 initialPayment = aValidOBWriteDomestic1();
        HttpEntity<OBWriteDomestic1> initialRequest = new HttpEntity<>(initialPayment, HTTP_HEADERS);
        ResponseEntity<OBWriteDomesticResponse1> initialResponse = restTemplate.postForEntity(paymentsUrl(), initialRequest, OBWriteDomesticResponse1.class);
        String initialPaymentId = initialResponse.getBody().getData().getDomesticPaymentId();
        OBWriteDomestic1 paymentWithDifferentConsentId = aValidOBWriteDomestic1()
                .data(aValidOBWriteDataDomestic1().consentId(UUID.randomUUID().toString()));
        HttpEntity<OBWriteDomestic1> subsequentRequest = new HttpEntity<>(paymentWithDifferentConsentId, HTTP_HEADERS);

        // When
        ResponseEntity<OBWriteDomesticResponse1> subsequentResponse = restTemplate.postForEntity(paymentsUrl(), subsequentRequest, OBWriteDomesticResponse1.class);

        // Then
        assertThat(subsequentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDataDomesticResponse1 responseData = subsequentResponse.getBody().getData();
        // ensure idempotentSave works
        assertThat(responseData.getDomesticPaymentId()).isNotEqualTo(initialPaymentId);
    }

    private String paymentsUrl() {
        return BASE_URL + port + DOMESTIC_PAYMENTS_URI;
    }

    private String paymentIdUrl(String id) {
        return paymentsUrl() + "/" + id;
    }

    private OBWriteDomestic1 aValidOBWriteDomestic1() {
        return new OBWriteDomestic1()
                .data(aValidOBWriteDataDomestic1())
                .risk(aValidOBRisk1());
    }

    private OBWriteDataDomestic1 aValidOBWriteDataDomestic1() {
        return new OBWriteDataDomestic1()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBDomestic1());
    }
}