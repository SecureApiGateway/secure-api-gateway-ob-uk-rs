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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_1_6.beneficiaries;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountBeneficiary;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRBeneficiary;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.beneficiaries.FRBeneficiaryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.account.OBReadBeneficiary5;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRAccountBeneficiaryTestDataFactory.aValidFRAccountBeneficiary;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredAccountHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link BeneficiariesApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class BeneficiariesApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNT_BENEFICIARIES_URI = "/open-banking/v3.1.6/aisp/accounts/{AccountId}/beneficiaries";
    private static final String BENEFICIARIES_URI = "/open-banking/v3.1.6/aisp/beneficiaries";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRBeneficiaryRepository frBeneficiaryRepository;

    @Autowired
    private TestRestTemplate restTemplate;

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

        FRAccountBeneficiary accountBeneficiary = aValidFRAccountBeneficiary(accountId);
        FRBeneficiary beneficiary = FRBeneficiary.builder()
                .accountId(accountId)
                .beneficiary(accountBeneficiary)
                .build();
        frBeneficiaryRepository.save(beneficiary);
    }

    @AfterEach
    public void removeData() {
        frAccountRepository.deleteAll();
        frBeneficiaryRepository.deleteAll();
    }

    @Test
    public void shouldGetAccountBeneficiaries() {
        // Given
        String url = accountBeneficiariesUrl(accountId);

        // When
        ResponseEntity<OBReadBeneficiary5> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountHttpHeaders(url, accountId)),
                OBReadBeneficiary5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadBeneficiary5 returnedBeneficiary = response.getBody();
        assertThat(returnedBeneficiary).isNotNull();
        assertThat(returnedBeneficiary.getData().getBeneficiary().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(url);
    }

    @Test
    public void shouldGetBeneficiaries() {
        // Given
        String url = beneficiariesUrl();

        // When
        ResponseEntity<OBReadBeneficiary5> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountHttpHeaders(url, accountId)),
                OBReadBeneficiary5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadBeneficiary5 returnedBeneficiary = response.getBody();
        assertThat(returnedBeneficiary).isNotNull();
        assertThat(returnedBeneficiary.getData().getBeneficiary().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(url);
    }

    private String accountBeneficiariesUrl(String accountId) {
        String url = BASE_URL + port + ACCOUNT_BENEFICIARIES_URI;
        return url.replace("{AccountId}", accountId);
    }

    private String beneficiariesUrl() {
        return BASE_URL + port + BENEFICIARIES_URI;
    }
}