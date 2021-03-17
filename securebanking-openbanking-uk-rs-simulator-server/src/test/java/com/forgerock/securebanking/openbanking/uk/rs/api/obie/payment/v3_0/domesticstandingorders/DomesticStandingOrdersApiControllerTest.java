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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.domesticstandingorders;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticStandingOrderPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticStandingOrder1;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticStandingOrderResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrder1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderResponse1;

import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBDomesticStandingOrder1;

/**
 * A SpringBoot test for the {@link DomesticStandingOrdersApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticStandingOrdersApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String STANDING_ORDERS_URI = "/open-banking/v3.0/pisp/domestic-standing-orders";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticStandingOrderPaymentSubmissionRepository standingOrderRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        standingOrderRepository.deleteAll();
    }

    @Test
    public void shouldCreateDomesticStandingOrder() {
        // Given
        OBWriteDomesticStandingOrder1 standingOrder = aValidOBWriteDomesticStandingOrder1();
        HttpEntity<OBWriteDomesticStandingOrder1> request = new HttpEntity<>(standingOrder, HTTP_HEADERS);
        String url = standingOrderUrl();

        // When
        ResponseEntity<OBWriteDomesticStandingOrderResponse1> response = restTemplate.postForEntity(url, request, OBWriteDomesticStandingOrderResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDataDomesticStandingOrderResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(standingOrder.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(standingOrder.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/domestic-standing-orders/" + responseData.getDomesticStandingOrderId())).isTrue();
    }

    @Test
    public void shouldGetDomesticStandingOrderById() {
        // Given
        OBWriteDomesticStandingOrder1 standingOrder = aValidOBWriteDomesticStandingOrder1();
        HttpEntity<OBWriteDomesticStandingOrder1> request = new HttpEntity<>(standingOrder, HTTP_HEADERS);
        ResponseEntity<OBWriteDomesticStandingOrderResponse1> persistedPayment = restTemplate.postForEntity(standingOrderUrl(), request, OBWriteDomesticStandingOrderResponse1.class);
        String url = standingOrderIdUrl(persistedPayment.getBody().getData().getDomesticStandingOrderId());

        // When
        ResponseEntity<OBWriteDomesticStandingOrderResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticStandingOrderResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDataDomesticStandingOrderResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(standingOrder.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(standingOrder.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().endsWith("/domestic-standing-orders/" + responseData.getDomesticStandingOrderId())).isTrue();
    }

    private String standingOrderUrl() {
        return BASE_URL + port + STANDING_ORDERS_URI;
    }

    private String standingOrderIdUrl(String id) {
        return standingOrderUrl() + "/" + id;
    }

    private OBWriteDomesticStandingOrder1 aValidOBWriteDomesticStandingOrder1() {
        return new OBWriteDomesticStandingOrder1()
                .data(aValidOBWriteDataDomesticStandingOrder1())
                .risk(aValidOBRisk1());
    }

    private OBWriteDataDomesticStandingOrder1 aValidOBWriteDataDomesticStandingOrder1() {
        return new OBWriteDataDomesticStandingOrder1()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBDomesticStandingOrder1());
    }
}