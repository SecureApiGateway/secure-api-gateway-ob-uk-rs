/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticpayments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.v3.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2;
import static uk.org.openbanking.testsupport.v3.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domestic.DomesticPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticPaymentSubmissionRepository;

import uk.org.openbanking.datamodel.v3.common.OBReadRefundAccount;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticResponse5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticResponse5Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticResponse5DataRefundAccount;
import uk.org.openbanking.datamodel.v3.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBWritePaymentDetailsResponse1DataPaymentStatusInner;

/**
 * A SpringBoot test for the DomesticPaymentsApiController.<br/>
 * Coverage versions v3.1.5 to v3.1.10
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticPaymentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "test_client_1234567890";
    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders(TEST_API_CLIENT_ID);
    private static final String BASE_URL = "http://localhost:";
    private static final String DOMESTIC_PAYMENTS_URI = "/open-banking/v3.1.10/pisp/domestic-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticPaymentSubmissionRepository domesticPaymentRepository;

    @MockBean
    private FRAccountRepository frAccountRepository;

    @MockBean
    @Qualifier("v3.1.10RestDomesticPaymentConsentStoreClient")
    private DomesticPaymentConsentStoreClient domesticPaymentConsentStoreClient;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String debtorAccountId = "debtor-acc-123";

    private FRAccount readRefundAccount;
    @BeforeEach
    void setup() {
        readRefundAccount = FRAccount.builder()
                .account(
                        FRFinancialAccount.builder().accounts(
                                List.of(
                                        FRAccountIdentifier.builder()
                                                .identification("08080021325698")
                                                .name("ACME Inc")
                                                .schemeName("UK.OBIE.SortCodeAccountNumber")
                                                .secondaryIdentification("0002")
                                                .build()
                                )
                        ).build()
                )
                .build();

        given(frAccountRepository.byAccountId(eq(debtorAccountId))).willReturn(readRefundAccount);
    }

    @AfterEach
    void removeData() {
        domesticPaymentRepository.deleteAll();
    }

    private void mockConsentStoreGetResponse(String consentId) {
        mockConsentStoreGetResponse(consentId, aValidOBWriteDomesticConsent4());
    }

    private void mockConsentStoreGetResponse(String consentId, OBWriteDomesticConsent4 consentRequest) {
        mockConsentStoreGetResponse(consentId, consentRequest, OBPaymentConsentStatus.AUTHORISED.toString());
    }

    private void mockConsentStoreGetResponseWithRefundAccount(String consentId) {
        final OBWriteDomesticConsent4 consent = aValidOBWriteDomesticConsent4();
        consent.getData().readRefundAccount(OBReadRefundAccount.YES);
        mockConsentStoreGetResponse(consentId, consent, OBPaymentConsentStatus.AUTHORISED.toString());
    }

    private void mockConsentStoreGetResponse(String consentId, OBWriteDomesticConsent4 consentRequest, String status) {
        final DomesticPaymentConsent consent = new DomesticPaymentConsent();
        consent.setId(consentId);
        consent.setStatus(status);
        consent.setRequestObj(FRWriteDomesticConsentConverter.toFRWriteDomesticConsent(consentRequest));
        consent.setAuthorisedDebtorAccountId(debtorAccountId);
        when(domesticPaymentConsentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(consent);
    }

    @Test
    public void shouldCreateDomesticPayment_refundYes() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponseWithRefundAccount(consentId);

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(paymentSubmitted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticResponse5Data responseData = paymentSubmitted.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNotNull();
        FRAccountIdentifier frAccountIdentifier = readRefundAccount.getAccount().getFirstAccount();
        OBWriteDomesticResponse5DataRefundAccount response5DataRefundAccount = responseData.getRefund().getAccount();
        assertThat(response5DataRefundAccount.getIdentification()).isEqualTo(frAccountIdentifier.getIdentification());
        assertThat(response5DataRefundAccount.getName()).isEqualTo(frAccountIdentifier.getName());
        assertThat(response5DataRefundAccount.getSchemeName()).isEqualTo(frAccountIdentifier.getSchemeName());
        assertThat(paymentSubmitted.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();

        verify(domesticPaymentConsentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));

        verifyConsentConsumed(consentId);
    }

    @Test
    public void testIdempotentSubmission() {
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponseWithRefundAccount(consentId);

        ResponseEntity<OBWriteDomesticResponse5> firstSubmissionResponse = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);
        assertThat(firstSubmissionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Send the same request again (same payload + idempotencyKey)
        ResponseEntity<OBWriteDomesticResponse5> secondSubmissionResponse = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);
        assertThat(secondSubmissionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(secondSubmissionResponse.getBody()).isEqualTo(firstSubmissionResponse.getBody());

        verify(domesticPaymentConsentStoreClient, times(2)).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyConsentConsumed(consentId); // Verifies consume was only called once
    }

    @Test
    public void testIdempotentSubmissionConcurrent() throws InterruptedException, ExecutionException, TimeoutException {
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponseWithRefundAccount(consentId);

        int numConcurrentRequests = 16;
        final ExecutorService executorService = Executors.newFixedThreadPool(numConcurrentRequests);
        final List<Callable<ResponseEntity<OBWriteDomesticResponse5>>> tasks = Collections.nCopies(numConcurrentRequests,
                () -> restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class));

        final List<Future<ResponseEntity<OBWriteDomesticResponse5>>> futures = executorService.invokeAll(tasks);
        final List<ResponseEntity<OBWriteDomesticResponse5>> responses = new ArrayList<>();
        for (final Future<ResponseEntity<OBWriteDomesticResponse5>> future : futures) {
            responses.add(future.get(2, TimeUnit.SECONDS));
        }

        final Set<OBWriteDomesticResponse5> paymentResponses = new HashSet<>();
        for (ResponseEntity<OBWriteDomesticResponse5> response : responses) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            paymentResponses.add(response.getBody());
        }
        assertThat(paymentResponses.size()).isEqualTo(1);
    }

    private void verifyConsentConsumed(String consentId) {
        // Verify that consumeConsent was called
        final ArgumentCaptor<ConsumePaymentConsentRequest> consumeReqCaptor = ArgumentCaptor.forClass(ConsumePaymentConsentRequest.class);
        verify(domesticPaymentConsentStoreClient).consumeConsent(consumeReqCaptor.capture());
        final ConsumePaymentConsentRequest consumeConsentReq = consumeReqCaptor.getValue();
        assertThat(consumeConsentReq.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
        assertThat(consumeConsentReq.getConsentId()).isEqualTo(consentId);
    }

    @Test
    public void shouldCreateDomesticPayment_refundNo() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        final OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        mockConsentStoreGetResponse(payment.getData().getConsentId(), consentRequest);

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);

        // Then
        assertThat(paymentSubmitted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticResponse5Data responseData = paymentSubmitted.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNull();
        assertThat(paymentSubmitted.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();

        verifyConsentConsumed(payment.getData().getConsentId());
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
        mockConsentStoreGetResponse(consentId, aValidOBWriteDomesticConsent4(), OBPaymentConsentStatus.CONSUMED.toString());

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(paymentsUrl(), request, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(OBPaymentConsentStatus.CONSUMED.toString()));

        verify(domesticPaymentConsentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(domesticPaymentConsentStoreClient);
    }

    @Test
    public void shouldGetDomesticPaymentById() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        final String consentId = payment.getData().getConsentId();
        mockConsentStoreGetResponseWithRefundAccount(consentId);

        ResponseEntity<OBWriteDomesticResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse5.class);

        String url = paymentIdUrl(paymentSubmitted.getBody().getData().getDomesticPaymentId());

        // When
        ResponseEntity<OBWriteDomesticResponse5> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteDomesticResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDomesticResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(consentId);
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNotNull();
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
        List<OBWritePaymentDetailsResponse1DataPaymentStatusInner> responseData = response.getBody().getData().getPaymentStatus();
        for (OBWritePaymentDetailsResponse1DataPaymentStatusInner data : responseData) {
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