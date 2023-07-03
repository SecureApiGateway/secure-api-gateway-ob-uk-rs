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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.InternationalPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
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
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.*;

import java.util.List;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.DomesticPaymentPlatformIntentTestFactory.aValidDomesticPaymentPlatformIntent;
import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.InternationalPaymentPlatformIntentTestFactory.aValidInternationalPaymentPlatformIntent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomestic2;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternational3DataInitiation;

/**
 * A SpringBoot test for the {@link InternationalPaymentsApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class InternationalPaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String INTERNATIONAL_PAYMENTS_URI = "/open-banking/v3.1.10/pisp/international-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private InternationalPaymentSubmissionRepository internationalPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FRAccountRepository frAccountRepository;

    @MockBean
    private ConsentService consentService;
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

    @Test
    public void shouldCreateInternationalPayment_refundYes() {
        // Given
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidInternationalPaymentPlatformIntent(payment.getData().getConsentId())
        );

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalConsentResponse6(payment)
        );

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
    }

    @Test
    public void shouldCreateInternationalPayment_refundNo() {
        // Given
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidInternationalPaymentPlatformIntent(payment.getData().getConsentId())
        );
        OBWriteInternationalConsentResponse6 obConsentResponse = PaymentsUtils.createTestDataInternationalConsentResponse6(payment);
        obConsentResponse.getData().readRefundAccount(OBReadRefundAccountEnum.NO);

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(obConsentResponse);

        // When
        ResponseEntity<OBWriteInternationalResponse5> response = restTemplate.postForEntity(paymentsUrl(), request, OBWriteInternationalResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteInternationalResponse5Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(responseData.getRefund()).isNull();
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/international-payments/" + responseData.getInternationalPaymentId())).isTrue();
    }

    @Test
    public void shouldGetInternationalPaymentById() {
        // Given
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidInternationalPaymentPlatformIntent(payment.getData().getConsentId())
        );

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalConsentResponse6(payment)
        );

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
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetInternationalPaymentDetailsById() {
        // Given
        OBWriteInternational3 payment = aValidOBWriteInternational3();
        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidInternationalPaymentPlatformIntent(payment.getData().getConsentId())
        );

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalConsentResponse6(payment)
        );

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

    @Test
    public void shouldThrowInvalidInternationalPayment() {
        // Given
        OBWriteInternational3 paymentInitiation = aValidOBWriteInternational3();

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidInternationalPaymentPlatformIntent(paymentInitiation.getData().getConsentId())
        );

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalConsentResponse6(paymentInitiation)
        );

        OBWriteInternational3 paymentSubmission = aValidOBWriteInternational3();
        paymentSubmission.getData().getInitiation().instructedAmount(
                new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("123123")
                        .currency("EUR")
        );

        HttpEntity<OBWriteInternational3> request = new HttpEntity<>(paymentSubmission, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(paymentsUrl(), request, OBErrorResponse1.class);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        OBError1 error = response.getBody().getErrors().get(0);
        assertThat(error.getErrorCode()).isEqualTo(OBRIErrorType.PAYMENT_INVALID_INITIATION.getCode().getValue());
        assertThat(error.getMessage()).contains(
                String.format(
                        OBRIErrorType.PAYMENT_INVALID_INITIATION.getMessage(),
                        "The initiation field from payment submitted does not match with the initiation field submitted for the consent"
                )
        );
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