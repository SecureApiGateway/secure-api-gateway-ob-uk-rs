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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticstandingorders;


import static com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils.createTestDataStandingOrderConsentResponse6;
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
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBWriteDomesticStandingOrder3DataInitiation;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTimeZone;
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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticstandingorder.DomesticStandingOrderConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticStandingOrderPaymentSubmissionRepository;

import uk.org.openbanking.datamodel.v3.common.OBReadRefundAccount;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticResponse5DataRefundAccount;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrder3;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrder3Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrder3DataInitiation;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsent5Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsentResponse6;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderResponse6;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderResponse6Data;
import uk.org.openbanking.datamodel.v3.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBWritePaymentDetailsResponse1DataPaymentStatusInner;

/**
 * A SpringBoot test for the {@link DomesticStandingOrdersApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticStandingOrdersApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "client-34wrwf";
    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders(TEST_API_CLIENT_ID);
    private static final String BASE_URL = "http://localhost:";
    private static final String STANDING_ORDERS_URI = "/open-banking/v3.1.10/pisp/domestic-standing-orders";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticStandingOrderPaymentSubmissionRepository standingOrderRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    @Qualifier("v3.1.10RestDomesticStandingOrderConsentStoreClient")
    private DomesticStandingOrderConsentStoreClient consentStoreClient;

    @MockBean
    private FRAccountRepository frAccountRepository;

    private FRAccount readRefundAccount;

    private final String debtorAccountId = "acc-23542";

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
        standingOrderRepository.deleteAll();
    }

    private void mockConsentStoreGetResponse(OBWriteDomesticStandingOrder3 paymentRequest) {
        mockConsentStoreGetResponse(paymentRequest, OBReadRefundAccount.NO);
    }

    private void mockConsentStoreGetResponse(OBWriteDomesticStandingOrder3 paymentRequest, OBReadRefundAccount readRefundAccount) {
        mockConsentStoreGetResponse(paymentRequest, readRefundAccount, OBPaymentConsentStatus.AUTHORISED.toString());
    }

    private void mockConsentStoreGetResponse(OBWriteDomesticStandingOrder3 paymentRequest, OBReadRefundAccount readRefundAccount, String status) {
        final String consentId = paymentRequest.getData().getConsentId();

        // reverse engineer the consent from the paymentRequest
        final OBWriteDomesticStandingOrderConsent5 consentRequest = new OBWriteDomesticStandingOrderConsent5();
        consentRequest.setRisk(paymentRequest.getRisk());
        consentRequest.setData(FRModelMapper.map(paymentRequest.getData(), OBWriteDomesticStandingOrderConsent5Data.class));
        // Force UTC so that initiation validation passes
        final OBWriteDomesticStandingOrder3DataInitiation initiation = consentRequest.getData().getInitiation();
        initiation.setFirstPaymentDateTime(initiation.getFirstPaymentDateTime().withZone(DateTimeZone.UTC));
        initiation.setRecurringPaymentDateTime(initiation.getRecurringPaymentDateTime().withZone(DateTimeZone.UTC));
        initiation.setFinalPaymentDateTime(initiation.getFinalPaymentDateTime().withZone(DateTimeZone.UTC));

        consentRequest.getData().setReadRefundAccount(readRefundAccount);

        final DomesticStandingOrderConsent consent = new DomesticStandingOrderConsent();
        consent.setId(consentId);
        consent.setStatus(status);
        consent.setRequestObj(FRWriteDomesticStandingOrderConsentConverter.toFRWriteDomesticStandingOrderConsent(consentRequest));
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
    public void shouldCreateDomesticStandingOrderPayment_refundYes() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        mockConsentStoreGetResponse(paymentRequest, OBReadRefundAccount.YES);

        // When
        ResponseEntity<OBWriteDomesticStandingOrderResponse6> response = restTemplate.postForEntity(
                standingOrderUrl(),
                request,
                OBWriteDomesticStandingOrderResponse6.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticStandingOrderResponse6Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(paymentRequest.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(
                PaymentsUtils.toOBWriteDomesticStandingOrderConsentResponse6DataInitiation(paymentRequest.getData().getInitiation())
        );
        assertThat(responseData.getRefund()).isNotNull();
        FRAccountIdentifier frAccountIdentifier = readRefundAccount.getAccount().getFirstAccount();
        OBWriteDomesticResponse5DataRefundAccount response5DataRefundAccount = responseData.getRefund().getAccount();
        assertThat(response5DataRefundAccount.getIdentification()).isEqualTo(frAccountIdentifier.getIdentification());
        assertThat(response5DataRefundAccount.getName()).isEqualTo(frAccountIdentifier.getName());
        assertThat(response5DataRefundAccount.getSchemeName()).isEqualTo(frAccountIdentifier.getSchemeName());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/domestic-standing-orders/" + responseData.getDomesticStandingOrderId())).isTrue();

        verifyConsentConsumed(paymentRequest.getData().getConsentId());
    }

    @Test
    public void testIdempotentSubmission() {
        OBWriteDomesticStandingOrder3 payment = aValidOBWriteDomesticStandingOrder3();
        final String consentId = payment.getData().getConsentId();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(payment, HTTP_HEADERS);

        mockConsentStoreGetResponse(payment, OBReadRefundAccount.YES);

        ResponseEntity<OBWriteDomesticStandingOrderResponse6> firstSubmissionResponse = restTemplate.postForEntity(standingOrderUrl(), request, OBWriteDomesticStandingOrderResponse6.class);
        assertThat(firstSubmissionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Send the same request again (same payload + idempotencyKey)
        ResponseEntity<OBWriteDomesticStandingOrderResponse6> secondSubmissionResponse = restTemplate.postForEntity(standingOrderUrl(), request, OBWriteDomesticStandingOrderResponse6.class);
        assertThat(secondSubmissionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(secondSubmissionResponse.getBody()).isEqualTo(firstSubmissionResponse.getBody());

        verify(consentStoreClient, times(2)).getConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
        verifyConsentConsumed(consentId); // Verifies consume was only called once
    }

    @Test
    public void shouldCreateDomesticStandingOrderPayment_refundNo() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        mockConsentStoreGetResponse(paymentRequest, OBReadRefundAccount.NO);

        OBWriteDomesticStandingOrderConsentResponse6 obConsentResponse6 = createTestDataStandingOrderConsentResponse6(paymentRequest);
        obConsentResponse6.getData().readRefundAccount(OBReadRefundAccount.NO);

        // When
        ResponseEntity<OBWriteDomesticStandingOrderResponse6> response = restTemplate.postForEntity(
                standingOrderUrl(),
                request,
                OBWriteDomesticStandingOrderResponse6.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticStandingOrderResponse6Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(paymentRequest.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(
                PaymentsUtils.toOBWriteDomesticStandingOrderConsentResponse6DataInitiation(paymentRequest.getData().getInitiation())
        );
        assertThat(responseData.getRefund()).isNull();
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/domestic-standing-orders/" + responseData.getDomesticStandingOrderId())).isTrue();

        verifyConsentConsumed(paymentRequest.getData().getConsentId());
    }

    @Test
    public void shouldGetDomesticStandingOrderPaymentById() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        mockConsentStoreGetResponse(paymentRequest, OBReadRefundAccount.YES);

        ResponseEntity<OBWriteDomesticStandingOrderResponse6> paymentSubmitted = restTemplate.postForEntity(
                standingOrderUrl(),
                request,
                OBWriteDomesticStandingOrderResponse6.class
        );

        String url = standingOrderIdUrl(paymentSubmitted.getBody().getData().getDomesticStandingOrderId());

        // When
        ResponseEntity<OBWriteDomesticStandingOrderResponse6> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(HTTP_HEADERS),
                OBWriteDomesticStandingOrderResponse6.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteDomesticStandingOrderResponse6Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(paymentRequest.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(
                PaymentsUtils.toOBWriteDomesticStandingOrderConsentResponse6DataInitiation(paymentRequest.getData().getInitiation())
        );
        assertThat(responseData.getRefund()).isNotNull();
        FRAccountIdentifier frAccountIdentifier = readRefundAccount.getAccount().getFirstAccount();
        OBWriteDomesticResponse5DataRefundAccount response5DataRefundAccount = responseData.getRefund().getAccount();
        assertThat(response5DataRefundAccount.getIdentification()).isEqualTo(frAccountIdentifier.getIdentification());
        assertThat(response5DataRefundAccount.getName()).isEqualTo(frAccountIdentifier.getName());
        assertThat(response5DataRefundAccount.getSchemeName()).isEqualTo(frAccountIdentifier.getSchemeName());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(url)).isTrue();
    }

    @Test
    public void shouldGetDomesticStandingOrderPaymentDetailsById() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        mockConsentStoreGetResponse(paymentRequest);

        ResponseEntity<OBWriteDomesticStandingOrderResponse6> paymentSubmitted = restTemplate.postForEntity(
                standingOrderUrl(),
                request,
                OBWriteDomesticStandingOrderResponse6.class
        );

        // Given
        OBWriteDomesticStandingOrderResponse6 responsePayment = paymentSubmitted.getBody();
        String url = standingOrderIdDetailsUrl(paymentSubmitted.getBody().getData().getDomesticStandingOrderId());
        // When
        ResponseEntity<OBWritePaymentDetailsResponse1> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(HTTP_HEADERS),
                OBWritePaymentDetailsResponse1.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<OBWritePaymentDetailsResponse1DataPaymentStatusInner> responseData = response.getBody().getData().getPaymentStatus();
        for (OBWritePaymentDetailsResponse1DataPaymentStatusInner data : responseData) {
            assertThat(data).isNotNull();
            String submittedPaymentStatus = PaymentsUtils.statusLinkingMap.get(responsePayment.getData().getStatus().getValue());
            assertThat(data.getStatus().getValue()).isEqualTo(submittedPaymentStatus);
            assertThat(data.getStatusDetail().getStatus()).isEqualTo(submittedPaymentStatus);
        }
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(url)).isTrue();
    }

    @Test
    public void shouldThrowInvalidStandingOrder() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();

        mockConsentStoreGetResponse(paymentRequest, OBReadRefundAccount.YES);
        paymentRequest.getData().getInitiation().firstPaymentAmount(
                new OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount()
                        .amount("123123")
                        .currency("EUR")
        );
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(
                standingOrderUrl(),
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
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        mockConsentStoreGetResponse(paymentRequest, OBReadRefundAccount.NO, OBPaymentConsentStatus.CONSUMED.toString());

        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.postForEntity(standingOrderUrl(), request, OBErrorResponse1.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("An error happened when parsing the request arguments");
        assertThat(errorResponse.getBody().getErrors()).hasSize(1);
        assertThat(errorResponse.getBody().getErrors().get(0)).isEqualTo(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(OBPaymentConsentStatus.CONSUMED.toString()));

        verify(consentStoreClient).getConsent(eq(paymentRequest.getData().getConsentId()), eq(TEST_API_CLIENT_ID));
        verifyNoMoreInteractions(consentStoreClient);
    }

    private String standingOrderUrl() {
        return BASE_URL + port + STANDING_ORDERS_URI;
    }

    private String standingOrderIdUrl(String id) {
        return standingOrderUrl() + "/" + id;
    }

    private String standingOrderIdDetailsUrl(String id) {
        return standingOrderUrl() + "/" + id + "/payment-details";
    }

    private OBWriteDomesticStandingOrder3 aValidOBWriteDomesticStandingOrder3() {
        return new OBWriteDomesticStandingOrder3()
                .data(aValidOOBWriteDomesticStandingOrder3Data())
                .risk(aValidOBRisk1());
    }

    private OBWriteDomesticStandingOrder3Data aValidOOBWriteDomesticStandingOrder3Data() {
        return new OBWriteDomesticStandingOrder3Data()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBWriteDomesticStandingOrder3DataInitiation());
    }
}