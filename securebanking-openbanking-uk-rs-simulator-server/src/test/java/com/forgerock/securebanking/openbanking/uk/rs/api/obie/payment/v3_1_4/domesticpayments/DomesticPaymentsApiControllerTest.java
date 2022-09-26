/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_4.domesticpayments;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticPaymentSubmissionRepository;
import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.rs.platform.client.services.PlatformClientService;
import com.forgerock.securebanking.rs.platform.client.test.support.DomesticPaymentConsentDetailsTestFactory;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.payment.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2;

/**
 * A SpringBoot test for {@link DomesticPaymentsApiController} <br/>
 * Coverage versions: v3.1.4
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticPaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String DOMESTIC_PAYMENTS_URI = "/open-banking/v3.1.4/pisp/domestic-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticPaymentSubmissionRepository domesticPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private PlatformClientService platformClientService;

    private OBWriteDomestic2 payment;

    private ResponseEntity<OBWriteDomesticResponse4> paymentSubmitted;

    @BeforeEach
    void setup() throws ExceptionClient {
        // Given
        payment = aValidOBWriteDomestic2();
        JsonObject intentResponse = DomesticPaymentConsentDetailsTestFactory.aValidOBDomesticPaymentConsentDetails(
                IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId(),
                UUID.randomUUID().toString()
        );
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(platformClientService.getIntent(anyString(), anyString())).willReturn(intentResponse);

        // When
        paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse4.class);
    }

    @AfterEach
    void removeData() {
        domesticPaymentRepository.deleteAll();
    }

    @Test
    public void shouldCreateDomesticPayment() {
        // Then
        assertThat(paymentSubmitted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticResponse4Data responseData = Objects.requireNonNull(paymentSubmitted.getBody()).getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(paymentSubmitted.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetDomesticPaymentById() {
        // Given
        String url = paymentIdUrl(Objects.requireNonNull(paymentSubmitted.getBody()).getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWriteDomesticResponse4> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticResponse4.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDomesticResponse4Data responseData = Objects.requireNonNull(response.getBody()).getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetDomesticPaymentDetailsById() {
        // Given
        String url = paymentIdDetailsUrl(Objects.requireNonNull(paymentSubmitted.getBody()).getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWritePaymentDetailsResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWritePaymentDetailsResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDomesticResponse4 persistedPayment = paymentSubmitted.getBody();
        List<OBWritePaymentDetailsResponse1DataPaymentStatus> responseData = Objects.requireNonNull(response.getBody()).getData().getPaymentStatus();
        for (OBWritePaymentDetailsResponse1DataPaymentStatus data : responseData) {
            assertThat(data).isNotNull();
            assertThat(persistedPayment).isNotNull();
            assertThat(data.getStatus().getValue()).isEqualTo(persistedPayment.getData().getStatus().getValue());
            assertThat(data.getStatusDetail().getLocalInstrument()).isEqualTo(persistedPayment.getData().getInitiation().getLocalInstrument());
            assertThat(data.getStatusDetail().getStatus()).isEqualTo(persistedPayment.getData().getStatus().getValue());
        }
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(url)).isTrue();
    }

    private String paymentsUrl() {
        return BASE_URL + port + DOMESTIC_PAYMENTS_URI;
    }

    private String paymentIdUrl(String id) {
        return paymentsUrl() + "/" + id;
    }

    private String paymentIdDetailsUrl(String id) {
        return paymentsUrl() + "/" + id + "/payment-details";
    }
}
