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
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_6.domesticpayments.DomesticPaymentsApiController;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.conent.store.client.v3_1_10.DomesticPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.ConsumeDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4;

/**
 * A SpringBoot test for the {@link DomesticPaymentsApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticPaymentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "test_client_1234567890";
    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeadersWithApiClientId(TEST_API_CLIENT_ID);
    private static final String BASE_URL = "http://localhost:";
    private static final String DOMESTIC_PAYMENTS_URI = "/open-banking/v3.1.5/pisp/domestic-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticPaymentSubmissionRepository domesticPaymentRepository;

    @MockBean
    private DomesticPaymentConsentStoreClient domesticPaymentConsentStoreClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        domesticPaymentRepository.deleteAll();
    }

    private void mockConsentStoreGetResponse(String consentId) {
        mockConsentStoreGetResponse(consentId, aValidOBWriteDomesticConsent4());
    }

    private void mockConsentStoreGetResponse(String consentId, OBWriteDomesticConsent4 consentRequest) {
        mockConsentStoreGetResponse(consentId, consentRequest, StatusEnum.AUTHORISED.toString());
    }

    private void mockConsentStoreGetResponse(String consentId, OBWriteDomesticConsent4 consentRequest, String status) {
        final DomesticPaymentConsent consent = new DomesticPaymentConsent();
        consent.setId(consentId);
        consent.setStatus(status);
        consent.setRequestObj(consentRequest);
        when(domesticPaymentConsentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(consent);
    }

    @Test
    public void shouldCreateDomesticPayment() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponse(consentId);

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(paymentSubmitted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticResponse5Data responseData = paymentSubmitted.getBody().getData();

        assertThat(responseData.getConsentId()).isEqualTo(consentId);
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(paymentSubmitted.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();

        verify(domesticPaymentConsentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));

        // Verify that consumeConsent was called
        final ArgumentCaptor<ConsumeDomesticPaymentConsentRequest> consumeReqCaptor = ArgumentCaptor.forClass(ConsumeDomesticPaymentConsentRequest.class);
        verify(domesticPaymentConsentStoreClient).consumeConsent(consumeReqCaptor.capture());
        final ConsumeDomesticPaymentConsentRequest consumeConsentReq = consumeReqCaptor.getValue();
        assertThat(consumeConsentReq.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
        assertThat(consumeConsentReq.getConsentId()).isEqualTo(consentId);
    }

    @Test
    public void failsToCreateDomesticPaymentIfInitiationChanged() {
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        final OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        consentRequest.getData().getInitiation().getInstructedAmount().setAmount("100000.00"); // Consent InstructedAmount different to Payment InstructedAmount
        mockConsentStoreGetResponse(consentId, consentRequest);

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(paymentsUrl(), request, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.PAYMENT_INVALID_INITIATION.toOBError1("The Initiation field in the request does not match with the consent"));

        verify(domesticPaymentConsentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(domesticPaymentConsentStoreClient);
    }

    @Test
    public void failsToCreateDomesticPaymentIfStatusNotAuthorised() {
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        // Consent in Store has Consumed Status (Payment already created)
        mockConsentStoreGetResponse(consentId, aValidOBWriteDomesticConsent4(), StatusEnum.CONSUMED.toString());

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(paymentsUrl(), request, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(StatusEnum.CONSUMED.toString()));

        verify(domesticPaymentConsentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(domesticPaymentConsentStoreClient);
    }

    @Test
    public void shouldGetDomesticPaymentById() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        final String consentId = payment.getData().getConsentId();
        mockConsentStoreGetResponse(consentId);

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);

        String url = paymentIdUrl(paymentSubmitted.getBody().getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWriteDomesticResponse5> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDomesticResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(consentId);
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();
    }

    @Test
    public void shouldGetDomesticPaymentDetailsById() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        final String consentId = payment.getData().getConsentId();
        mockConsentStoreGetResponse(consentId);

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
        // Mock consent store response for this payment
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        final String consentId = payment.getData().getConsentId();
        mockConsentStoreGetResponse(consentId);

        // Create a payment instruction which changes initiation data
        OBWriteDomestic2 paymentSubmission = aValidOBWriteDomestic2();
        paymentSubmission.getData().setConsentId(consentId);
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
