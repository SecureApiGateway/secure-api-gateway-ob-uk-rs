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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_5.domesticpayments;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_6.domesticpayments.DomesticPaymentsApiController;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.*;

import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.DomesticPaymentPlatformIntentTestFactory.aValidDomesticPaymentPlatformIntent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2;

/**
 * A SpringBoot test for the {@link DomesticPaymentsApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticPaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String DOMESTIC_PAYMENTS_URI = "/open-banking/v3.1.5/pisp/domestic-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticPaymentSubmissionRepository domesticPaymentRepository;

    @MockBean
    private ConsentService consentService;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        domesticPaymentRepository.deleteAll();
    }

    @Test
    public void shouldCreateDomesticPayment() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticPaymentPlatformIntent(payment.getData().getConsentId()));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataConsent4(payment)
        );

        given(consentService.getOBIntentObject(any(), anyString(), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse5(payment)
        );

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(paymentSubmitted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticResponse5Data responseData = paymentSubmitted.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(paymentSubmitted.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetDomesticPaymentById() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticPaymentPlatformIntent(payment.getData().getConsentId()));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataConsent4(payment)
        );

        given(consentService.getOBIntentObject(any(), anyString(), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse5(payment)
        );

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);

        String url = paymentIdUrl(paymentSubmitted.getBody().getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWriteDomesticResponse5> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDomesticResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetDomesticPaymentDetailsById() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticPaymentPlatformIntent(payment.getData().getConsentId()));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataConsent4(payment)
        );

        given(consentService.getOBIntentObject(any(), anyString(), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse5(payment)
        );

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);


        // Given
        OBWriteDomesticResponse5 responsePayment = paymentSubmitted.getBody();
        String url = paymentIdDetailsUrl(paymentSubmitted.getBody().getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWritePaymentDetailsResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWritePaymentDetailsResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<OBWritePaymentDetailsResponse1DataPaymentStatus> responseData = response.getBody().getData().getPaymentStatus();
        for (OBWritePaymentDetailsResponse1DataPaymentStatus data : responseData) {
            assertThat(data).isNotNull();
            assertThat(data.getStatus().getValue()).isEqualTo(responsePayment.getData().getStatus().getValue());
            assertThat(data.getStatusDetail().getLocalInstrument()).isEqualTo(responsePayment.getData().getInitiation().getLocalInstrument());
            assertThat(data.getStatusDetail().getStatus()).isEqualTo(responsePayment.getData().getStatus().getValue());
        }
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(url)).isTrue();
    }

    @Test
    public void shouldThrowInvalidPayment() {
        // Given
        OBWriteDomestic2 paymentInitiation = aValidOBWriteDomestic2();

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticPaymentPlatformIntent(paymentInitiation.getData().getConsentId()));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataConsent4(paymentInitiation)
        );

        given(consentService.getOBIntentObject(any(), anyString(), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse5(paymentInitiation)
        );

        OBWriteDomestic2 paymentSubmission = aValidOBWriteDomestic2();
        paymentSubmission.getData().getInitiation().instructedAmount(
                new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("123123")
                        .currency("EUR")
        );

        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(paymentSubmission, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(paymentsUrl(), request, OBErrorResponse1.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        OBError1 error = response.getBody().getErrors().get(0);
        assertThat(error.getErrorCode()).isEqualTo(OBRIErrorType.PAYMENT_INVALID_INITIATION.getCode().getValue());
        assertThat(error.getMessage()).contains(
                String.format(
                        OBRIErrorType.PAYMENT_INVALID_INITIATION.getMessage(),
                        "The Initiation field in the request does not match with the consent"
                )
        );
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
