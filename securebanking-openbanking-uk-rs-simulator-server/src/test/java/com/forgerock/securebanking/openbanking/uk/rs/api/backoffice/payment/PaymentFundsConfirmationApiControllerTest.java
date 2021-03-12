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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRBalanceType;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCashBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCreditDebitIndicator;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRFundsConfirmationResponse;
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
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.FRAccountTestDataFactory.aValidFRAccount;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Unit test for {@link PaymentFundsConfirmationApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class PaymentFundsConfirmationApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String FUNDS_CONFIRMATION_URI = "/backoffice/payment-funds-confirmation";
    private static final String CURRENCY = "GBP";
    private static final String BALANCE = "10.00";

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
    public void shouldBeFundsAvailable() {
        // Given
        FRAccount account = aValidFRAccount();
        frAccountRepository.save(account);
        String accountId = account.getId();
        FRBalance accountBalance = aValidFRBalance(accountId);
        frBalanceRepository.save(accountBalance);
        URI uri = fundsConfirmationUri(accountId, BALANCE);

        // When
        ResponseEntity<FRFundsConfirmationResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders()),
                FRFundsConfirmationResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isFundsAvailable()).isTrue();
    }

    @Test
    public void shouldNotBeFundsAvailable() {
        // Given
        FRAccount account = aValidFRAccount();
        frAccountRepository.save(account);
        String accountId = account.getId();
        FRBalance accountBalance = aValidFRBalance(accountId);
        frBalanceRepository.save(accountBalance);
        URI uri = fundsConfirmationUri(accountId, "10.01");

        // When
        ResponseEntity<FRFundsConfirmationResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders()),
                FRFundsConfirmationResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isFundsAvailable()).isFalse();
    }

    private FRBalance aValidFRBalance(String accountId) {
        FRBalance accountBalance = FRBalance.builder()
                .accountId(accountId)
                .balance(FRCashBalance.builder()
                        .accountId(accountId)
                        .creditDebitIndicator(FRCreditDebitIndicator.CREDIT)
                        .type(FRBalanceType.INTERIMAVAILABLE)
                        .amount(FRAmount.builder()
                                .currency(CURRENCY)
                                .amount(BALANCE)
                                .build())
                        .build())
                .build();
        return accountBalance;
    }

    private URI fundsConfirmationUri(String accountId, String amount) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + FUNDS_CONFIRMATION_URI + "/" + accountId);
        builder.queryParam("amount", amount);
        return builder.build().encode().toUri();
    }

    public static HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}