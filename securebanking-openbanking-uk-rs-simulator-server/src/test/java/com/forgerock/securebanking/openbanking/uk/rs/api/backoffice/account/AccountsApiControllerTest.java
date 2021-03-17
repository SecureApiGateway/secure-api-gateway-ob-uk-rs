/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.account;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountWithBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRBalanceType;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCashBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCreditDebitIndicator;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAmount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRBalance;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.balances.FRBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.FRAccountTestDataFactory.aValidFRAccount;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Unit test for {@link AccountsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class AccountsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String FIND_USER_ACCOUNTS_URI = "/backoffice/accounts/search/findByUserId";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRBalanceRepository frBalanceRepository;

    @BeforeEach
    public void setup() {
        frAccountRepository.deleteAll();
        frBalanceRepository.deleteAll();
    }

    @Test
    public void shouldFindUserAccountsWithBalance() {
        // Given
        FRAccount account = aValidFRAccount();
        frAccountRepository.save(account);
        FRBalance accountBalance = aValidFRBalance(account.getId());
        frBalanceRepository.save(accountBalance);
        URI uri = findUserAccountsUriWithBalance(account.getUserID());
        ParameterizedTypeReference<List<FRAccountWithBalance>> typeReference = new ParameterizedTypeReference<>() {
        };

        // When
        ResponseEntity<List<FRAccountWithBalance>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders()),
                typeReference);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        FRAccountWithBalance accountWithBalance = response.getBody().get(0);
        assertThat(accountWithBalance.getId()).isEqualTo(account.getId());
        assertThat(accountWithBalance.getUserId()).isEqualTo(account.getUserID());
        assertThat(accountWithBalance.getAccount().getAccountId()).isEqualTo(account.getAccount().getAccountId());
        assertThat(accountWithBalance.getBalances()).isNotEmpty();
        assertThat(accountWithBalance.getBalances().get(0)).isEqualTo(accountBalance.getBalance());
    }

    @Test
    public void shouldFindUserAccountsWithoutBalance() {
        // Given
        FRAccount account = aValidFRAccount();
        frAccountRepository.save(account);
        FRBalance accountBalance = aValidFRBalance(account.getId());
        frBalanceRepository.save(accountBalance);
        URI uri = findUserAccountsUri(account.getUserID(), false);
        ParameterizedTypeReference<List<FRAccountWithBalance>> typeReference = new ParameterizedTypeReference<>() {
        };

        // When
        ResponseEntity<List<FRAccountWithBalance>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders()),
                typeReference);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        FRAccountWithBalance accountWithBalance = response.getBody().get(0);
        assertThat(accountWithBalance.getId()).isEqualTo(account.getId());
        assertThat(accountWithBalance.getBalances()).isEmpty();
    }

    @Test
    public void shouldNotFindUserAccountsWithBalanceGivenUnknownUser() {
        // Given
        FRAccount account = aValidFRAccount();
        frAccountRepository.save(account);
        FRBalance accountBalance = aValidFRBalance(account.getId());
        frBalanceRepository.save(accountBalance);
        URI uri = findUserAccountsUri("unknown-user-id", false);
        ParameterizedTypeReference<List<FRAccountWithBalance>> typeReference = new ParameterizedTypeReference<>() {
        };

        // When
        ResponseEntity<List<FRAccountWithBalance>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders()),
                typeReference);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    public void shouldFailToFindUserAccountsWithBalanceGivenMissingUserId() {
        // Given
        String uri = BASE_URL + port + FIND_USER_ACCOUNTS_URI;

        // When
        ResponseEntity<?> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(httpHeaders()), Void.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private FRBalance aValidFRBalance(String accountId) {
        FRBalance accountBalance = FRBalance.builder()
                .accountId(accountId)
                .balance(FRCashBalance.builder()
                        .accountId(accountId)
                        .creditDebitIndicator(FRCreditDebitIndicator.CREDIT)
                        .type(FRBalanceType.INTERIMAVAILABLE)
                        .amount(FRAmount.builder()
                                .currency("GBP")
                                .amount("10.00")
                                .build())
                        .build())
                .build();
        return accountBalance;
    }

    private URI findUserAccountsUri(String userId, boolean withBalance) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL + port + FIND_USER_ACCOUNTS_URI);
        builder.queryParam("userId", userId);
        builder.queryParam("withBalance", withBalance);
        return builder.build().encode().toUri();
    }

    private URI findUserAccountsUriWithBalance(String userId) {
        return findUserAccountsUri(userId, true);
    }

    public static HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}