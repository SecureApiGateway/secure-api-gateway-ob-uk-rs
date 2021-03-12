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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.internationalpayments;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternational1;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteInternational1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalResponse1;

import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory.aValidOBInternational1;

/**
 * A SpringBoot test for the {@link InternationalPaymentsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class InternationalPaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String INTERNATIONAL_PAYMENTS_URI = "/open-banking/v3.0/pisp/international-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private InternationalPaymentSubmissionRepository internationalPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        internationalPaymentRepository.deleteAll();
    }

    @Test
    public void shouldCreateInternationalPayment() {
        // Given
        OBWriteInternational1 payment = aValidOBWriteInternational1();
        HttpEntity<OBWriteInternational1> request = new HttpEntity<>(payment, HTTP_HEADERS);

        // When
        ResponseEntity<OBWriteInternationalResponse1> response = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDataInternationalResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/international-payments/" + responseData.getInternationalPaymentId())).isTrue();
    }

    @Test
    public void shouldGetInternationalPaymentById() {
        // Given
        OBWriteInternational1 payment = aValidOBWriteInternational1();
        HttpEntity<OBWriteInternational1> request = new HttpEntity<>(payment, HTTP_HEADERS);
        ResponseEntity<OBWriteInternationalResponse1> persistedPayment = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse1.class);
        String url = paymentIdUrl(persistedPayment.getBody().getData().getInternationalPaymentId());

        // When
        ResponseEntity<OBWriteInternationalResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteInternationalResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDataInternationalResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/international-payments/" + responseData.getInternationalPaymentId())).isTrue();
    }

    private String paymentsUrl() {
        return BASE_URL + port + INTERNATIONAL_PAYMENTS_URI;
    }

    private String paymentIdUrl(String id) {
        return paymentsUrl() + "/" + id;
    }

    private OBWriteInternational1 aValidOBWriteInternational1() {
        return new OBWriteInternational1()
                .data(aValidOBWriteDataInternational1())
                .risk(aValidOBRisk1());
    }

    private OBWriteDataInternational1 aValidOBWriteDataInternational1() {
        return new OBWriteDataInternational1()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBInternational1());
    }
}