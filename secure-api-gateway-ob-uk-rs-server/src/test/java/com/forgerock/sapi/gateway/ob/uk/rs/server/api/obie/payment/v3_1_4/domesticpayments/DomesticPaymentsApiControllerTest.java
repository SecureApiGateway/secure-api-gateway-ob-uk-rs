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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_4.domesticpayments;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticPaymentSubmissionRepository;
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
import uk.org.openbanking.datamodel.payment.*;

import java.util.List;
import java.util.Objects;

import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.DomesticPaymentPlatformIntentTestFactory.aValidDomesticPaymentPlatformIntent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.*;

/**
 * A SpringBoot test for {@link DomesticPaymentsApiController} <br/>
 * Coverage versions: v3.1.4
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticPaymentsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String DOMESTIC_PAYMENTS_URI = "/open-banking/v3.1.4/pisp/domestic-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticPaymentSubmissionRepository domesticPaymentRepository;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private FRAccountRepository frAccountRepository;

    @Autowired
    private TestRestTemplate restTemplate;

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
        domesticPaymentRepository.deleteAll();
    }

    @Test
    public void shouldCreateDomesticPayment() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticPaymentPlatformIntent(payment.getData().getConsentId()));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse4(payment)
        );

        given(consentService.getOBIntentObject(any(), anyString(), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse4(payment)
        );

        // When
        ResponseEntity<OBWriteDomesticResponse4> response = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse4.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBWriteDomesticResponse4Data responseData = Objects.requireNonNull(response.getBody()).getData();
        assertThat(responseData.getConsentId()).isEqualTo(payment.getData().getConsentId());
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(payment.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().toString().endsWith("/domestic-payments/" + responseData.getDomesticPaymentId())).isTrue();
        assertThat(responseData.getCharges()).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldGetDomesticPaymentById() {
        // Given
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticPaymentPlatformIntent(payment.getData().getConsentId()));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse4(payment)
        );

        given(consentService.getOBIntentObject(any(), anyString(), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse4(payment)
        );

        // When
        ResponseEntity<OBWriteDomesticResponse4> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse4.class);

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
        OBWriteDomestic2 payment = aValidOBWriteDomestic2();
        HttpEntity<OBWriteDomestic2> request = new HttpEntity<>(payment, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticPaymentPlatformIntent(payment.getData().getConsentId()));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse4(payment)
        );

        given(consentService.getOBIntentObject(any(), anyString(), anyString())).willReturn(
                PaymentsUtils.createTestDataConsentResponse4(payment)
        );

        // When
        ResponseEntity<OBWriteDomesticResponse4> paymentSubmitted = restTemplate.postForEntity(paymentsUrl(), request, OBWriteDomesticResponse4.class);

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
