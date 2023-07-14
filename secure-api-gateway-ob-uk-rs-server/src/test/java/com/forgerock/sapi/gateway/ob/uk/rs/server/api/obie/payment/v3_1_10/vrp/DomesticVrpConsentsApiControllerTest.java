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

import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_CONSENT_NOT_FOUND;
import static com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode.OBRI_PERMISSION_INVALID;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.FundsConfirmationTestHelpers.validateConsentNotAuthorisedErrorResponse;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentsHttpHeadersWithApiClientId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRBalanceType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCashBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCreditDebitIndicator;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.BalanceStoreService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException;
import com.forgerock.sapi.gateway.rcs.consent.store.client.ConsentStoreClientException.ErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.vrp.v3_1_10.DomesticVRPConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.CreateDomesticVRPConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponse;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponseData.StatusEnum;
import uk.org.openbanking.datamodel.vrp.OBPAFundsAvailableResult1;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequest;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequestData;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationResponse;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;

/**
 * A SpringBoot test for the {@link DomesticVrpConsentsApiController} <br/>
 * Coverage versions v3.1.9 to v3.1.10.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticVrpConsentsApiControllerTest {

    private static final String TEST_API_CLIENT_ID = "client-id-325w25";

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @MockBean
    private DomesticVRPConsentStoreClient consentStoreClient;

    @MockBean
    private BalanceStoreService balanceStoreService;

    private String createConsentUri() {
        return "http://localhost:" + port + "/open-banking/v3.1.10/pisp/domestic-vrp-consents";
    }

    private String getConsentUri(String consentId) {
        return createConsentUri() + "/" + consentId;
    }

    private String getFundsConfirmationUrl(String consentId) {
        return getConsentUri(consentId) + "/funds-confirmation";
    }

    private static OBDomesticVRPConsentRequest createValidateConsentRequest() {
        final OBDomesticVRPConsentRequest consentRequest = OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest();
        final DateTime now = DateTime.now(DateTimeZone.UTC);
        consentRequest.getData().getControlParameters().setValidFromDateTime(now);
        consentRequest.getData().getControlParameters().setValidToDateTime(now.plusDays(60));
        return consentRequest;
    }

    public static DomesticVRPConsent buildAwaitingAuthorisationConsent(OBDomesticVRPConsentRequest consentRequest) {
        final DomesticVRPConsent consent = new DomesticVRPConsent();
        consent.setId(IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId());
        consent.setRequestObj(FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(consentRequest));
        consent.setStatus(StatusEnum.AWAITINGAUTHORISATION.toString());
        consent.setCharges(List.of());
        final DateTime creationDateTime = DateTime.now();
        consent.setCreationDateTime(creationDateTime);
        consent.setStatusUpdateDateTime(creationDateTime);
        return consent;
    }

    private static DomesticVRPConsent buildAuthorisedConsent(OBDomesticVRPConsentRequest consentRequest, String debtorAccountId) {
        final DomesticVRPConsent domesticPaymentConsent = buildAwaitingAuthorisationConsent(consentRequest);
        domesticPaymentConsent.setStatus(StatusEnum.AUTHORISED.toString());
        domesticPaymentConsent.setAuthorisedDebtorAccountId(debtorAccountId);
        return domesticPaymentConsent;
    }

    @Test
    public void createConsent() {
        final OBDomesticVRPConsentRequest consentRequest = createValidateConsentRequest();
        final DomesticVRPConsent consentStoreResponse = buildAwaitingAuthorisationConsent(consentRequest);

        final HttpHeaders httpHeaders = requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID);
        when(consentStoreClient.createConsent(any())).thenAnswer(invocation -> {
            final CreateDomesticVRPConsentRequest createConsentArg = invocation.getArgument(0, CreateDomesticVRPConsentRequest.class);
            assertThat(createConsentArg.getApiClientId()).isEqualTo(TEST_API_CLIENT_ID);
            assertThat(createConsentArg.getConsentRequest()).isEqualTo(FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(consentRequest));
            assertThat(createConsentArg.getCharges()).isNull();
            assertThat(createConsentArg.getIdempotencyKey()).isEqualTo(httpHeaders.getFirst("x-idempotency-key"));

            return consentStoreResponse;
        });

        final HttpEntity<OBDomesticVRPConsentRequest> entity = new HttpEntity<>(consentRequest, httpHeaders);

        final ResponseEntity<OBDomesticVRPConsentResponse> createResponse = restTemplate.exchange(createConsentUri(), HttpMethod.POST,
                entity, OBDomesticVRPConsentResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        final OBDomesticVRPConsentResponse consentResponse = createResponse.getBody();
        final String consentId = consentResponse.getData().getConsentId();
        assertThat(consentId).isEqualTo(consentStoreResponse.getId());
        assertThat(consentResponse.getData().getStatus()).isEqualTo(StatusEnum.AWAITINGAUTHORISATION);
        assertThat(consentResponse.getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(consentResponse.getData().getReadRefundAccount()).isEqualTo(consentRequest.getData().getReadRefundAccount());
        assertThat(consentResponse.getData().getCreationDateTime()).isNotNull();
        assertThat(consentResponse.getData().getStatusUpdateDateTime()).isNotNull();
        assertThat(consentResponse.getRisk()).isEqualTo(consentRequest.getRisk());
        final String selfLinkToConsent = consentResponse.getLinks().getSelf().toString();
        assertThat(selfLinkToConsent).isEqualTo(getConsentUri(consentId));


        // Get the consent and verify it matches the create response
        when(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).thenReturn(consentStoreResponse);

        final ResponseEntity<OBDomesticVRPConsentResponse> getConsentResponse = restTemplate.exchange(selfLinkToConsent,
                HttpMethod.GET, new HttpEntity<>(httpHeaders), OBDomesticVRPConsentResponse.class);

        assertThat(getConsentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getConsentResponse.getBody()).isEqualTo(consentResponse);

    }

    @Test
    public void failsToCreateConsentWhichDoesNotPassSchemaValidation() {
        final OBDomesticVRPConsentRequest consentRequest = new OBDomesticVRPConsentRequest();

        final HttpEntity<OBDomesticVRPConsentRequest> entity = new HttpEntity<>(consentRequest, requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID));

        final ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(createConsentUri(), HttpMethod.POST,
                entity, OBErrorResponse1.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final List<OBError1> errors = response.getBody().getErrors();
        assertThat(errors).hasSize(2);
        final String fieldMustNotBeNullErrMsg = "The field received is invalid. Reason 'must not be null'";
        final String fieldInvalidErrCode = OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID.toString();
        assertThat(errors).containsExactlyInAnyOrder(new OBError1().errorCode(fieldInvalidErrCode).message(fieldMustNotBeNullErrMsg).path("risk"),
                new OBError1().errorCode(fieldInvalidErrCode).message(fieldMustNotBeNullErrMsg).path("data"));

        verifyNoMoreInteractions(consentStoreClient);
    }

    @Test
    public void failsToGetConsentThatDoesNotExist() {
        when(consentStoreClient.getConsent(anyString(), anyString())).thenThrow(new ConsentStoreClientException(ErrorType.NOT_FOUND, "Consent Not Found"));
        final ResponseEntity<OBErrorResponse1> consentNotFoundResponse = restTemplate.exchange(getConsentUri("unknown"),
                HttpMethod.GET, new HttpEntity<>(requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID)), OBErrorResponse1.class);

        assertThat(consentNotFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(consentNotFoundResponse.getBody().getCode()).isEqualTo(OBRI_CONSENT_NOT_FOUND.toString());
    }

    @Test
    public void failsToGetConsentInvalidPermissions() {
        when(consentStoreClient.getConsent(anyString(), anyString())).thenThrow(new ConsentStoreClientException(ErrorType.INVALID_PERMISSIONS, "ApiClient does not have permission to access Consent"));
        final ResponseEntity<OBErrorResponse1> consentNotFoundResponse = restTemplate.exchange(getConsentUri("unknown"),
                HttpMethod.GET, new HttpEntity<>(requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID)), OBErrorResponse1.class);

        assertThat(consentNotFoundResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(consentNotFoundResponse.getBody().getCode()).isEqualTo(OBRI_PERMISSION_INVALID.toString());
    }

    @Test
    public void deleteConsent() {
        final String consentId = "consentid-35254";
        final ResponseEntity<OBDomesticVRPConsentResponse> deleteResponse = restTemplate.exchange(getConsentUri(consentId), HttpMethod.DELETE,
                new HttpEntity<>(requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID)), OBDomesticVRPConsentResponse.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(consentStoreClient).deleteConsent(eq(consentId), eq(TEST_API_CLIENT_ID));
    }

    @Test
    public void testFundsConfirmationGetFundsAvailableResponse() {
        // Given
        final BigDecimal instructedAmount = new BigDecimal("50.00").setScale(2);
        FRBalance balance = aValidBalance(instructedAmount.multiply(new BigDecimal("5.00").setScale(2)));
        given(balanceStoreService.getBalance(balance.getAccountId(), balance.getBalance().getType())).willReturn(Optional.of(balance));

        final DomesticVRPConsent consent = buildAuthorisedConsent(createValidateConsentRequest(), balance.getAccountId());
        final String consentId = consent.getId();
        given(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).willReturn(consent);

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

        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(
                obvrpFundsConfirmationRequest, requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID)
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
    public void testFundsConfirmationGetFundsNotAvailableResponse() {
        // Given
        FRBalance balance = aValidBalance(new BigDecimal("49.99").setScale(2));
        given(balanceStoreService.getBalance(balance.getAccountId(), balance.getBalance().getType())).willReturn(Optional.of(balance));

        final DomesticVRPConsent consent = buildAuthorisedConsent(createValidateConsentRequest(), balance.getAccountId());
        final String consentId = consent.getId();
        given(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).willReturn(consent);

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

        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(
                obvrpFundsConfirmationRequest, requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID)
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
    public void failsToGetFundsConfirmationWhenConsentNotAuthorised() {
        final DomesticVRPConsent consent = buildAwaitingAuthorisationConsent(createValidateConsentRequest());
        final String consentId = consent.getId();
        given(consentStoreClient.getConsent(eq(consentId), eq(TEST_API_CLIENT_ID))).willReturn(consent);

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

        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(
                obvrpFundsConfirmationRequest, requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID)
        );

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(
                getFundsConfirmationUrl(consentId),
                request,
                OBErrorResponse1.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        validateConsentNotAuthorisedErrorResponse(response);
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


        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(
                obvrpFundsConfirmationRequest, requiredPaymentsHttpHeadersWithApiClientId(TEST_API_CLIENT_ID)
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