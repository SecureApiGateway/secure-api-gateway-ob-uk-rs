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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v4_0_0.scheduledpayments;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRScheduledPaymentData;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v4_0_0.scheduledpayments.ScheduledPaymentsApiController;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRScheduledPayment;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.scheduledpayments.FRScheduledPaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.v4.account.OBReadScheduledPayment3;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.v4.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.v4.account.FRScheduledPaymentDataTestDataFactory.aValidFRScheduledPaymentData;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpersV4.createAuthorisedConsentAllPermissions;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpersV4.mockAccountResourceAccessServiceResponse;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredAccountApiHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link ScheduledPaymentsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class ScheduledPaymentsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNT_SCHEDULED_PAYMENTS_URI = "/open-banking/v4.0.0/aisp/accounts/{AccountId}/scheduled-payments";
    private static final String SCHEDULED_PAYMENTS_URI = "/open-banking/v4.0.0/aisp/scheduled-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRScheduledPaymentRepository frScheduledPaymentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    @Qualifier("v4.0.0DefaultAccountResourceAccessService")
    private AccountResourceAccessService accountResourceAccessService;

    private String accountId;

    @BeforeEach
    public void saveData() {
        FRFinancialAccount financialAccount = aValidFRFinancialAccount();
        FRAccount account = FRAccount.builder()
                .userID("AUserId")
                .account(financialAccount)
                .latestStatementId("5678")
                .build();
        frAccountRepository.save(account);
        accountId = account.getId();

        FRScheduledPaymentData scheduledPaymentData = aValidFRScheduledPaymentData(accountId);
        FRScheduledPayment scheduledPayment = FRScheduledPayment.builder()
                .accountId(accountId)
                .scheduledPayment(scheduledPaymentData)
                .build();
        frScheduledPaymentRepository.save(scheduledPayment);
    }

    @AfterEach
    public void removeData() {
        frAccountRepository.deleteAll();
        frScheduledPaymentRepository.deleteAll();
    }

    @Test
    public void shouldGetAccountScheduledPayments() {
        // Given
        String url = accountScheduledPaymentsUrl(accountId);

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent, accountId);

        // When
        ResponseEntity<OBReadScheduledPayment3> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadScheduledPayment3.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadScheduledPayment3 returnedPayment = response.getBody();
        assertThat(returnedPayment).isNotNull();
        assertThat(returnedPayment.getData().getScheduledPayment().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    @Test
    public void shouldGetScheduledPayments() {
        // Given
        String url = scheduledPaymentsUrl();

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent);

        // When
        ResponseEntity<OBReadScheduledPayment3> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadScheduledPayment3.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadScheduledPayment3 returnedPayment = response.getBody();
        assertThat(returnedPayment).isNotNull();
        assertThat(returnedPayment.getData().getScheduledPayment().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    private String accountScheduledPaymentsUrl(String accountId) {
        String url = BASE_URL + port + ACCOUNT_SCHEDULED_PAYMENTS_URI;
        return url.replace("{AccountId}", accountId);
    }

    private String scheduledPaymentsUrl() {
        return BASE_URL + port + SCHEDULED_PAYMENTS_URI;
    }
}