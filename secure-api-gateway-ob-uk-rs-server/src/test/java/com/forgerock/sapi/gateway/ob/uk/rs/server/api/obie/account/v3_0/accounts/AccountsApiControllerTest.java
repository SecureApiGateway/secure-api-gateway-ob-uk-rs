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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_0.accounts;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.account.OBAccount2;
import uk.org.openbanking.datamodel.account.OBReadAccount2;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRFinancialAccountConverter.toOBExternalAccountSubType1Code;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRFinancialAccountConverter.toOBExternalAccountType1Code;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link AccountsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class AccountsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNTS_URI = "/open-banking/v3.0/aisp/accounts";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    public void removeData() {
        frAccountRepository.deleteAll();
    }

    @Test
    public void shouldGetAccount() {
        // Given
        FRFinancialAccount financialAccount = aValidFRFinancialAccount();
        FRAccount account = FRAccount.builder()
                .userID("AUserId")
                .account(financialAccount)
                .latestStatementId("5678")
                .build();
        frAccountRepository.save(account);
        String accountId = account.getId();
        String url = accountsIdUrl(accountId);

        // When
        ResponseEntity<OBReadAccount2> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(HttpHeadersTestDataFactory.requiredAccountHttpHeaders(url, accountId)),
                OBReadAccount2.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBAccount2 returnedAccount = response.getBody().getData().getAccount().get(0);
        assertThat(returnedAccount).isNotNull();
        assertThat(returnedAccount.getAccountId()).isEqualTo(financialAccount.getAccountId());
        assertThat(returnedAccount.getCurrency()).isEqualTo(financialAccount.getCurrency());
        assertThat(returnedAccount.getAccountType()).isEqualTo(toOBExternalAccountType1Code(financialAccount.getAccountType()));
        assertThat(returnedAccount.getAccountSubType()).isEqualTo(toOBExternalAccountSubType1Code(financialAccount.getAccountSubType()));
        assertThat(returnedAccount.getDescription()).isEqualTo(financialAccount.getDescription());
        assertThat(returnedAccount.getNickname()).isEqualTo(financialAccount.getNickname());
        assertThat(returnedAccount.getServicer().getSchemeName()).isEqualTo(financialAccount.getServicer().getSchemeName());
        assertThat(returnedAccount.getServicer().getIdentification()).isEqualTo(financialAccount.getServicer().getIdentification());
        assertThat(returnedAccount.getAccount().get(0).getIdentification()).isEqualTo(financialAccount.getAccounts().get(0).getIdentification());
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    @Test
    public void shouldGetAccounts() {
        // Given
        FRFinancialAccount financialAccount = aValidFRFinancialAccount();
        FRAccount account = FRAccount.builder()
                .userID("AUserId")
                .account(financialAccount)
                .latestStatementId("5678")
                .build();
        frAccountRepository.save(account);
        String accountId = account.getId();
        String url = accountsUrl();

        // When
        ResponseEntity<OBReadAccount2> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(HttpHeadersTestDataFactory.requiredAccountHttpHeaders(url, accountId)),
                OBReadAccount2.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBAccount2 returnedAccount = response.getBody().getData().getAccount().get(0);
        assertThat(returnedAccount).isNotNull();
        assertThat(returnedAccount.getAccountId()).isEqualTo(financialAccount.getAccountId());
        assertThat(returnedAccount.getCurrency()).isEqualTo(financialAccount.getCurrency());
        assertThat(returnedAccount.getAccountType()).isEqualTo(toOBExternalAccountType1Code(financialAccount.getAccountType()));
        assertThat(returnedAccount.getAccountSubType()).isEqualTo(toOBExternalAccountSubType1Code(financialAccount.getAccountSubType()));
        assertThat(returnedAccount.getDescription()).isEqualTo(financialAccount.getDescription());
        assertThat(returnedAccount.getNickname()).isEqualTo(financialAccount.getNickname());
        assertThat(returnedAccount.getServicer().getSchemeName()).isEqualTo(financialAccount.getServicer().getSchemeName());
        assertThat(returnedAccount.getServicer().getIdentification()).isEqualTo(financialAccount.getServicer().getIdentification());
        assertThat(returnedAccount.getAccount().get(0).getIdentification()).isEqualTo(financialAccount.getAccounts().get(0).getIdentification());
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    private String accountsUrl() {
        return BASE_URL + port + ACCOUNTS_URI;
    }

    private String accountsIdUrl(String id) {
        return accountsUrl() + "/" + id;
    }
}