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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.domesticscheduledpayments;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticScheduledPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticScheduled1;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticScheduledResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduled1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledResponse1;

import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticScheduledConsentTestDataFactory.aValidOBDomesticScheduled1;

/**
 * A SpringBoot test for the {@link DomesticScheduledPaymentsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticScheduledPaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String SCHEDULED_PAYMENTS_URI = "/open-banking/v3.0/pisp/domestic-scheduled-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticScheduledPaymentSubmissionRepository scheduledPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        scheduledPaymentRepository.deleteAll();
    }

    @Test
    public void shouldCreateDomesticScheduledPayment() {
        // Given
        OBWriteDomesticScheduled1 payment = aValidOBWriteDomesticScheduled1();
        HttpEntity<OBWriteDomesticScheduled1> request = new HttpEntity<>(payment, HTTP_HEADERS);
        String url = scheduledPaymentsUrl();

        // When
        ResponseEntity<OBWriteDomesticScheduledResponse1> response = restTemplate.postForEntity(url, request, OBWriteDomesticScheduledResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDataDomesticScheduledResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/domestic-scheduled-payments/" + responseData.getDomesticScheduledPaymentId())).isTrue();
    }

    @Test
    public void shouldGetDomesticScheduledPaymentById() {
        // Given
        OBWriteDomesticScheduled1 payment = aValidOBWriteDomesticScheduled1();
        HttpEntity<OBWriteDomesticScheduled1> request = new HttpEntity<>(payment, HTTP_HEADERS);
        ResponseEntity<OBWriteDomesticScheduledResponse1> persistedPayment = restTemplate.postForEntity(scheduledPaymentsUrl(), request, OBWriteDomesticScheduledResponse1.class);
        String url = scheduledPaymentIdUrl(persistedPayment.getBody().getData().getDomesticScheduledPaymentId());

        // When
        ResponseEntity<OBWriteDomesticScheduledResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticScheduledResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDataDomesticScheduledResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/domestic-scheduled-payments/" + responseData.getDomesticScheduledPaymentId())).isTrue();
    }

    private String scheduledPaymentsUrl() {
        return BASE_URL + port + SCHEDULED_PAYMENTS_URI;
    }

    private String scheduledPaymentIdUrl(String id) {
        return scheduledPaymentsUrl() + "/" + id;
    }

    private OBWriteDomesticScheduled1 aValidOBWriteDomesticScheduled1() {
        return new OBWriteDomesticScheduled1()
                .data(aValidOBWriteDataDomesticScheduled1())
                .risk(aValidOBRisk1());
    }

    private OBWriteDataDomesticScheduled1 aValidOBWriteDataDomesticScheduled1() {
        return new OBWriteDataDomesticScheduled1()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBDomesticScheduled1());
    }
}