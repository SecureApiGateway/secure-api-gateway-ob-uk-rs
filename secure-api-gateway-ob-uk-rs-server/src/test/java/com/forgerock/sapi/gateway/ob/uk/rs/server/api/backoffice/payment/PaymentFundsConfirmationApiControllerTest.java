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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRBalanceType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCashBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCreditDebitIndicator;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances.FRBalanceRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.FRAccountTestDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1;

import java.net.URI;

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
    private static final String VERSION = "v3.1.8";

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
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
        frAccountRepository.save(account);
        String accountId = account.getId();
        FRBalance accountBalance = aValidFRBalance(accountId);
        frBalanceRepository.save(accountBalance);
        URI uri = fundsConfirmationUri(accountId, BALANCE);

        // When
        ResponseEntity<OBWriteFundsConfirmationResponse1> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(HttpHeadersTestDataFactory.requiredPaymentFundsConfirmationHttpHeaders(uri.toString())),
                OBWriteFundsConfirmationResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getFundsAvailableResult().getFundsAvailable()).isTrue();
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(uri);
    }

    @Test
    public void shouldNotBeFundsAvailable() {
        // Given
        FRAccount account = FRAccountTestDataFactory.aValidFRAccount();
        frAccountRepository.save(account);
        String accountId = account.getId();
        FRBalance accountBalance = aValidFRBalance(accountId);
        frBalanceRepository.save(accountBalance);
        URI uri = fundsConfirmationUri(accountId, "10.01");

        // When
        ResponseEntity<OBWriteFundsConfirmationResponse1> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(HttpHeadersTestDataFactory.requiredPaymentFundsConfirmationHttpHeaders(uri.toString())),
                OBWriteFundsConfirmationResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getFundsAvailableResult().getFundsAvailable()).isFalse();
        assertThat(response.getBody().getLinks().getSelf()).isEqualTo(uri);
    }

    private FRBalance aValidFRBalance(String accountId) {
        return FRBalance.builder()
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
    }

    private URI fundsConfirmationUri(String accountId, String amount) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + FUNDS_CONFIRMATION_URI + "/" + accountId);
        builder.queryParam("amount", amount);
        builder.queryParam("version", VERSION);
        return builder.build().encode().toUri();
    }
}
