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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v4_0_0;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCashBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationConsent;
import com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.BalanceStoreService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.v4_0_0.RestFundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.funds.FundsConfirmationRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v4.common.ExternalProxyAccountType1Code;
import uk.org.openbanking.datamodel.v4.common.OBProxy1;
import uk.org.openbanking.datamodel.v4.fund.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRBalanceType.INTERIMAVAILABLE;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRFundsConfirmationConsentStatusConverter.toOBFundsConfirmationConsentStatusV4;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.v4.FRProxyTestDataFactory.aValidFRProxy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum.*;

/**
 * Test for {@link FundsConfirmationsApiController}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class FundsConfirmationsApiControllerTest {
    private static final String API_CLIENT_ID = UUID.randomUUID().toString();
    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredFundsHttpHeadersWithApiClientId(API_CLIENT_ID);
    private static final String BASE_URL = "http://localhost:";
    private static final String FUNDS_CONFIRMATION_URI = "/open-banking/v4.0.0/cbpii/funds-confirmations";
    private static final String DEBTOR_ACCOUNT_ID = UUID.randomUUID().toString();
    public static final String DEFAULT_CURRENCY = "GBP";

    @LocalServerPort
    private int port;

    @Autowired
    private FundsConfirmationRepository fundsConfirmationRepository;

    @MockBean
    private BalanceStoreService balanceStoreService;

    @InjectMocks
    private FundsAvailabilityService fundsAvailabilityService;

    @MockBean
    private FRAccountRepository frAccountRepository;

    @MockBean
    @Qualifier("v4.0.0RestFundsConfirmationConsentStoreClient")
    private RestFundsConfirmationConsentStoreClient consentStoreClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        fundsConfirmationRepository.deleteAll();
    }

    private void mockAccountRepository() {
        mockAccountRepository(DEFAULT_CURRENCY);
    }

    private void mockAccountRepository(String currency) {
        final FRAccount debtorAccount = FRAccount.builder()
                .account(
                        FRFinancialAccount.builder().accounts(
                                        List.of(
                                                FRAccountIdentifier.builder()
                                                        .identification("08080021325698")
                                                        .name("ACME Inc")
                                                        .schemeName("UK.OBIE.SortCodeAccountNumber")
                                                        .secondaryIdentification("0002")
                                                        .proxy(aValidFRProxy())
                                                        .build()
                                        )
                                )
                                .accountId(DEBTOR_ACCOUNT_ID)
                                .currency(currency)
                                .build()
                )
                .build();
        when(frAccountRepository.byAccountId(eq(DEBTOR_ACCOUNT_ID))).thenReturn(debtorAccount);
    }

    private void mockConsentStoreGetResponse(String consentId) {
        mockConsentStoreGetResponse(
                consentId,
                AUTHORISED,
                aValidOBFundsConfirmationConsent(),
                DateTime.now().plusMonths(3).withZone(DateTimeZone.UTC)
        );
    }

    private void mockConsentStoreGetResponse(String consentId, uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum status) {
        mockConsentStoreGetResponse(consentId, status, aValidOBFundsConfirmationConsent(), DateTime.now().plusMonths(3).withZone(DateTimeZone.UTC));
    }

    private void mockConsentStoreGetResponse(String consentId, DateTime expirationDateTime) {
        mockConsentStoreGetResponse(consentId, AUTHORISED, aValidOBFundsConfirmationConsent(), expirationDateTime);
    }

    private void mockConsentStoreGetResponse(String consentId, DateTime expirationDateTime, uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum status) {
        mockConsentStoreGetResponse(consentId, status, aValidOBFundsConfirmationConsent(), expirationDateTime);
    }

    private void mockConsentStoreGetResponse(
            String consentId,
            uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum status,
            OBFundsConfirmationConsent1 consentRequest,
            DateTime expirationDateTime
    ) {
        final FundsConfirmationConsent consent = new FundsConfirmationConsent();
        consent.setId(consentId);
        consent.setStatus(status.toString());
        FRFundsConfirmationConsent frConfirmationConsent = FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(consentRequest);
        frConfirmationConsent.getData().setExpirationDateTime(expirationDateTime);
        consent.setRequestObj(frConfirmationConsent);
        consent.setAuthorisedDebtorAccountId(DEBTOR_ACCOUNT_ID);
        when(consentStoreClient.getConsent(eq(consentId), eq(API_CLIENT_ID))).thenReturn(consent);
    }

    private void mockBalanceStoreService(String amount) {
        mockBalanceStoreService(amount, DEFAULT_CURRENCY);
    }

    private void mockBalanceStoreService(String amount, String currency) {
        Optional<FRBalance> balanceOptional = Optional.of(
                FRBalance.builder()
                        .accountId(DEBTOR_ACCOUNT_ID)
                        .balance(FRCashBalance.builder()
                                .accountId(DEBTOR_ACCOUNT_ID)
                                .amount(FRAmount.builder().amount(amount).currency(currency).build())
                                .build())
                        .build()
        );
        when(balanceStoreService.getBalance(DEBTOR_ACCOUNT_ID, INTERIMAVAILABLE)).thenReturn(balanceOptional);
    }

    @Test
    public void shouldCreateFundsConfirmationAvailabilityTrue() {
        // Given
        final String consentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        OBFundsConfirmation1 fundsConfirmationRequest = aValidOBFundsConfirmation(consentId);
        mockAccountRepository();
        String balanceAmount = new BigDecimal(fundsConfirmationRequest.getData().getInstructedAmount().getAmount()).add(new BigDecimal("1000.000")).toPlainString();
        mockBalanceStoreService(balanceAmount);
        mockConsentStoreGetResponse(consentId);
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmationRequest, HTTP_HEADERS);

        // When
        ResponseEntity<OBFundsConfirmationResponse1> fundsConfirmationResponse = restTemplate.postForEntity(fundsConfirmationUrl(), request, OBFundsConfirmationResponse1.class);

        // Then
        assertThat(fundsConfirmationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBFundsConfirmationResponse1Data responseData = fundsConfirmationResponse.getBody().getData();
        assertThat(responseData.getFundsConfirmationId()).isNotNull().isNotBlank();
        assertThat(responseData.getConsentId()).isEqualTo(fundsConfirmationRequest.getData().getConsentId());
        assertThat(responseData.getFundsAvailable()).isTrue();
        assertThat(responseData.getReference()).isEqualTo(fundsConfirmationRequest.getData().getReference());
        assertThat(responseData.getInstructedAmount()).isEqualTo(fundsConfirmationRequest.getData().getInstructedAmount());
    }

    @Test
    public void shouldCreateFundsConfirmationAvailabilityFalse() {
        // Given
        final String consentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        OBFundsConfirmation1 fundsConfirmationRequest = aValidOBFundsConfirmation(consentId);
        mockAccountRepository();
        String balanceAmount = new BigDecimal(fundsConfirmationRequest.getData().getInstructedAmount().getAmount()).add(new BigDecimal("-10.00")).toPlainString();
        mockBalanceStoreService(balanceAmount);
        mockConsentStoreGetResponse(consentId);
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmationRequest, HTTP_HEADERS);

        // When
        ResponseEntity<OBFundsConfirmationResponse1> fundsConfirmationResponse = restTemplate.postForEntity(fundsConfirmationUrl(), request, OBFundsConfirmationResponse1.class);

        // Then
        assertThat(fundsConfirmationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBFundsConfirmationResponse1Data responseData = fundsConfirmationResponse.getBody().getData();
        assertThat(responseData.getFundsConfirmationId()).isNotNull().isNotBlank();
        assertThat(responseData.getConsentId()).isEqualTo(fundsConfirmationRequest.getData().getConsentId());
        assertThat(responseData.getFundsAvailable()).isFalse();
        assertThat(responseData.getReference()).isEqualTo(fundsConfirmationRequest.getData().getReference());
        assertThat(responseData.getInstructedAmount()).isEqualTo(fundsConfirmationRequest.getData().getInstructedAmount());
    }

    @Test
    public void shouldThrowsDebtorAccountNotFound() {
        // Given
        final String consentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        OBFundsConfirmation1 fundsConfirmationRequest = aValidOBFundsConfirmation(consentId);
        String balanceAmount = new BigDecimal(fundsConfirmationRequest.getData().getInstructedAmount().getAmount()).add(new BigDecimal("100.00")).toPlainString();
        mockBalanceStoreService(balanceAmount);
        mockConsentStoreGetResponse(consentId);
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmationRequest, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> exception = restTemplate.postForEntity(fundsConfirmationUrl(), request, OBErrorResponse1.class);

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        OBErrorResponse1 errorResponse = exception.getBody();
        assertThat(errorResponse.getErrors()).isNotEmpty();
        assertThat(errorResponse.getErrors().get(0)).isEqualTo(
                OBRIErrorType.FUNDS_CONFIRMATION_DEBTOR_ACCOUNT_NOT_FOUND.toOBError1(DEBTOR_ACCOUNT_ID)
        );
        assertThat(errorResponse.getErrors().get(0).getErrorCode()).isEqualTo(
                ErrorCode.OBRI_FUNDS_CONFIRMATION_INVALID.getValue()
        );
    }

    @Test
    public void shouldFundsConfirmationCurrencyMismatch() {
        // Given
        final String otherCurrency = "EUR";
        final String consentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        OBFundsConfirmation1 fundsConfirmationRequest = aValidOBFundsConfirmation(consentId);
        String balanceAmount = new BigDecimal(fundsConfirmationRequest.getData().getInstructedAmount().getAmount()).add(new BigDecimal("100.00")).toPlainString();
        mockAccountRepository(otherCurrency);
        ValidationResult<OBError1> validationResult = new ValidationResult<>();
        validationResult.addError(OBRIErrorType.FUNDS_CONFIRMATION_CURRENCY_MISMATCH.toOBError1(DEFAULT_CURRENCY, otherCurrency));
        mockBalanceStoreService(balanceAmount);
        mockConsentStoreGetResponse(consentId);
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmationRequest, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> exception = restTemplate.postForEntity(fundsConfirmationUrl(), request, OBErrorResponse1.class);

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        OBErrorResponse1 errorResponse = exception.getBody();
        assertThat(errorResponse.getErrors()).isNotEmpty();
        assertEquals(validationResult.getErrors(), errorResponse.getErrors());
    }

    @Test
    public void shouldFundsConfirmationExpired() {
        // Given
        final String consentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        OBFundsConfirmation1 fundsConfirmationRequest = aValidOBFundsConfirmation(consentId);
        String balanceAmount = new BigDecimal(fundsConfirmationRequest.getData().getInstructedAmount().getAmount()).add(new BigDecimal("100.00")).toPlainString();
        mockAccountRepository();
        mockBalanceStoreService(balanceAmount);
        DateTime expirationDateTime = DateTime.now().minusMinutes(1).withZone(DateTimeZone.UTC);
        ValidationResult<OBError1> validationResult = new ValidationResult<>();
        validationResult.addError(OBRIErrorType.FUNDS_CONFIRMATION_EXPIRED.toOBError1(expirationDateTime));
        mockConsentStoreGetResponse(consentId, expirationDateTime);
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmationRequest, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> exception = restTemplate.postForEntity(fundsConfirmationUrl(), request, OBErrorResponse1.class);

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        OBErrorResponse1 errorResponse = exception.getBody();
        assertThat(errorResponse.getErrors()).isNotEmpty();
        assertEquals(validationResult.getErrors(), errorResponse.getErrors());
    }

    @Test
    public void shouldFundsConfirmationConsentStatusNotAuthorised() {
        // Given
        final String consentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        OBFundsConfirmation1 fundsConfirmationRequest = aValidOBFundsConfirmation(consentId);
        String balanceAmount = new BigDecimal(fundsConfirmationRequest.getData().getInstructedAmount().getAmount()).add(new BigDecimal("100.00")).toPlainString();
        mockAccountRepository();
        mockBalanceStoreService(balanceAmount);
        ValidationResult<OBError1> validationResult = new ValidationResult<>();
        validationResult.addError(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(REJECTED));
        mockConsentStoreGetResponse(consentId, REJECTED);
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmationRequest, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> exception = restTemplate.postForEntity(fundsConfirmationUrl(), request, OBErrorResponse1.class);

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        OBErrorResponse1 errorResponse = exception.getBody();
        assertThat(errorResponse.getErrors()).isNotEmpty();
        assertEquals(validationResult.getErrors(), errorResponse.getErrors());
    }

    @Test
    public void shouldFundsConfirmationValidationFailsAll() {
        // Given
        final String otherCurrency = "EUR";
        final String consentId = IntentType.FUNDS_CONFIRMATION_CONSENT.generateIntentId();
        OBFundsConfirmation1 fundsConfirmationRequest = aValidOBFundsConfirmation(consentId);
        String balanceAmount = new BigDecimal(fundsConfirmationRequest.getData().getInstructedAmount().getAmount()).add(new BigDecimal("-10.00")).toPlainString();
        mockAccountRepository(otherCurrency);
        mockBalanceStoreService(balanceAmount);
        DateTime expirationDateTime = DateTime.now().minusMinutes(1).withZone(DateTimeZone.UTC);
        ValidationResult<OBError1> validationResult = new ValidationResult<>();
        validationResult.addError(OBRIErrorType.FUNDS_CONFIRMATION_CURRENCY_MISMATCH.toOBError1(DEFAULT_CURRENCY, otherCurrency));
        validationResult.addError(OBRIErrorType.FUNDS_CONFIRMATION_EXPIRED.toOBError1(expirationDateTime));
        validationResult.addError(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(REVOKED));
        mockConsentStoreGetResponse(consentId, expirationDateTime, REVOKED);
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmationRequest, HTTP_HEADERS);

        // When
        ResponseEntity<OBErrorResponse1> exception = restTemplate.postForEntity(fundsConfirmationUrl(), request, OBErrorResponse1.class);

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        OBErrorResponse1 errorResponse = exception.getBody();
        assertThat(errorResponse.getErrors()).isNotEmpty();
        assertEquals(validationResult.getErrors(), errorResponse.getErrors());
    }

    private OBFundsConfirmation1 aValidOBFundsConfirmation(String consentId) {
        return aValidOBFundsConfirmation(consentId, DEFAULT_CURRENCY);
    }

    private OBFundsConfirmation1 aValidOBFundsConfirmation(String consentId, String currency) {
        return (new OBFundsConfirmation1().data(
                new OBFundsConfirmation1Data()
                        .reference("test-reference")
                        .consentId(consentId)
                        .instructedAmount(
                                new OBFundsConfirmation1DataInstructedAmount()
                                        .amount("20.00")
                                        .currency(currency)
                        )
        ));
    }

    private OBFundsConfirmationConsent1 aValidOBFundsConfirmationConsent() {
        return (new OBFundsConfirmationConsent1())
                .data(
                        new OBFundsConfirmationConsent1Data()
                                .expirationDateTime(DateTime.now().plusMonths(5))
                                .debtorAccount(
                                        new OBFundsConfirmationConsent1DataDebtorAccount()
                                                .identification("08080021325698")
                                                .name("ACME Inc")
                                                .schemeName("UK.OBIE.SortCodeAccountNumber")
                                                .secondaryIdentification("0002")
                                                .proxy(new OBProxy1().code(ExternalProxyAccountType1Code.fromValue("TELE")).identification("+441632960540").type("Telephone"))
                                )
                );
    }

    private String fundsConfirmationUrl() {
        return BASE_URL + port + FUNDS_CONFIRMATION_URI;
    }

}
