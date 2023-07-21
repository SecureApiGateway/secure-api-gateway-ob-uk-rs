/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.standingorders;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.testsupport.account.FRStandingOrderDataTestDataFactory.aValidFRStandingOrderData;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpers.createAuthorisedConsentAllPermissions;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.AccountResourceAccessServiceTestHelpers.mockAccountResourceAccessServiceResponse;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredAccountApiHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRStandingOrderData;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_6.standingorders.StandingOrdersApiController;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStandingOrder;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.standingorders.FRStandingOrderRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import uk.org.openbanking.datamodel.account.OBReadStandingOrder6;

/**
 * Spring Boot Test for {@link StandingOrdersApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class StandingOrdersApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNT_STANDING_ORDERS_URI = "/open-banking/v3.1.10/aisp/accounts/{AccountId}/standing-orders";
    private static final String STANDING_ORDERS_URI = "/open-banking/v3.1.10/aisp/standing-orders";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRStandingOrderRepository frStandingOrderRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
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

        FRStandingOrderData standingOrderData = aValidFRStandingOrderData(accountId);
        FRStandingOrder standingOrder = FRStandingOrder.builder()
                .accountId(accountId)
                .standingOrder(standingOrderData)
                .build();
        frStandingOrderRepository.save(standingOrder);
    }

    @AfterEach
    public void removeData() {
        frAccountRepository.deleteAll();
        frStandingOrderRepository.deleteAll();
    }

    @Test
    public void shouldGetAccountStandingOrders() {
        // Given
        String url = accountStandingOrdersUrl(accountId);

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent, accountId);

        // When
        ResponseEntity<OBReadStandingOrder6> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadStandingOrder6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadStandingOrder6 returnedStandingOrder = response.getBody();
        assertThat(returnedStandingOrder).isNotNull();
        assertThat(returnedStandingOrder.getData().getStandingOrder().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    @Test
    public void shouldGetStandingOrders() {
        // Given
        String url = standingOrdersUrl();

        final AccountAccessConsent consent = createAuthorisedConsentAllPermissions(accountId);
        mockAccountResourceAccessServiceResponse(accountResourceAccessService, consent);

        // When
        ResponseEntity<OBReadStandingOrder6> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountApiHeaders(consent.getId(), consent.getApiClientId())),
                OBReadStandingOrder6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadStandingOrder6 returnedStandingOrder = response.getBody();
        assertThat(returnedStandingOrder).isNotNull();
        assertThat(returnedStandingOrder.getData().getStandingOrder().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf().toString()).isEqualTo(url);
    }

    private String accountStandingOrdersUrl(String accountId) {
        String url = BASE_URL + port + ACCOUNT_STANDING_ORDERS_URI;
        return url.replace("{AccountId}", accountId);
    }

    private String standingOrdersUrl() {
        return BASE_URL + port + STANDING_ORDERS_URI;
    }
}