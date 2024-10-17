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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v4_0_0.directdebits;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRDirectDebitData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v4_0_0.directdebits.DirectDebitsApiController;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRDirectDebit;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.v4.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.v4.accounts.directdebits.FRDirectDebitRepository;
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
import uk.org.openbanking.datamodel.v4.account.OBReadDirectDebit2;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.v4.account.FRDirectDebitDataTestDataFactory.aValidFRDirectDebitData;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.v4.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpersV4.createAuthorisedConsentAllPermissions;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpersV4.mockAccountResourceAccessServiceResponse;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredAccountApiHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link DirectDebitsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DirectDebitsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNT_DIRECT_DEBIT_URI = "/open-banking/v4.0.0/aisp/accounts/{AccountId}/direct-debits";
    private static final String DIRECT_DEBIT_URI = "/open-banking/v4.0.0/aisp/direct-debits";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRDirectDebitRepository frDirectDebitRepository;

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

        FRDirectDebitData directDebitData = aValidFRDirectDebitData(accountId);
        FRDirectDebit directDebit = FRDirectDebit.builder()
                .accountId(accountId)
                .directDebit(directDebitData)
                .build();
        frDirectDebitRepository.save(directDebit);
    }

    @AfterEach
    public void removeData() {
        frAccountRepository.deleteAll();
        frDirectDebitRepository.deleteAll();
    }

    @Test
    public void shouldGetAccountDirectDebits() {
        // Given
        String url = accountDirectDebitsUrl(accountId);

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent, accountId);

        // When
        ResponseEntity<OBReadDirectDebit2> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadDirectDebit2.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadDirectDebit2 returnedDirectDebit = response.getBody();
        assertThat(returnedDirectDebit).isNotNull();
        assertThat(returnedDirectDebit.getData().getDirectDebit().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    @Test
    public void shouldGetDirectDebits() {
        // Given
        String url = directDebitsUrl();

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent);

        // When
        ResponseEntity<OBReadDirectDebit2> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadDirectDebit2.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadDirectDebit2 returnedDirectDebit = response.getBody();
        assertThat(returnedDirectDebit).isNotNull();
        assertThat(returnedDirectDebit.getData().getDirectDebit().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    private String accountDirectDebitsUrl(String accountId) {
        String url = BASE_URL + port + ACCOUNT_DIRECT_DEBIT_URI;
        return url.replace("{AccountId}", accountId);
    }

    private String directDebitsUrl() {
        return BASE_URL + port + DIRECT_DEBIT_URI;
    }
}