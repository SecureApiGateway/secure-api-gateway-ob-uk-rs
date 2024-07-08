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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.account;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRBalanceType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCashBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCreditDebitIndicator;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances.FRBalanceRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.FRAccountTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

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
    private static final String FIND_USER_ACCOUNTS_URI_BY_IDENTIFIERS = "/backoffice/accounts/search/findByAccountIdentifiers";

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
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
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
    public void shouldFindUserAccountsWithMultipleBalances() {
        // Given
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
        frAccountRepository.save(account);

        final List<FRBalance> balances = createMultipleBalances(account);

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
        assertThat(accountWithBalance.getBalances()).isEqualTo(balances.stream().map(FRBalance::getBalance).toList());
    }

    @Test
    public void shouldFindUserAccountsWithoutBalance() {
        // Given
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
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
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
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

    @Test
    public void shouldFindAccountWithBalanceByAccountIdentifiers(){
        // Given
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
        frAccountRepository.save(account);
        FRBalance accountBalance = aValidFRBalance(account.getId());
        frBalanceRepository.save(accountBalance);
        URI uri = findAccountUriByAccountIdentifiers(account.getUserID(), account.getAccount().getFirstAccount());
        ParameterizedTypeReference<FRAccountWithBalance> typeReference = new ParameterizedTypeReference<>() {
        };

        // When
        ResponseEntity<FRAccountWithBalance> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders()),
                typeReference);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        FRAccountWithBalance accountWithBalance = response.getBody();
        assertThat(accountWithBalance.getId()).isEqualTo(account.getId());
        assertThat(accountWithBalance.getUserId()).isEqualTo(account.getUserID());
        assertThat(accountWithBalance.getAccount().getAccountId()).isEqualTo(account.getAccount().getAccountId());
        assertThat(accountWithBalance.getBalances()).isNotEmpty();
        assertThat(accountWithBalance.getBalances().get(0)).isEqualTo(accountBalance.getBalance());
    }

    @Test
    public void shouldFindAccountWithMultipleBalancesByAccountIdentifiers(){
        // Given
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
        frAccountRepository.save(account);
        final List<FRBalance> balances = createMultipleBalances(account);
        URI uri = findAccountUriByAccountIdentifiers(account.getUserID(), account.getAccount().getFirstAccount());
        ParameterizedTypeReference<FRAccountWithBalance> typeReference = new ParameterizedTypeReference<>() {
        };

        // When
        ResponseEntity<FRAccountWithBalance> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders()),
                typeReference);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        FRAccountWithBalance accountWithBalance = response.getBody();
        assertThat(accountWithBalance.getId()).isEqualTo(account.getId());
        assertThat(accountWithBalance.getUserId()).isEqualTo(account.getUserID());
        assertThat(accountWithBalance.getAccount().getAccountId()).isEqualTo(account.getAccount().getAccountId());
        assertThat(accountWithBalance.getBalances()).isNotEmpty();
        assertThat(accountWithBalance.getBalances()).isEqualTo(balances.stream().map(FRBalance::getBalance).toList());
    }

    private List<FRBalance> createMultipleBalances(FRAccount account) {
        List<FRBalance> balances = List.of(aValidFRBalance(account.getId(), FRBalanceType.PREVIOUSLYCLOSEDBOOKED),
                                           aValidFRBalance(account.getId(), FRBalanceType.INTERIMAVAILABLE),
                                           aValidFRBalance(account.getId(), FRBalanceType.INTERIMBOOKED));
        frBalanceRepository.saveAll(balances);
        return balances;
    }

    @Test
    public void shouldFindAccountWithBalanceByAccountIdentifiersNoUserId(){
        // Given
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
        frAccountRepository.save(account);
        FRBalance accountBalance = aValidFRBalance(account.getId());
        frBalanceRepository.save(accountBalance);
        URI uri = findAccountUriByAccountIdentifiers(null, account.getAccount().getFirstAccount());
        ParameterizedTypeReference<FRAccountWithBalance> typeReference = new ParameterizedTypeReference<>() {
        };

        // When
        ResponseEntity<FRAccountWithBalance> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders()),
                typeReference);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        FRAccountWithBalance accountWithBalance = response.getBody();
        assertThat(accountWithBalance.getId()).isEqualTo(account.getId());
        assertThat(accountWithBalance.getUserId()).isEqualTo(account.getUserID());
        assertThat(accountWithBalance.getAccount().getAccountId()).isEqualTo(account.getAccount().getAccountId());
        assertThat(accountWithBalance.getBalances()).isNotEmpty();
        assertThat(accountWithBalance.getBalances().get(0)).isEqualTo(accountBalance.getBalance());
    }

    private FRBalance aValidFRBalance(String accountId) {
        return aValidFRBalance(accountId, FRBalanceType.INTERIMAVAILABLE);
    }

    private FRBalance aValidFRBalance(String accountId, FRBalanceType balanceType) {
        FRBalance accountBalance = FRBalance.builder()
                .accountId(accountId)
                .balance(FRCashBalance.builder()
                        .accountId(accountId)
                        .creditDebitIndicator(FRCreditDebitIndicator.CREDIT)
                        .type(balanceType)
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

    private URI findAccountUriByAccountIdentifiers(String userId, FRAccountIdentifier accountIdentifier){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL + port + FIND_USER_ACCOUNTS_URI_BY_IDENTIFIERS);
        if(Objects.nonNull(userId)) {
            builder.queryParam("userId", userId);
        }
        builder.queryParam("identification", accountIdentifier.getIdentification());
        builder.queryParam("name", accountIdentifier.getName());
        builder.queryParam("schemeName", accountIdentifier.getSchemeName());
        return builder.build().encode().toUri();
    }

    public static HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
