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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.funds.v3_0;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRBalanceType;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCashBalance;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCreditDebitIndicator;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAmount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRBalance;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.balances.FRBalanceRepository;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.funds.FundsConfirmationRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmation1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationData1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationDataResponse1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationResponse1;
import uk.org.openbanking.datamodel.payment.OBActiveOrHistoricCurrencyAndAmount;

import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.FRAccountTestDataFactory.aValidFRAccount;
import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredFundsHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**OBFundsConfirmationResponse1
 * A SpringBoot test for the {@link FundsConfirmationsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class FundsConfirmationsApiControllerTest {
    private static final String BASE_URL = "http://localhost:";
    private static final String FUND_CONFIRMATIONS_URI = "/open-banking/v3.0/cbpii/funds-confirmations";
    private static final String CURRENCY = "GBP";
    private static final String BALANCE = "10.00";

    @LocalServerPort
    private int port;

    @Autowired
    private FundsConfirmationRepository fundsConfirmationRepository;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRBalanceRepository frBalanceRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String accountId;

    @BeforeEach
    void setup() {
        FRAccount account = aValidFRAccount();
        frAccountRepository.save(account);
        accountId = account.getId();
        FRBalance accountBalance = aValidFRBalance(accountId);
        frBalanceRepository.save(accountBalance);
    }

    @AfterEach
    void removeData() {
        fundsConfirmationRepository.deleteAll();
        frAccountRepository.deleteAll();
        frBalanceRepository.deleteAll();
    }

    @Test
    public void shouldCreateFundsConfirmation() {
        // Given
        OBFundsConfirmation1 fundsConfirmation = aValidOBFundsConfirmation1();
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmation, requiredFundsHttpHeaders(fundsConfirmationsUrl(), accountId));

        // When
        ResponseEntity<OBFundsConfirmationResponse1> response = restTemplate.postForEntity(fundsConfirmationsUrl(), request, OBFundsConfirmationResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        OBFundsConfirmationDataResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getFundsAvailable()).isTrue();
        assertThat(responseData.getReference()).isEqualTo(fundsConfirmation.getData().getReference());
        assertThat(responseData.getInstructedAmount()).isEqualTo(fundsConfirmation.getData().getInstructedAmount());
        assertThat(response.getBody().getMeta()).isNotNull();
        // TODO - enable as part of #54
        //assertThat(response.getBody().getLinks().getFirst().equals(callbacksUrl()));
    }

    @Test
    public void shouldGetFundsConfirmation() {
        // Given
        OBFundsConfirmation1 fundsConfirmation = aValidOBFundsConfirmation1();
        HttpHeaders headers = requiredFundsHttpHeaders(fundsConfirmationsUrl(), accountId);
        HttpEntity<OBFundsConfirmation1> request = new HttpEntity<>(fundsConfirmation, headers);
        ResponseEntity<OBFundsConfirmationResponse1> createResponse = restTemplate.postForEntity(fundsConfirmationsUrl(), request, OBFundsConfirmationResponse1.class);
        String url = fundsConfirmationsIdUrl(createResponse.getBody().getData().getFundsConfirmationId());

        // When
        ResponseEntity<OBFundsConfirmationResponse1> response = restTemplate.exchange(url, GET, new HttpEntity<>(headers), OBFundsConfirmationResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        OBFundsConfirmationDataResponse1 responseData = response.getBody().getData();
        assertThat(responseData.getFundsAvailable()).isTrue();
        assertThat(responseData.getReference()).isEqualTo(fundsConfirmation.getData().getReference());
        assertThat(responseData.getInstructedAmount()).isEqualTo(fundsConfirmation.getData().getInstructedAmount());
        AssertionsForClassTypes.assertThat(response.getBody().getMeta()).isNotNull();
        // TODO - enable as part of #54
        //assertThat(response.getBody().getLinks().getFirst().equals(callbacksUrl()));
    }

    private String fundsConfirmationsUrl() {
        return BASE_URL + port + FUND_CONFIRMATIONS_URI;
    }

    private String fundsConfirmationsIdUrl(String id) {
        return fundsConfirmationsUrl() + "/" + id;
    }

    private OBFundsConfirmation1 aValidOBFundsConfirmation1() {
        return new OBFundsConfirmation1()
                .data(new OBFundsConfirmationData1()
                        .consentId(UUID.randomUUID().toString())
                        .reference("Funds confirmation ref")
                        .instructedAmount(new OBActiveOrHistoricCurrencyAndAmount()
                                .currency(CURRENCY)
                                .amount(BALANCE)
                        ));
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
}