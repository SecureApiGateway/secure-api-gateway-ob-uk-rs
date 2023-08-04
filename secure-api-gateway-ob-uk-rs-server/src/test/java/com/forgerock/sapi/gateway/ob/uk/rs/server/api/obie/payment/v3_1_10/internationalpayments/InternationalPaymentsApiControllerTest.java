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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.internationalpayments;

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
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternational3DataInitiation;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5;

import java.util.List;
import java.util.UUID;

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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRExchangeRateConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.international.v3_1_10.InternationalPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.InternationalPaymentSubmissionRepository;

import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.OBReadRefundAccountEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticResponse5DataRefundAccount;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3Data;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsentResponse6;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalResponse5Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1DataPaymentStatus;

/**
 * A SpringBoot test for the {@link InternationalPaymentsApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class InternationalPaymentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "test_client_1234567890";
    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID);
    private static final String BASE_URL = "http://localhost:";
    private static final String INTERNATIONAL_PAYMENTS_URI = "/open-banking/v3.1.10/pisp/international-payments";

    private final String debtorAccountId = "debtor-acc-123";
    @LocalServerPort
    private int port;

    @Autowired
    private InternationalPaymentSubmissionRepository internationalPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FRAccountRepository frAccountRepository;

    @MockBean
    private InternationalPaymentConsentStoreClient consentStoreClient;

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

        given(frAccountRepository.byAccountId(anyString())).willReturn(readRefundAccount);
    }

    @AfterEach
    void removeData() {
        internationalPaymentRepository.deleteAll();
    }

    private void mockConsentStoreGetResponse(String consentId) {
        mockConsentStoreGetResponse(consentId, aValidOBWriteInternationalConsent5());
    }

    private void mockConsentStoreGetResponse(String consentId, OBWriteInternationalConsent5 consentRequest) {
        mockConsentStoreGetResponse(consentId, consentRequest, StatusEnum.AUTHORISED.toString());
    }

    private void mockConsentStoreGetResponseWithRefundAccount(String consentId) {
        final OBWriteInternationalConsent5 consent = aValidOBWriteInternationalConsent5();
        consent.getData().readRefundAccount(OBReadRefundAccountEnum.YES);
        mockConsentStoreGetResponse(consentId, consent, StatusEnum.AUTHORISED.toString());
    }

    private void mockConsentStoreGetResponse(String consentId, OBWriteInternationalConsent5 consentRequest, String status) {
        final InternationalPaymentConsent consent = new InternationalPaymentConsent();
        consent.setId(consentId);
        consent.setStatus(status);
        consent.setRequestObj(FRWriteInternationalConsentConverter.toFRWriteInternationalConsent(consentRequest));
        consent.setAuthorisedDebtorAccountId(debtorAccountId);
        consent.setExchangeRateInformation(FRExchangeRateConverter.toFRExchangeRateInformation(consentRequest.getData().getInitiation().getExchangeRateInformation()));
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
    public void shouldCreateInternationalPayment_refundYes() {
        // Given
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponseWithRefundAccount(consentId);

        // When
        ResponseEntity<OBWriteInternationalResponse5> response = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteInternationalResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNotNull();
        FRAccountIdentifier frAccountIdentifier = readRefundAccount.getAccount().getFirstAccount();
        OBWriteDomesticResponse5DataRefundAccount response5DataRefundAccount = responseData.getRefund().getAccount();
        assertThat(response5DataRefundAccount.getIdentification()).isEqualTo(frAccountIdentifier.getIdentification());
        assertThat(response5DataRefundAccount.getName()).isEqualTo(frAccountIdentifier.getName());
        assertThat(response5DataRefundAccount.getSchemeName()).isEqualTo(frAccountIdentifier.getSchemeName());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/international-payments/" + responseData.getInternationalPaymentId())).isTrue();

        verify(consentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyConsentConsumed(consentId);
    }

    @Test
    public void testIdempotentSubmission() {
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponseWithRefundAccount(consentId);

        ResponseEntity<OBWriteInternationalResponse5> firstSubmissionResponse = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse5.class);
        assertThat(firstSubmissionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Send the same request again (same payload + idempotencyKey)
        ResponseEntity<OBWriteInternationalResponse5> secondSubmissionResponse = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse5.class);
        assertThat(secondSubmissionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(secondSubmissionResponse.getBody()).isEqualTo(firstSubmissionResponse.getBody());

        verify(consentStoreClient, times(2)).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyConsentConsumed(consentId); // Verifies consume was only called once
    }

    @Test
    public void shouldCreateInternationalPayment_refundNo() {
        // Given
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponse(payment.getData().getConsentId());
        OBWriteInternationalConsentResponse6 obConsentResponse = PaymentsUtils.createTestDataInternationalConsentResponse6(payment);
        obConsentResponse.getData().readRefundAccount(OBReadRefundAccountEnum.NO);

        // When
        ResponseEntity<OBWriteInternationalResponse5> response = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteInternationalResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(consentId);
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNull();
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/international-payments/" + responseData.getInternationalPaymentId())).isTrue();

        verify(consentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyConsentConsumed(consentId);
    }

    @Test
    public void failsToCreatePaymentIfInitiationChanged() {
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);

        final OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().getInstructedAmount().setAmount("100000.00"); // Consent InstructedAmount different to Payment InstructedAmount
        mockConsentStoreGetResponse(consentId, consentRequest);

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(paymentsUrl(), request, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.PAYMENT_INVALID_INITIATION.toOBError1("The Initiation field in the request does not match with the consent"));

        verify(consentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(consentStoreClient);
    }

    @Test
    public void failsToCreateDomesticPaymentIfStatusNotAuthorised() {
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);

        // Consent in Store has Consumed Status (Payment already created)
        mockConsentStoreGetResponse(consentId, aValidOBWriteInternationalConsent5(), StatusEnum.CONSUMED.toString());

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(paymentsUrl(), request, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(StatusEnum.CONSUMED.toString()));

        verify(consentStoreClient).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(consentStoreClient);
    }

    @Test
    public void shouldGetInternationalPaymentById() {
        // Given
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);

        final String consentId = payment.getData().getConsentId();
        mockConsentStoreGetResponseWithRefundAccount(consentId);

        // When
        ResponseEntity<OBWriteInternationalResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse5.class);

        String url = paymentIdUrl(paymentSubmitted.getBody().getData().getInternationalPaymentId());

        // When
        ResponseEntity<OBWriteInternationalResponse5> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteInternationalResponse5.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteInternationalResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNotNull();
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/international-payments/" + responseData.getInternationalPaymentId())).isTrue();
        assertThat(responseData.getCharges()).isNull();
    }

    @Test
    public void shouldGetInternationalPaymentDetailsById() {
        // Given
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);

        final String consentId = payment.getData().getConsentId();
        mockConsentStoreGetResponse(consentId);

        // When
        ResponseEntity<OBWriteInternationalResponse5> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse5.class);

        // Given
        OBWriteInternationalResponse5 responsePayment = paymentSubmitted.getBody();
        String url = paymentIdDetailsUrl(paymentSubmitted.getBody().getData().getInternationalPaymentId());

        // When
        ResponseEntity<OBWritePaymentDetailsResponse1> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWritePaymentDetailsResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<OBWritePaymentDetailsResponse1DataPaymentStatus> responseData = response.getBody().getData().getPaymentStatus();
        for (OBWritePaymentDetailsResponse1DataPaymentStatus data : responseData) {
            assertThat(data).isNotNull();
            assertThat(data.getStatus().getValue()).isEqualTo(responsePayment.getData().getStatus().getValue());
            assertThat(data.getPaymentTransactionId()).isNotNull().isNotEmpty().isNotBlank();
            assertThat(data.getStatusDetail().getStatus()).isEqualTo(responsePayment.getData().getStatus().getValue());
        }
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(url)).isTrue();
    }



    private String paymentsUrl() {
        return BASE_URL + port + INTERNATIONAL_PAYMENTS_URI;
    }

    private String paymentIdUrl(String id) {
        return paymentsUrl() + "/" + id;
    }
    private String paymentIdDetailsUrl(String id) {
        return paymentsUrl() + "/" + id + "/payment-details";
    }

    private OBWriteInternational3 aValidOBWriteInternational3() {
        return new OBWriteInternational3()
                .data(aValidOBWriteInternational3Data())
                .risk(aValidOBRisk1());
    }

    private OBWriteInternational3Data aValidOBWriteInternational3Data() {
        return new OBWriteInternational3Data()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBWriteInternational3DataInitiation());
    }
}