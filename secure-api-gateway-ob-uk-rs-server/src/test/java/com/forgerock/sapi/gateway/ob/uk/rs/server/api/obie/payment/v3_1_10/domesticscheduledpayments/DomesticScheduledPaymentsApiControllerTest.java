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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticscheduledpayments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticScheduledConsentTestDataFactory.aValidOBWriteDomesticScheduled2DataInitiation;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticScheduledPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.OBReadRefundAccountEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticResponse5DataRefundAccount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduled2;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduled2Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledResponse5Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1DataPaymentStatus;

/**
 * A SpringBoot test for the {@link DomesticScheduledPaymentsApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticScheduledPaymentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "test_client_1234567890";
    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID);
    private static final String BASE_URL = "http://localhost:";
    private static final String SCHEDULED_PAYMENTS_URI = "/open-banking/v3.1.10/pisp/domestic-scheduled-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticScheduledPaymentSubmissionRepository scheduledPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private DomesticScheduledPaymentConsentStoreClient consentStoreClient;

    @MockBean
    private FRAccountRepository frAccountRepository;

    private FRAccount readRefundAccount;

    private final String debtorAccountId = "debtor-acc-123";

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

        given(frAccountRepository.byAccountId(anyString())).willReturn(readRefundAccount);
    }

    @AfterEach
    void removeData() {
        scheduledPaymentRepository.deleteAll();
    }


    private void mockConsentStoreGetResponse(OBWriteDomesticScheduled2 paymentRequest) {
        mockConsentStoreGetResponse(paymentRequest, OBReadRefundAccountEnum.NO);
    }

    private void mockConsentStoreGetResponse(OBWriteDomesticScheduled2 paymentRequest, OBReadRefundAccountEnum readRefundAccount) {
        mockConsentStoreGetResponse(paymentRequest, readRefundAccount, StatusEnum.AUTHORISED.toString());
    }
    private void mockConsentStoreGetResponse(OBWriteDomesticScheduled2 paymentRequest, OBReadRefundAccountEnum readRefundAccount, String status) {
        final String consentId = paymentRequest.getData().getConsentId();

        // reverse engineer the consent from the paymentRequest
        final OBWriteDomesticScheduledConsent4 consentRequest = new OBWriteDomesticScheduledConsent4();
        consentRequest.setRisk(paymentRequest.getRisk());
        consentRequest.setData(FRModelMapper.map(paymentRequest.getData(), OBWriteDomesticScheduledConsent4Data.class));
        consentRequest.getData().getInitiation().setRequestedExecutionDateTime(consentRequest.getData().getInitiation().getRequestedExecutionDateTime().withZone(DateTimeZone.UTC)); // Force UTC so that initiation validation passes
        consentRequest.getData().setReadRefundAccount(readRefundAccount);

        final DomesticScheduledPaymentConsent consent = new DomesticScheduledPaymentConsent();
        consent.setId(consentId);
        consent.setStatus(status);
        consent.setRequestObj(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledConsent(consentRequest));
        consent.setAuthorisedDebtorAccountId(debtorAccountId);
        when(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(consent);
    }

    private void verifyConsentConsumed(String consentId) {
        // Verify that consumeConsent was called
        final ArgumentCaptor<ConsumePaymentConsentRequest> consumeReqCaptor = ArgumentCaptor.forClass(ConsumePaymentConsentRequest.class);
        verify(consentStoreClient).consumeConsent(consumeReqCaptor.capture());
        final ConsumePaymentConsentRequest consumeConsentReq = consumeReqCaptor.getValue();
        assertThat(consumeConsentReq.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
        assertThat(consumeConsentReq.getConsentId()).isEqualTo(consentId);
    }


    @Test
    public void shouldCreateDomesticScheduledPayment_refundYes() {
        // Given
        OBWriteDomesticScheduled2 payment = aValidOBWriteDomesticScheduled2();
        HttpEntity<OBWriteDomesticScheduled2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponse(payment, OBReadRefundAccountEnum.YES);

        // When
        ResponseEntity<OBWriteDomesticScheduledResponse5> response = restTemplate.postForEntity(
                scheduledPaymentsUrl(),
                request,
                OBWriteDomesticScheduledResponse5.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticScheduledResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNotNull();
        FRAccountIdentifier frAccountIdentifier = readRefundAccount.getAccount().getFirstAccount();
        OBWriteDomesticResponse5DataRefundAccount response5DataRefundAccount = responseData.getRefund().getAccount();
        assertThat(response5DataRefundAccount.getIdentification()).isEqualTo(frAccountIdentifier.getIdentification());
        assertThat(response5DataRefundAccount.getName()).isEqualTo(frAccountIdentifier.getName());
        assertThat(response5DataRefundAccount.getSchemeName()).isEqualTo(frAccountIdentifier.getSchemeName());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/domestic-scheduled-payments/" + responseData.getDomesticScheduledPaymentId())).isTrue();

        verifyConsentConsumed(payment.getData().getConsentId());
    }

    @Test
    public void testIdempotentSubmission() {
        OBWriteDomesticScheduled2 payment = aValidOBWriteDomesticScheduled2();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomesticScheduled2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponse(payment, OBReadRefundAccountEnum.YES);

        ResponseEntity<OBWriteDomesticScheduledResponse5> firstSubmissionResponse = restTemplate.postForEntity(scheduledPaymentsUrl(), request, OBWriteDomesticScheduledResponse5.class);
        assertThat(firstSubmissionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Send the same request again (same payload + idempotencyKey)
        ResponseEntity<OBWriteDomesticScheduledResponse5> secondSubmissionResponse = restTemplate.postForEntity(scheduledPaymentsUrl(), request, OBWriteDomesticScheduledResponse5.class);
        assertThat(secondSubmissionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(secondSubmissionResponse.getBody()).isEqualTo(firstSubmissionResponse.getBody());

        verify(consentStoreClient, times(2)).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyConsentConsumed(consentId); // Verifies consume was only called once
    }

    @Test
    public void shouldCreateDomesticScheduledPayment_refundNo() {
        // Given
        OBWriteDomesticScheduled2 payment = aValidOBWriteDomesticScheduled2();
        HttpEntity<OBWriteDomesticScheduled2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponse(payment, OBReadRefundAccountEnum.NO);

        // When
        ResponseEntity<OBWriteDomesticScheduledResponse5> response = restTemplate.postForEntity(
                scheduledPaymentsUrl(),
                request,
                OBWriteDomesticScheduledResponse5.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticScheduledResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNull();
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/domestic-scheduled-payments/" + responseData.getDomesticScheduledPaymentId())).isTrue();

        verifyConsentConsumed(payment.getData().getConsentId());
    }

    @Test
    public void shouldGetDomesticScheduledPaymentById() {
        // Given
        OBWriteDomesticScheduled2 payment = aValidOBWriteDomesticScheduled2();
        HttpEntity<OBWriteDomesticScheduled2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponse(payment, OBReadRefundAccountEnum.YES);

        ResponseEntity<OBWriteDomesticScheduledResponse5> paymentSubmitted = restTemplate.postForEntity(
                scheduledPaymentsUrl(),
                request,
                OBWriteDomesticScheduledResponse5.class
        );

        String url = scheduledPaymentIdUrl(paymentSubmitted.getBody().getData().getDomesticScheduledPaymentId());

        // When
        ResponseEntity<OBWriteDomesticScheduledResponse5> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(HTTP_HEADERS),
                OBWriteDomesticScheduledResponse5.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDomesticScheduledResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNotNull();
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(url)).isTrue();
    }

    @Test
    public void shouldGetDomesticScheduledPaymentDetailsById() {
        // Given
        OBWriteDomesticScheduled2 payment = aValidOBWriteDomesticScheduled2();
        HttpEntity<OBWriteDomesticScheduled2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponse(payment);

        ResponseEntity<OBWriteDomesticScheduledResponse5> paymentSubmitted = restTemplate.postForEntity(
                scheduledPaymentsUrl(),
                request,
                OBWriteDomesticScheduledResponse5.class
        );

        // Given
        OBWriteDomesticScheduledResponse5 responsePayment = paymentSubmitted.getBody();
        String url = scheduledPaymentIdDetailsUrl(paymentSubmitted.getBody().getData().getDomesticScheduledPaymentId());
        // When
        ResponseEntity<OBWritePaymentDetailsResponse1> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(HTTP_HEADERS),
                OBWritePaymentDetailsResponse1.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<OBWritePaymentDetailsResponse1DataPaymentStatus> responseData = response.getBody().getData().getPaymentStatus();
        for (OBWritePaymentDetailsResponse1DataPaymentStatus data : responseData) {
            assertThat(data).isNotNull();
            String submittedPaymentStatus = PaymentsUtils.statusLinkingMap.get(responsePayment.getData().getStatus().getValue());
            assertThat(data.getStatus().getValue()).isEqualTo(submittedPaymentStatus);
            assertThat(data.getStatusDetail().getLocalInstrument()).isEqualTo(responsePayment.getData().getInitiation().getLocalInstrument());
            assertThat(data.getStatusDetail().getStatus()).isEqualTo(submittedPaymentStatus);
        }
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(url)).isTrue();
    }

    @Test
    public void shouldThrowInvalidScheduledPayment() {
        // Given
        OBWriteDomesticScheduled2 paymentSubmission = aValidOBWriteDomesticScheduled2();
        mockConsentStoreGetResponse(paymentSubmission);
        paymentSubmission.getData().getInitiation().instructedAmount(
                new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("123123")
                        .currency("EUR")
        );

        HttpEntity<OBWriteDomesticScheduled2> request = new HttpEntity<>(paymentSubmission, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(
                scheduledPaymentsUrl(),
                request,
                OBErrorResponse1.class
        );
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

    @Test
    public void failsToCreatePaymentIfStatusNotAuthorised() {
        OBWriteDomesticScheduled2 payment = aValidOBWriteDomesticScheduled2();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomesticScheduled2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        // Consent in Store has Consumed Status (Payment already created)
        mockConsentStoreGetResponse(payment, OBReadRefundAccountEnum.NO, StatusEnum.CONSUMED.toString());

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(scheduledPaymentsUrl(), request, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(StatusEnum.CONSUMED.toString()));

        verify(consentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(consentStoreClient);
    }

    private String scheduledPaymentsUrl() {
        return BASE_URL + port + SCHEDULED_PAYMENTS_URI;
    }

    private String scheduledPaymentIdUrl(String id) {
        return scheduledPaymentsUrl() + "/" + id;
    }

    private String scheduledPaymentIdDetailsUrl(String id) {
        return scheduledPaymentsUrl() + "/" + id + "/payment-details";
    }

    private OBWriteDomesticScheduled2 aValidOBWriteDomesticScheduled2() {
        return new OBWriteDomesticScheduled2()
                .data(aValidOBWriteDomesticScheduled2Data())
                .risk(aValidOBRisk1());
    }

    private OBWriteDomesticScheduled2Data aValidOBWriteDomesticScheduled2Data() {
        return new OBWriteDomesticScheduled2Data()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBWriteDomesticScheduled2DataInitiation());
    }
}