/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
///*
// * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v4_0_0.balances;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCashBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRTotalValue;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances.FRBalanceRepository;
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
import uk.org.openbanking.datamodel.v4.account.OBReadBalance1;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.v4.FRTotalValueTestDataFactory.aValidFRTotalValue;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.v4.account.FRCashBalanceTestDataFactory.aValidFRCashBalance;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.v4.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpersV4.createAuthorisedConsentAllPermissions;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpersV4.mockAccountResourceAccessServiceResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link BalancesApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class BalancesApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNT_BALANCES_URI = "/open-banking/v4.0.0/aisp/accounts/{AccountId}/balances";
    private static final String BALANCES_URI = "/open-banking/v4.0.0/aisp/balances";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRBalanceRepository frBalanceRepository;

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

        FRCashBalance cashBalance = aValidFRCashBalance(accountId);
        FRTotalValue totalValue = aValidFRTotalValue();
        FRBalance balance = FRBalance.builder()
                .accountId(accountId)
                .balance(cashBalance)
                .totalValue(totalValue)
                .build();
        frBalanceRepository.save(balance);
    }

    @AfterEach
    public void removeData() {
        frAccountRepository.deleteAll();
        frBalanceRepository.deleteAll();
    }

    @Test
    public void shouldGetAccountBalances() {
        // Given
        String url = accountBalancesUrl(accountId);

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent, accountId);

        // When
        ResponseEntity<OBReadBalance1> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(HttpHeadersTestDataFactory.requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadBalance1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadBalance1 returnedBalance = response.getBody();
        assertThat(returnedBalance).isNotNull();
        assertThat(returnedBalance.getData().getBalance().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    @Test
    public void shouldGetBalances() {
        // Given
        String url = balancesUrl();

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent);

        // When
        ResponseEntity<OBReadBalance1> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(HttpHeadersTestDataFactory.requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadBalance1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadBalance1 returnedBalance = response.getBody();
        assertThat(returnedBalance).isNotNull();
        assertThat(returnedBalance.getData().getBalance().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    private String accountBalancesUrl(String accountId) {
        String url = BASE_URL + port + ACCOUNT_BALANCES_URI;
        return url.replace("{AccountId}", accountId);
    }

    private String balancesUrl() {
        return BASE_URL + port + BALANCES_URI;
    }
}