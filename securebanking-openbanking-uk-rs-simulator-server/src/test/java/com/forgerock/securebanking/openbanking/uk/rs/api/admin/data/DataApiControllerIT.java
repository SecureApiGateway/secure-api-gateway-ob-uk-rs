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
package com.forgerock.securebanking.openbanking.uk.rs.api.admin.data;

import com.forgerock.securebanking.openbanking.uk.rs.api.admin.data.dto.FRAccountData;
import com.forgerock.securebanking.openbanking.uk.rs.api.admin.data.dto.FRUserData;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.balances.FRBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.org.openbanking.datamodel.account.OBAccount6;
import uk.org.openbanking.datamodel.account.OBBalanceType1Code;
import uk.org.openbanking.datamodel.account.OBCashBalance1;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.PUT;

/**
 * A SpringBoot test for the {@link DataApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {"rs.data.upload.limit.accounts=10", "rs.data.upload.limit.documents=1"})
public class DataApiControllerIT {

    private static final String BASE_URL = "http://localhost:";
    private static final String DATA_URI = "/admin/data/user";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRBalanceRepository frBalanceRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        frAccountRepository.deleteAll();
        frBalanceRepository.deleteAll();
    }

    @Test
    public void shouldCreateNewData() {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(account, new OBCashBalance1()));
        FRUserData userData = new FRUserData();
        userData.setAccountDatas(accountDatas);
        userData.setUserName(UUID.randomUUID().toString());

        // When
        ResponseEntity<FRUserData> response = restTemplate.postForEntity(dataUrl(), userData, FRUserData.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateNewDataUsingUpdate() {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        FRAccount savedAccount = frAccountRepository.save(FRAccount.builder()
                .id(account.getAccountId())
                .userID(UUID.randomUUID().toString())
                .build());

        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(account, new OBCashBalance1()));
        FRUserData userData = new FRUserData();
        userData.setAccountDatas(accountDatas);
        userData.setUserName(savedAccount.getUserID());

        // When
        ResponseEntity<FRUserData> response = restTemplate.exchange(dataUrl(), PUT, new HttpEntity<>(userData), FRUserData.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldFailToCreateNewDataUsingUpdateGivenPayloadTooLarge() {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        FRAccount savedAccount = frAccountRepository.save(FRAccount.builder()
                .id(account.getAccountId())
                .userID(UUID.randomUUID().toString())
                .build());

        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(
                account,
                new OBCashBalance1().type(OBBalanceType1Code.INTERIMAVAILABLE),
                new OBCashBalance1().type(OBBalanceType1Code.INTERIMBOOKED)));
        FRUserData userData = new FRUserData();
        userData.setAccountDatas(accountDatas);
        userData.setUserName(savedAccount.getUserID());

        // When
        ResponseEntity<FRUserData> response = restTemplate.exchange(dataUrl(), PUT, new HttpEntity<>(userData), FRUserData.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
    }

    private FRAccountData accountDataWithBalances(OBAccount6 account, OBCashBalance1... obCashBalance1s) {
        FRAccountData accountData = new FRAccountData();
        accountData.setAccount(account);
        accountData.setBalances(Arrays.asList(obCashBalance1s));
        return accountData;
    }

    private String dataUrl() {
        return BASE_URL + port + DATA_URI;
    }
}