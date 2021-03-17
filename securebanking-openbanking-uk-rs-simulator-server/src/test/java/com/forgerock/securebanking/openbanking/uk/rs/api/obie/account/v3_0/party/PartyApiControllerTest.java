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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_0.party;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRPartyData;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRParty;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.party.FRPartyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.account.OBReadParty1;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRPartyDataTestDataFactory.aValidFRPartyData;
import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredAccountHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link PartyApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class PartyApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNT_PARTY_URI = "/open-banking/v3.0/aisp/accounts/{AccountId}/party";
    private static final String PARTY_URI = "/open-banking/v3.0/aisp/party";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRPartyRepository frPartyRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String accountId;

    private FRPartyData partyData = aValidFRPartyData();

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

        FRParty party = FRParty.builder()
                .accountId(accountId)
                .party(partyData)
                .userId("AUserId")
                .build();
        frPartyRepository.save(party);
    }

    @AfterEach
    public void removeData() {
        frAccountRepository.deleteAll();
        frPartyRepository.deleteAll();
    }

    @Test
    public void shouldGetAccountParty() {
        // Given
        String url = accountPartyUrl(accountId);

        // When
        ResponseEntity<OBReadParty1> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountHttpHeaders(url, accountId)),
                OBReadParty1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadParty1 returnedParty = response.getBody();
        assertThat(returnedParty).isNotNull();
        assertThat(returnedParty.getData().getParty().getPartyId()).isEqualTo(partyData.getPartyId());
        assertThat(returnedParty.getData().getParty().getPartyNumber()).isEqualTo(partyData.getPartyNumber());
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(url);
    }

    @Test
    public void shouldGetParty() {
        // Given
        String url = partyUrl();
        HttpHeaders headers = requiredAccountHttpHeaders(url, accountId);
        headers.add("x-ob-user-id", "AUserId");

        // When
        ResponseEntity<OBReadParty1> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                OBReadParty1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadParty1 returnedParty = response.getBody();
        assertThat(returnedParty).isNotNull();
        assertThat(returnedParty.getData().getParty().getPartyId()).isEqualTo(partyData.getPartyId());
        assertThat(returnedParty.getData().getParty().getPartyNumber()).isEqualTo(partyData.getPartyNumber());
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(url);
    }

    private String accountPartyUrl(String accountId) {
        String url = BASE_URL + port + ACCOUNT_PARTY_URI;
        return url.replace("{AccountId}", accountId);
    }

    private String partyUrl() {
        return BASE_URL + port + PARTY_URI;
    }
}