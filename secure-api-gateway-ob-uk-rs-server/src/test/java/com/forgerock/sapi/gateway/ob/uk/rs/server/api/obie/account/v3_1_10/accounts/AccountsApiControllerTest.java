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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.accounts;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRReadConsent;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRReadConsentData;
import com.forgerock.sapi.gateway.rcs.consent.store.client.account.AccountAccessConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import org.junit.jupiter.api.AfterEach;
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
import uk.org.openbanking.datamodel.v3.account.OBAccount6;
import uk.org.openbanking.datamodel.v3.account.OBReadAccount6;

import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRFinancialAccountConverter.toOBExternalAccountSubType1Code;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRFinancialAccountConverter.toOBExternalAccountType1Code;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredAccountApiHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link AccountsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class AccountsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNTS_URI = "/open-banking/v3.1.10/aisp/accounts";

    @LocalServerPort
    private int port;
    @MockBean
    @Qualifier("v3.1.10RestAccountAccessConsentStoreClient")
    private AccountAccessConsentStoreClient accountAccessConsentStoreClient;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
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
        final String apiClientId = "client-123";
        final String consentId = "consent-343553";
        // This makes this test ensure that the DETAIL permission overrides the BASIC permissions. See issue
        // https://github.com/SecureApiGateway/SecureApiGateway/issues/1431
        List<FRExternalPermissionsCode> permissions = List.of(FRExternalPermissionsCode.READACCOUNTSDETAIL, FRExternalPermissionsCode.READACCOUNTSBASIC);
        mockAuthorisedConsentResponse(accountId, apiClientId, consentId, permissions);

        ResponseEntity<OBReadAccount6> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consentId, apiClientId)),
                OBReadAccount6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBAccount6 returnedAccount = response.getBody().getData().getAccount().get(0);
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

        final String apiClientId = "client-123";
        final String consentId = "consent-343553";

        List<FRExternalPermissionsCode> permissions = List.of(FRExternalPermissionsCode.READACCOUNTSDETAIL);
        mockAuthorisedConsentResponse(accountId, apiClientId, consentId, permissions);

        // When
        ResponseEntity<OBReadAccount6> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consentId, apiClientId)),
                OBReadAccount6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBAccount6 returnedAccount = response.getBody().getData().getAccount().get(0);
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

    private void mockAuthorisedConsentResponse(String accountId, String apiClientId, String consentId, List<FRExternalPermissionsCode> permissions) {
        final AccountAccessConsent consent = new AccountAccessConsent();
        consent.setId(consentId);
        consent.setApiClientId(apiClientId);
        consent.setStatus("Authorised");
        consent.setAuthorisedAccountIds(List.of(accountId));
        consent.setRequestObj(FRReadConsent.builder().data(FRReadConsentData.builder().permissions(permissions).build()).build());
        given(accountAccessConsentStoreClient.getConsent(eq(consentId), eq(apiClientId))).willReturn(consent);
    }

    private String accountsUrl() {
        return BASE_URL + port + ACCOUNTS_URI;
    }

    private String accountsIdUrl(String id) {
        return accountsUrl() + "/" + id;
    }
}