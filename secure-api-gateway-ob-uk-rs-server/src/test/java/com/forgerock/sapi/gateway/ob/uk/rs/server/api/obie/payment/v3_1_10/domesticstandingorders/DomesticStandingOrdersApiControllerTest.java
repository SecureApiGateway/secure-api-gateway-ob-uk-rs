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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticstandingorders;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticStandingOrderPaymentSubmissionRepository;
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

import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.DomesticStandingOrdersPlatformIntentTestFactory.aValidDomesticStandingOrdersPlatformIntent;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils.createTestDataStandingOrderConsentResponse5;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils.createTestDataStandingOrderConsentResponse6;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBWriteDomesticStandingOrder3DataInitiation;

/**
 * A SpringBoot test for the {@link DomesticStandingOrdersApiController}.<br/>
 * Coverage versions v3.1.5 to v3.1.10.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticStandingOrdersApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String STANDING_ORDERS_URI = "/open-banking/v3.1.10/pisp/domestic-standing-orders";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticStandingOrderPaymentSubmissionRepository standingOrderRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private FRAccountRepository frAccountRepository;

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
    public void shouldCreateDomesticStandingOrderPayment_refundYes() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticStandingOrdersPlatformIntent(paymentRequest.getData().getConsentId())
        );

        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse5.class), any(JsonObject.class), anyString())).willReturn(
                createTestDataStandingOrderConsentResponse5(paymentRequest)
        );
        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                createTestDataStandingOrderConsentResponse6(paymentRequest)
        );

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
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldCreateDomesticStandingOrderPayment_refundNo() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticStandingOrdersPlatformIntent(paymentRequest.getData().getConsentId())
        );
        OBWriteDomesticStandingOrderConsentResponse5 obConsentResponse5 = createTestDataStandingOrderConsentResponse5(paymentRequest);
        obConsentResponse5.getData().readRefundAccount(OBReadRefundAccountEnum.NO);
        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse5.class), any(JsonObject.class), anyString())).willReturn(
                obConsentResponse5
        );

        OBWriteDomesticStandingOrderConsentResponse6 obConsentResponse6 = createTestDataStandingOrderConsentResponse6(paymentRequest);
        obConsentResponse6.getData().readRefundAccount(OBReadRefundAccountEnum.NO);
        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                obConsentResponse6
        );

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
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetDomesticStandingOrderPaymentById() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticStandingOrdersPlatformIntent(paymentRequest.getData().getConsentId())
        );

        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse5.class), any(JsonObject.class), anyString())).willReturn(
                createTestDataStandingOrderConsentResponse5(paymentRequest)
        );
        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                createTestDataStandingOrderConsentResponse6(paymentRequest)
        );

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
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetDomesticStandingOrderPaymentDetailsById() {
        // Given
        OBWriteDomesticStandingOrder3 paymentRequest = aValidOBWriteDomesticStandingOrder3();
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentRequest, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticStandingOrdersPlatformIntent(paymentRequest.getData().getConsentId())
        );

        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse5.class), any(JsonObject.class), anyString())).willReturn(
                createTestDataStandingOrderConsentResponse5(paymentRequest)
        );
        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                createTestDataStandingOrderConsentResponse6(paymentRequest)
        );

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
        List<OBWritePaymentDetailsResponse1DataPaymentStatus> responseData = response.getBody().getData().getPaymentStatus();
        for (OBWritePaymentDetailsResponse1DataPaymentStatus data : responseData) {
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

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticStandingOrdersPlatformIntent(paymentRequest.getData().getConsentId())
        );

        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse5.class), any(JsonObject.class), anyString())).willReturn(
                createTestDataStandingOrderConsentResponse5(paymentRequest)
        );
        given(consentService.deserialize(eq(OBWriteDomesticStandingOrderConsentResponse6.class), any(JsonObject.class), anyString())).willReturn(
                createTestDataStandingOrderConsentResponse6(paymentRequest)
        );
        OBWriteDomesticStandingOrder3 paymentSubmission = aValidOBWriteDomesticStandingOrder3();
        paymentSubmission.getData().getInitiation().firstPaymentAmount(
                new OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount()
                        .amount("123123")
                        .currency("EUR")
        );
        HttpEntity<OBWriteDomesticStandingOrder3> request = new HttpEntity<>(paymentSubmission, HTTP_HEADERS);

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
                        "The initiation field from payment submitted does not match with the initiation field submitted for the consent"
                )
        );
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