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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_0.scheduledpayments;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRFinancialAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRScheduledPaymentData;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRScheduledPayment;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.scheduledpayments.FRScheduledPaymentRepository;
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
import uk.org.openbanking.datamodel.account.OBReadScheduledPayment1;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRFinancialAccountTestDataFactory.aValidFRFinancialAccount;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.testsupport.account.FRScheduledPaymentDataTestDataFactory.aValidFRScheduledPaymentData;
import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredAccountHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Spring Boot Test for {@link ScheduledPaymentsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class ScheduledPaymentsApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String ACCOUNT_SCHEDULED_PAYMENTS_URI = "/open-banking/v3.0/aisp/accounts/{AccountId}/scheduled-payments";
    private static final String SCHEDULED_PAYMENTS_URI = "/open-banking/v3.0/aisp/scheduled-payments";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRScheduledPaymentRepository frScheduledPaymentRepository;

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

        // When
        ResponseEntity<OBReadScheduledPayment1> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountHttpHeaders(url, accountId)),
                OBReadScheduledPayment1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadScheduledPayment1 returnedPayment = response.getBody();
        assertThat(returnedPayment).isNotNull();
        assertThat(returnedPayment.getData().getScheduledPayment().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(url);
    }

    @Test
    public void shouldGetScheduledPayments() {
        // Given
        String url = scheduledPaymentsUrl();

        // When
        ResponseEntity<OBReadScheduledPayment1> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(requiredAccountHttpHeaders(url, accountId)),
                OBReadScheduledPayment1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBReadScheduledPayment1 returnedPayment = response.getBody();
        assertThat(returnedPayment).isNotNull();
        assertThat(returnedPayment.getData().getScheduledPayment().get(0).getAccountId()).isEqualTo(accountId);
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(url);
    }

    private String accountScheduledPaymentsUrl(String accountId) {
        String url = BASE_URL + port + ACCOUNT_SCHEDULED_PAYMENTS_URI;
        return url.replace("{AccountId}", accountId);
    }

    private String scheduledPaymentsUrl() {
        return BASE_URL + port + SCHEDULED_PAYMENTS_URI;
    }
}