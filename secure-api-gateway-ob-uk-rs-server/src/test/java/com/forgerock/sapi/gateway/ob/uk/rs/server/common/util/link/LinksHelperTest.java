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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link;

import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticPaymentSubmissionRepository;
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

import java.net.URI;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBDomestic1;

/**
 * Spring Boot Test for {@link LinksHelper}.
 *
 * <p>
 * Due to the complexity of mocking an HTTP request and all of the Spring Boot context, this class tests the
 * {@link LinksHelper} indirectly via one of the Payment API controllers. This is probably the most reliable way to
 * ensure the "self" links are returned correctly.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class LinksHelperTest {
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
    public void shouldGetSelfLinkWithDefaultDomain() {
        // Given
        OBWriteDomestic1 payment = aValidOBWriteDomestic1();
        HttpHeaders headers = requiredPaymentHttpHeaders();
        HttpEntity<OBWriteDomestic1> request = new HttpEntity<>(payment, headers);
        ResponseEntity<OBWriteDomesticResponse1> persistedPayment = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse1.class);
        String url = paymentIdUrl(persistedPayment.getBody().getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWriteDomesticResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), OBWriteDomesticResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDataDomesticResponse1 responseData = response.getBody().getData();
        URI expectedUrl = URI.create("http://localhost:" + port + "/open-banking/v3.0/pisp/domestic-payments/" + responseData.getDomesticPaymentId());
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(expectedUrl);
    }

    @Test
    public void shouldGetSelfLinkWithProvidedDomain() {
        // Given
        OBWriteDomestic1 payment = aValidOBWriteDomestic1();
        HttpHeaders headers = requiredPaymentHttpHeaders();
        headers.add("X-Forwarded-Host", "forgerock.com");
        headers.add("X-Forwarded-Proto", "https");
        HttpEntity<OBWriteDomestic1> request = new HttpEntity<>(payment, headers);
        ResponseEntity<OBWriteDomesticResponse1> persistedPayment = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse1.class);
        String url = paymentIdUrl(persistedPayment.getBody().getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWriteDomesticResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), OBWriteDomesticResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDataDomesticResponse1 responseData = response.getBody().getData();
        URI expectedUrl = URI.create("https://forgerock.com/open-banking/v3.0/pisp/domestic-payments/" + responseData.getDomesticPaymentId());
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(expectedUrl);
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