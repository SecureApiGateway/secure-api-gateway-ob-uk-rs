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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRBalanceType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCashBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCreditDebitIndicator;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRBalance;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.BalanceStoreService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.vrp.OBPAFundsAvailableResult1;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequest;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequestData;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationResponse;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class DomesticVrpConsentsApiControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private BalanceStoreService balanceStoreService;

    private String getFundsConfirmationUrl(String consentId) {
        return "http://localhost:" + port + "/open-banking/v3.1.10/pisp/domestic-vrp-consents/" + consentId + "/funds-confirmation";
    }

    @Test
    public void shouldGetFundsAvailable() {
        // Given
        final String consentId = UUID.randomUUID().toString();
        final BigDecimal instructedAmount = new BigDecimal("50.00").setScale(2);
        // TODO build complete request obj
        final OBVRPFundsConfirmationRequest obvrpFundsConfirmationRequest = new OBVRPFundsConfirmationRequest()
                .data(new OBVRPFundsConfirmationRequestData()
                        .consentId(consentId)
                        .reference("reference")
                        .instructedAmount(
                                new OBActiveOrHistoricCurrencyAndAmount()
                                        .amount(instructedAmount.toPlainString())
                                        .currency("GBP")
                        )
                );

        // Given
        FRBalance balance = aValidBalance(instructedAmount.multiply(new BigDecimal("5.00").setScale(2)));
        given(balanceStoreService.getBalance(balance.getAccountId(), balance.getBalance().getType())).willReturn(Optional.of(balance));

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticVrpPaymentConsentDetails(obvrpFundsConfirmationRequest.getData().getConsentId())
        );

        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(
                obvrpFundsConfirmationRequest, HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders()
        );

        // When
        ResponseEntity<OBVRPFundsConfirmationResponse> response = restTemplate.postForEntity(
                getFundsConfirmationUrl(consentId),
                request,
                OBVRPFundsConfirmationResponse.class
        );

        // Then
        OBVRPFundsConfirmationResponse body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.getData().getConsentId()).isEqualTo(obvrpFundsConfirmationRequest.getData().getConsentId());
        assertThat(body.getData().getReference()).isEqualTo(obvrpFundsConfirmationRequest.getData().getReference());
        assertThat(body.getData().getInstructedAmount()).isEqualTo(obvrpFundsConfirmationRequest.getData().getInstructedAmount());
        assertThat(body.getData().getFundsAvailableResult().getFundsAvailable()).isEqualTo(OBPAFundsAvailableResult1.FundsAvailableEnum.AVAILABLE);
    }

    @Test
    public void shouldGetFundsNotAvailable() {
        // Given
        final String consentId = UUID.randomUUID().toString();
        final BigDecimal instructedAmount = new BigDecimal("50.00").setScale(2);
        final OBVRPFundsConfirmationRequest obvrpFundsConfirmationRequest = new OBVRPFundsConfirmationRequest()
                .data(new OBVRPFundsConfirmationRequestData()
                        .consentId(consentId)
                        .reference("reference")
                        .instructedAmount(
                                new OBActiveOrHistoricCurrencyAndAmount()
                                        .amount(instructedAmount.toPlainString())
                                        .currency("GBP")
                        )
                );

        // Given
        FRBalance balance = aValidBalance(new BigDecimal("49.99").setScale(2));
        given(balanceStoreService.getBalance(balance.getAccountId(), balance.getBalance().getType())).willReturn(Optional.of(balance));

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticVrpPaymentConsentDetails(obvrpFundsConfirmationRequest.getData().getConsentId())
        );

        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(
                obvrpFundsConfirmationRequest, HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders()
        );

        // When
        ResponseEntity<OBVRPFundsConfirmationResponse> response = restTemplate.postForEntity(
                getFundsConfirmationUrl(consentId),
                request,
                OBVRPFundsConfirmationResponse.class
        );

        // Then
        OBVRPFundsConfirmationResponse body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body.getData().getConsentId()).isEqualTo(obvrpFundsConfirmationRequest.getData().getConsentId());
        assertThat(body.getData().getReference()).isEqualTo(obvrpFundsConfirmationRequest.getData().getReference());
        assertThat(body.getData().getInstructedAmount()).isEqualTo(obvrpFundsConfirmationRequest.getData().getInstructedAmount());
        assertThat(body.getData().getFundsAvailableResult().getFundsAvailable()).isEqualTo(OBPAFundsAvailableResult1.FundsAvailableEnum.NOTAVAILABLE);
    }

    @Test
    public void shouldRaiseBadRequestMandatoryData() {
        // Given
        final String consentId = UUID.randomUUID().toString();
        final OBVRPFundsConfirmationRequest obvrpFundsConfirmationRequest = new OBVRPFundsConfirmationRequest()
                .data(new OBVRPFundsConfirmationRequestData()
                        .consentId(consentId)
                        .reference("reference")
                        .instructedAmount(null)
                );

        // Given
        FRBalance balance = aValidBalance(new BigDecimal("49.99").setScale(2));
        given(balanceStoreService.getBalance(balance.getAccountId(), balance.getBalance().getType())).willReturn(Optional.of(balance));

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticVrpPaymentConsentDetails(obvrpFundsConfirmationRequest.getData().getConsentId())
        );

        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(
                obvrpFundsConfirmationRequest, HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders()
        );

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(
                getFundsConfirmationUrl(consentId),
                request,
                OBErrorResponse1.class
        );

        // Then
        OBErrorResponse1 body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body.getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(body.getErrors().get(0).getMessage()).contains("Mandatory data not provided.");
    }

    @Test
    public void shouldRaiseBadRequestConsentIdMismatch() {
        // Given
        final String consentId = UUID.randomUUID().toString();
        final BigDecimal instructedAmount = new BigDecimal("50.00").setScale(2);
        final OBVRPFundsConfirmationRequest obvrpFundsConfirmationRequest = new OBVRPFundsConfirmationRequest()
                .data(new OBVRPFundsConfirmationRequestData()
                        .consentId(consentId)
                        .reference("reference")
                        .instructedAmount(new OBActiveOrHistoricCurrencyAndAmount()
                                .amount(instructedAmount.toPlainString())
                                .currency("GBP"))
                );

        // Given
        FRBalance balance = aValidBalance(new BigDecimal("49.99").setScale(2));
        given(balanceStoreService.getBalance(balance.getAccountId(), balance.getBalance().getType())).willReturn(Optional.of(balance));

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(
                aValidDomesticVrpPaymentConsentDetails(obvrpFundsConfirmationRequest.getData().getConsentId())
        );

        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(
                obvrpFundsConfirmationRequest, HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders()
        );

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(
                getFundsConfirmationUrl(UUID.randomUUID().toString()),
                request,
                OBErrorResponse1.class
        );

        // Then
        OBErrorResponse1 body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body.getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(body.getErrors().get(0).getMessage()).contains(
                "The consentId provided in the body doesn't match with the consent id provided as parameter"
        );
    }

    private FRBalance aValidBalance(BigDecimal amountBalance) {
        FRCashBalance cashBalance2 = new FRCashBalance();
        cashBalance2.setAccountId(DomesticVrpPaymentConsentDetailsTestFactory.DEFAULT_ACCOUNT_ID);
        FRAmount frAmount = new FRAmount();
        frAmount.setAmount(amountBalance.toPlainString());
        frAmount.setCurrency("GBP");
        cashBalance2.setAmount(frAmount);
        cashBalance2.setCreditDebitIndicator(FRCreditDebitIndicator.CREDIT);
        cashBalance2.setDateTime(DateTime.now());
        cashBalance2.setType(FRBalanceType.INTERIMAVAILABLE);
        String accountId = cashBalance2.getAccountId();
        return FRBalance.builder()
                .accountId(accountId)
                .balance(cashBalance2)
                .build();
    }
}