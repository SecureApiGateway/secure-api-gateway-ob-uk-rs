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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.internationalstandingorders;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.InternationalStandingOrderPaymentSubmissionRepository;
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

import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.InternationalStandingOrdersPlatformIntentTestFactory.aValidStandingOrdersPlatformIntent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalStandingOrderConsentTestDataFactory.aValidOBWriteInternationalStandingOrder4DataInitiation;

/**
 * A SpringBoot test for the {@link InternationalStandingOrdersApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class InternationalStandingOrdersApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String INTERNATIONAL_PAYMENTS_URI = "/open-banking/v3.1.10/pisp/international-standing-orders";

    @LocalServerPort
    private int port;

    @Autowired
    private InternationalStandingOrderPaymentSubmissionRepository standingOrderRepository;

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
        standingOrderRepository.deleteAll();
    }

    @Test
    public void shouldCreateInternationalStandingOrder_refundYes() {
        // Given
        OBWriteInternationalStandingOrder4 payment = aValidOBWriteInternationalStandingOrder4();
        HttpEntity<OBWriteInternationalStandingOrder4> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidStandingOrdersPlatformIntent(payment.getData().getConsentId())
        );

        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse6(payment)
        );

        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse7.class), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse7(payment)
        );

        // When
        ResponseEntity<OBWriteInternationalStandingOrderResponse7> response = restTemplate.postForEntity(
                paymentsUrl(), request, OBWriteInternationalStandingOrderResponse7.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteInternationalStandingOrderResponse7Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(
                PaymentsUtils.toOBWriteInternationalStandingOrderConsentResponse7DataInitiation(payment.getData().getInitiation())
        );
        assertThat(responseData.getRefund()).isNotNull();
        FRAccountIdentifier frAccountIdentifier = readRefundAccount.getAccount().getFirstAccount();
        OBWriteDomesticResponse5DataRefundAccount response5DataRefundAccount = responseData.getRefund().getAccount();
        assertThat(response5DataRefundAccount.getIdentification()).isEqualTo(frAccountIdentifier.getIdentification());
        assertThat(response5DataRefundAccount.getName()).isEqualTo(frAccountIdentifier.getName());
        assertThat(response5DataRefundAccount.getSchemeName()).isEqualTo(frAccountIdentifier.getSchemeName());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(
                "/international-standing-orders/" + responseData.getInternationalStandingOrderId())
        ).isTrue();
    }

    @Test
    public void shouldCreateInternationalStandingOrder_refundNo() {
        // Given
        OBWriteInternationalStandingOrder4 payment = aValidOBWriteInternationalStandingOrder4();
        HttpEntity<OBWriteInternationalStandingOrder4> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidStandingOrdersPlatformIntent(payment.getData().getConsentId())
        );
        OBWriteInternationalStandingOrderConsentResponse6 obConsentResponse6 = PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse6(payment);
        obConsentResponse6.getData().readRefundAccount(OBReadRefundAccountEnum.NO);
        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                obConsentResponse6
        );
        OBWriteInternationalStandingOrderConsentResponse7 obConsentResponse7 = PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse7(payment);
        obConsentResponse7.getData().readRefundAccount(OBReadRefundAccountEnum.NO);
        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse7.class), any(JsonObject.class), anyString())).willReturn(
                obConsentResponse7
        );

        // When
        ResponseEntity<OBWriteInternationalStandingOrderResponse7> response = restTemplate.postForEntity(
                paymentsUrl(), request, OBWriteInternationalStandingOrderResponse7.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteInternationalStandingOrderResponse7Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(
                PaymentsUtils.toOBWriteInternationalStandingOrderConsentResponse7DataInitiation(payment.getData().getInitiation())
        );
        assertThat(responseData.getRefund()).isNull();
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(
                "/international-standing-orders/" + responseData.getInternationalStandingOrderId())
        ).isTrue();
    }

    @Test
    public void shouldGetInternationalStandingOrderById() {
        // Given
        OBWriteInternationalStandingOrder4 payment = aValidOBWriteInternationalStandingOrder4();
        HttpEntity<OBWriteInternationalStandingOrder4> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidStandingOrdersPlatformIntent(payment.getData().getConsentId())
        );

        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse6(payment)
        );

        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse7.class), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse7(payment)
        );

        // When
        ResponseEntity<OBWriteInternationalStandingOrderResponse7> paymentSubmitted = restTemplate.postForEntity(
                paymentsUrl(), request, OBWriteInternationalStandingOrderResponse7.class
        );

        String url = paymentIdUrl(paymentSubmitted.getBody().getData().getInternationalStandingOrderId());

        // When
        ResponseEntity<OBWriteInternationalStandingOrderResponse7> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWriteInternationalStandingOrderResponse7.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBWriteInternationalStandingOrderResponse7Data responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(
                PaymentsUtils.toOBWriteInternationalStandingOrderConsentResponse7DataInitiation(payment.getData().getInitiation())
        );
        assertThat(responseData.getRefund()).isNotNull();
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(
                "/international-standing-orders/" + responseData.getInternationalStandingOrderId())
        ).isTrue();
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetInternationalStandingOrderDetailsById() {
        // Given
        OBWriteInternationalStandingOrder4 payment = aValidOBWriteInternationalStandingOrder4();
        HttpEntity<OBWriteInternationalStandingOrder4> request = new HttpEntity<>(payment, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidStandingOrdersPlatformIntent(payment.getData().getConsentId())
        );

        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse6(payment)
        );

        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse7.class), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse7(payment)
        );

        // When
        ResponseEntity<OBWriteInternationalStandingOrderResponse7> paymentSubmitted = restTemplate.postForEntity(
                paymentsUrl(), request, OBWriteInternationalStandingOrderResponse7.class
        );

        // Given
        OBWriteInternationalStandingOrderResponse7 responsePayment = paymentSubmitted.getBody();
        String url = paymentIdDetailsUrl(paymentSubmitted.getBody().getData().getInternationalStandingOrderId());

        // When
        ResponseEntity<OBWritePaymentDetailsResponse1> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBWritePaymentDetailsResponse1.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<OBWritePaymentDetailsResponse1DataPaymentStatus> responseData = response.getBody().getData().getPaymentStatus();
        for (OBWritePaymentDetailsResponse1DataPaymentStatus data : responseData) {
            assertThat(data).isNotNull();
            String submittedPaymentStatus = PaymentsUtils.statusLinkingMap.get(responsePayment.getData().getStatus().getValue());
            assertThat(data.getStatus().getValue()).isEqualTo(submittedPaymentStatus);
            assertThat(data.getPaymentTransactionId()).isNotNull().isNotEmpty().isNotBlank();
            assertThat(data.getStatusDetail().getStatus()).isEqualTo(submittedPaymentStatus);
        }
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith(url)).isTrue();
    }

    @Test
    public void shouldThrowInvalidInternationalStandingOrder() {
        // Given
        OBWriteInternationalStandingOrder4 payment = aValidOBWriteInternationalStandingOrder4();
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidStandingOrdersPlatformIntent(payment.getData().getConsentId())
        );

        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse6(payment)
        );

        given(consentService.deserialize(eq(OBWriteInternationalStandingOrderConsentResponse7.class), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataInternationalStandingOrderConsentResponse7(payment)
        );

        OBWriteInternationalStandingOrder4 paymentSubmission = aValidOBWriteInternationalStandingOrder4();
        paymentSubmission.getData().getInitiation().instructedAmount(
                new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("123123")
                        .currency("EUR")
        );

        HttpEntity<OBWriteInternationalStandingOrder4> request = new HttpEntity<>(paymentSubmission, HTTP_HEADERS);

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

    private OBWriteInternationalStandingOrder4 aValidOBWriteInternationalStandingOrder4() {
        return new OBWriteInternationalStandingOrder4()
                .data(aValidOBWriteInternationalStandingOrder4Data())
                .risk(aValidOBRisk1());
    }

    private OBWriteInternationalStandingOrder4Data aValidOBWriteInternationalStandingOrder4Data() {
        return new OBWriteInternationalStandingOrder4Data()
                .consentId(UUID.randomUUID().toString())
                .initiation(aValidOBWriteInternationalStandingOrder4DataInitiation());
    }
}