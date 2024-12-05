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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static uk.org.openbanking.datamodel.v4.account.ExternalEntryStatus1Code.BOOK;
import static uk.org.openbanking.datamodel.v4.account.OBExternalAccountSubType1Code.CACC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRTransactionData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.model.User;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.services.UserClientService;
import com.forgerock.sapi.gateway.rs.resource.store.api.testsupport.FRCustomerInfoTestHelper;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.account.FRAccountData;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.user.FRUserData;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRTransaction;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.customerinfo.FRCustomerInfoEntity;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances.FRBalanceRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.transactions.FRTransactionRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.customerinfo.FRCustomerInfoRepository;

import uk.org.openbanking.datamodel.v4.account.OBAccount6;
import uk.org.openbanking.datamodel.v4.account.OBBalanceType1Code;
import uk.org.openbanking.datamodel.v4.account.OBCreditDebitCode2;
import uk.org.openbanking.datamodel.v4.account.OBReadBalance1DataBalanceInner;
import uk.org.openbanking.datamodel.v4.account.OBTransaction6;
import uk.org.openbanking.datamodel.v4.account.OBTransactionCashBalance;
import uk.org.openbanking.datamodel.v4.account.OBTransactionCashBalanceAmount;

/**
 * A SpringBoot test for the {@link DataApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {"rs.data.upload.limit.accounts=10", "rs.data.upload.limit.documents=1000", "rs.data.customerInfo.enabled=true"})
@AutoConfigureWebClient(registerRestTemplate = true)
public class DataApiControllerTest {

    private static final String BASE_URL = "http://localhost:";
    private static final String DATA_URI = "/admin/data/user";

    @LocalServerPort
    private int port;

    @Autowired
    private FRAccountRepository frAccountRepository;

    @Autowired
    private FRBalanceRepository frBalanceRepository;

    @Autowired
    private FRCustomerInfoRepository frCustomerInfoRepository;

    @Autowired
    private FRTransactionRepository frTransactionRepository;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private UserClientService userClientService;

    private final static String USER_NAME = "user-name";
    private final static String ACCOUNT_STATUS = "active";

    @BeforeEach
    public void setUp() {
        frAccountRepository.deleteAll();
        frBalanceRepository.deleteAll();
        frCustomerInfoRepository.deleteAll();
        frTransactionRepository.deleteAll();

        // Insert extra test data into the repo to ensure that the controller only exports data belonging to the user
        final String accountId = UUID.randomUUID().toString();
        frTransactionRepository.save(FRTransaction.builder().accountId(accountId).transaction(
                        FRTransactionData.builder().accountId(accountId)
                                .amount(new FRAmount("1.23", "GBP"))
                                .build())
                .build());

        frBalanceRepository.save(FRBalance.builder().accountId(accountId).build());
        frAccountRepository.save(FRAccount.builder().id(accountId).account(FRFinancialAccount.builder().build()).build());
        frCustomerInfoRepository.save(FRCustomerInfoEntity.builder().id(accountId).build());
    }

    @Test
    public void shouldCreateNewData() throws Exception {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString()).accountTypeCode(CACC);
        final int numTransactions = 650;
        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(account, numTransactions,
                new OBReadBalance1DataBalanceInner().type(OBBalanceType1Code.ITAV)));
        FRUserData userData = new FRUserData();
        userData.setAccountDatas(accountDatas);
        userData.setUserName(USER_NAME);

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .userName(userData.getUserName())
                .accountStatus(ACCOUNT_STATUS)
                .build();

        userData.setCustomerInfo(
                FRCustomerInfoTestHelper.aValidFRCustomerInfo(user.getId(), user.getUserName())
        );

        // When
        when(userClientService.getUserByName(eq(user.getUserName()))).thenReturn(user);

        ResponseEntity<FRUserData> response = restTemplate.postForEntity(dataUrl(), userData, FRUserData.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        FRCustomerInfo customerInfoResponse = response.getBody().getCustomerInfo();
        validateCustomerInfoResponse(customerInfoResponse, userData.getCustomerInfo());
        final List<FRAccountData> responseAccountData = userData.getAccountDatas();
        assertThat(responseAccountData).hasSize(1);
        final FRAccountData responseAccount = responseAccountData.get(0);
        assertThat(responseAccount.getTransactions()).hasSize(numTransactions);

        when(userClientService.getUserById(eq(user.getId()))).thenReturn(user);

        ResponseEntity<FRUserData> exportedData = restTemplate.exchange(dataUrl() + "?userId=" + user.getId(), GET, HttpEntity.EMPTY, FRUserData.class);
        assertThat(exportedData.getStatusCode()).isEqualTo(HttpStatus.OK);
        final FRUserData exportedUserData = exportedData.getBody();
        assertThat(exportedUserData.getUserId()).isEqualTo(user.getId());
        assertThat(exportedUserData.getUserName()).isEqualTo(user.getUserName());
        final List<FRAccountData> exportedAccountData = exportedUserData.getAccountDatas();
        assertThat(exportedAccountData).hasSize(1);
        final FRAccountData exportedAccount = exportedAccountData.get(0);
        assertThat(exportedAccount.getTransactions()).hasSize(numTransactions);
        assertThat(exportedAccount.getBalances()).hasSize(1);
    }

    @Test
    public void shouldRaiseUserNotFoundRejectCreationData() throws Exception {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(account, 5, new OBReadBalance1DataBalanceInner()));
        FRUserData userData = new FRUserData();
        userData.setAccountDatas(accountDatas);
        userData.setUserName(USER_NAME);
        String errorReason = String.format("User with userName '%s' not found.", userData.getUserName());

        // When
        when(userClientService.getUserByName(anyString())).thenThrow(
                new ExceptionClient(
                        ErrorClient.builder()
                                .errorType(ErrorType.NOT_FOUND)
                                .reason(errorReason)
                                .userName(userData.getUserName())
                                .build(),
                        errorReason
                )
        );
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.postForEntity(dataUrl(), userData, FRUserData.class),
                HttpClientErrorException.class
        );

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldCreateNewDataUsingUpdate() throws Exception {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        FRAccount savedAccount = frAccountRepository.save(FRAccount.builder()
                .id(account.getAccountId())
                .userID(UUID.randomUUID().toString())
                .build());

        List<FRAccountData> accountDataList = List.of(accountDataWithBalances(account,12,
                new OBReadBalance1DataBalanceInner().type(OBBalanceType1Code.ITAV)));
        FRUserData userData = new FRUserData();
        userData.setAccountDatas(accountDataList);
        userData.setUserName(savedAccount.getUserID());
        User user = User.builder()
                .id(savedAccount.getUserID())
                .userName(userData.getUserName())
                .accountStatus(ACCOUNT_STATUS)
                .build();

        userData.setCustomerInfo(
                FRCustomerInfoTestHelper.aValidFRCustomerInfo(user.getId(), user.getUserName())
        );

        // When
        when(userClientService.getUserByName(eq(user.getUserName()))).thenReturn(user);
        when(userClientService.getUserById(eq(user.getId()))).thenReturn(user);

        // When
        ResponseEntity<FRUserData> response = restTemplate.exchange(dataUrl(), PUT, new HttpEntity<>(userData), FRUserData.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        validateCustomerInfoResponse(response.getBody().getCustomerInfo(), userData.getCustomerInfo());
    }

    @Test
    public void shouldRaiseUserNotFoundUsingUpdate() throws Exception {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(account, 15, new OBReadBalance1DataBalanceInner()));
        FRUserData userData = new FRUserData();
        userData.setAccountDatas(accountDatas);
        userData.setUserName(USER_NAME);
        String errorReason = String.format("User with userName '%s' not found.", userData.getUserName());

        // When
        when(userClientService.getUserByName(anyString())).thenThrow(
                new ExceptionClient(
                        ErrorClient.builder()
                                .errorType(ErrorType.NOT_FOUND)
                                .reason(errorReason)
                                .userName(userData.getUserName())
                                .build(),
                        errorReason
                )
        );
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.exchange(dataUrl(), PUT, new HttpEntity<>(userData), FRUserData.class),
                HttpClientErrorException.class
        );

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailToCreateNewDataUsingUpdateGivenPayloadTooLarge() throws Exception {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        FRAccount savedAccount = frAccountRepository.save(FRAccount.builder()
                .id(account.getAccountId())
                .userID(UUID.randomUUID().toString())
                .build());

        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(
                account, 1001,
                new OBReadBalance1DataBalanceInner().type(OBBalanceType1Code.ITAV)));
        FRUserData userData = new FRUserData();
        userData.setAccountDatas(accountDatas);
        userData.setUserName(savedAccount.getUserID());
        User user = User.builder()
                .id(savedAccount.getUserID())
                .userName(userData.getUserName())
                .accountStatus(ACCOUNT_STATUS)
                .build();

        userData.setCustomerInfo(
                FRCustomerInfoTestHelper.aValidFRCustomerInfo(user.getId(), user.getUserName())
        );
        // When
        when(userClientService.getUserByName(anyString())).thenReturn(user);
        // When
        HttpClientErrorException exception = catchThrowableOfType(() ->
                        restTemplate.exchange(dataUrl(), PUT, new HttpEntity<>(userData), FRUserData.class)
                , HttpClientErrorException.class
        );

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
    }

    private FRAccountData accountDataWithBalances(OBAccount6 account, int numTransactions, OBReadBalance1DataBalanceInner... obCashBalance1s) {
        FRAccountData accountData = new FRAccountData();
        accountData.setAccount(account);
        //accountData.setAccount(account.accountTypeCode(CACC));
        accountData.setTransactions(generateTransactions(numTransactions));
        accountData.setBalances(Arrays.asList(obCashBalance1s));

        return accountData;
    }

    private List<OBTransaction6> generateTransactions(int numTransactions) {
        final List<OBTransaction6> transactions = new ArrayList<>(numTransactions);
        for (int i = 0; i < numTransactions; i++) {
            OBTransaction6 transaction = new OBTransaction6();
            transaction.status(BOOK);
            transaction.balance(new OBTransactionCashBalance(OBCreditDebitCode2.CREDIT, OBBalanceType1Code.CLBD, new OBTransactionCashBalanceAmount(i + ".00", "GBP")))
                    .transactionReference("Test Payment: " + i);
            transactions.add(transaction);
        }
        return transactions;
    }

    private String dataUrl() {
        return BASE_URL + port + DATA_URI;
    }

    private void validateCustomerInfoResponse(FRCustomerInfo customerInfoResponse, FRCustomerInfo customerInfoRequest) {
        assertThat(customerInfoResponse.getUserID()).isEqualTo(customerInfoRequest.getUserID());
        assertThat(customerInfoResponse.getUserName()).isEqualTo(customerInfoRequest.getUserName());
        assertThat(customerInfoResponse.getFamilyName()).isEqualTo(customerInfoRequest.getFamilyName());
        assertThat(customerInfoResponse.getGivenName()).isEqualTo(customerInfoRequest.getGivenName());
        assertThat(customerInfoResponse.getInitials()).isEqualTo(customerInfoRequest.getInitials());
        assertThat(customerInfoResponse.getPartyId()).isEqualTo(customerInfoRequest.getPartyId());
        assertThat(customerInfoResponse.getPhoneNumber()).isEqualTo(customerInfoRequest.getPhoneNumber());
        assertThat(customerInfoResponse.getEmail()).isEqualTo(customerInfoRequest.getEmail());
        assertThat(customerInfoResponse.getPhoneNumber()).isEqualTo(customerInfoRequest.getPhoneNumber());
        assertThat(customerInfoResponse.getBirthdate()).isEqualTo(customerInfoRequest.getBirthdate());
        assertThat(customerInfoResponse.getAddress()).isEqualTo(customerInfoRequest.getAddress());
    }
}