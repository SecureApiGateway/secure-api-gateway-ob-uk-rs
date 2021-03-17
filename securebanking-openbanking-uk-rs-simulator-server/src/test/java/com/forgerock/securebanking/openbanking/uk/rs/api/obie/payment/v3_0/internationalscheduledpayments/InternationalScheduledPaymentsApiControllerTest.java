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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.internationalscheduledpayments;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalScheduledPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalScheduled1;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalScheduledResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduled1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledResponse1;

import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalScheduledConsentTestDataFactory.aValidOBInternationalScheduled1;

/**
 * A SpringBoot test for the {@link InternationalScheduledPaymentsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class InternationalScheduledPaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String SCHEDULED_PAYMENTS_URI = "/open-banking/v3.0/pisp/international-scheduled-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private InternationalScheduledPaymentSubmissionRepository scheduledPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        scheduledPaymentRepository.deleteAll();
    }

    @Test
    public void shouldCreateInternationalScheduledPayment() {
        // Given
        OBWriteInternationalScheduled1 payment = aValidOBWriteInternationalScheduled1();
        HttpEntity<OBWriteInternationalScheduled1> request = new HttpEntity<>(payment, HTTP_HEADERS);
        String url = scheduledPaymentsUrl();

        // When
        ResponseEntity<OBWriteInternationalScheduledResponse1> response = restTemplate.postForEntity(url, request, OBWriteInternationalScheduledResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDataInternationalScheduledResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/international-scheduled-payments/" + responseData.getInternationalScheduledPaymentId())).isTrue();
    }

    @Test
    public void shouldGetInternationalScheduledPaymentById() {
        // Given
        OBWriteInternationalScheduled1 payment = aValidOBWriteInternationalScheduled1();
        HttpEntity<OBWriteInternationalScheduled1> request = new HttpEntity<>(payment, HTTP_HEADERS);
        ResponseEntity<OBWriteInternationalScheduledResponse1> persistedPayment = restTemplate.postForEntity(scheduledPaymentsUrl(), request, OBWriteInternationalScheduledResponse1.class);
        String url = scheduledPaymentIdUrl(persistedPayment.getBody().getData().getInternationalScheduledPaymentId());

        // When
        ResponseEntity<OBWriteInternationalScheduledResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteInternationalScheduledResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDataInternationalScheduledResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/international-scheduled-payments/" + responseData.getInternationalScheduledPaymentId())).isTrue();
    }

    private String scheduledPaymentsUrl() {
        return BASE_URL + port + SCHEDULED_PAYMENTS_URI;
    }

    private String scheduledPaymentIdUrl(String id) {
        return scheduledPaymentsUrl() + "/" + id;
    }

    private OBWriteInternationalScheduled1 aValidOBWriteInternationalScheduled1() {
        return new OBWriteInternationalScheduled1()
                .data(aValidOBWriteDataInternationalScheduled1())
                .risk(aValidOBRisk1());
    }

    private OBWriteDataInternationalScheduled1 aValidOBWriteDataInternationalScheduled1() {
        return new OBWriteDataInternationalScheduled1()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBInternationalScheduled1());
    }
}