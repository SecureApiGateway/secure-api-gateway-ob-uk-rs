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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.internationalstandingorders;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalStandingOrderPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalStandingOrder1;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalStandingOrderResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderResponse1;

import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalStandingOrderConsentTestDataFactory.aValidOBInternationalStandingOrder1;

/**
 * A SpringBoot test for the {@link InternationalStandingOrdersApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class InternationalStandingOrdersApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String STANDING_ORDERS_URI = "/open-banking/v3.0/pisp/international-standing-orders";

    @LocalServerPort
    private int port;

    @Autowired
    private InternationalStandingOrderPaymentSubmissionRepository standingOrderRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        standingOrderRepository.deleteAll();
    }

    @Test
    public void shouldCreateInternationalStandingOrder() {
        // Given
        OBWriteInternationalStandingOrder1 standingOrder = aValidOBWriteInternationalStandingOrder1();
        HttpEntity<OBWriteInternationalStandingOrder1> request = new HttpEntity<>(standingOrder, HTTP_HEADERS);
        String url = standingOrderUrl();

        // When
        ResponseEntity<OBWriteInternationalStandingOrderResponse1> response = restTemplate.postForEntity(url, request, OBWriteInternationalStandingOrderResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDataInternationalStandingOrderResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(standingOrder.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(standingOrder.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/international-standing-orders/" + responseData.getInternationalStandingOrderId())).isTrue();
    }

    @Test
    public void shouldGetInternationalStandingOrderById() {
        // Given
        OBWriteInternationalStandingOrder1 standingOrder = aValidOBWriteInternationalStandingOrder1();
        HttpEntity<OBWriteInternationalStandingOrder1> request = new HttpEntity<>(standingOrder, HTTP_HEADERS);
        ResponseEntity<OBWriteInternationalStandingOrderResponse1> persistedPayment = restTemplate.postForEntity(standingOrderUrl(), request, OBWriteInternationalStandingOrderResponse1.class);
        String url = standingOrderIdUrl(persistedPayment.getBody().getData().getInternationalStandingOrderId());

        // When
        ResponseEntity<OBWriteInternationalStandingOrderResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteInternationalStandingOrderResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDataInternationalStandingOrderResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(standingOrder.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(standingOrder.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/international-standing-orders/" + responseData.getInternationalStandingOrderId())).isTrue();
    }

    private String standingOrderUrl() {
        return BASE_URL + port + STANDING_ORDERS_URI;
    }

    private String standingOrderIdUrl(String id) {
        return standingOrderUrl() + "/" + id;
    }

    private OBWriteInternationalStandingOrder1 aValidOBWriteInternationalStandingOrder1() {
        return new OBWriteInternationalStandingOrder1()
                .data(aValidOBWriteDataInternationalStandingOrder1())
                .risk(aValidOBRisk1());
    }

    private OBWriteDataInternationalStandingOrder1 aValidOBWriteDataInternationalStandingOrder1() {
        return new OBWriteDataInternationalStandingOrder1()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBInternationalStandingOrder1());
    }
}