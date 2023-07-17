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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin;

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
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances.FRBalanceRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.customerinfo.FRCustomerInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.org.openbanking.datamodel.account.OBAccount6;
import uk.org.openbanking.datamodel.account.OBBalanceType1Code;
import uk.org.openbanking.datamodel.account.OBCashBalance1;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.PUT;

/**
 * A SpringBoot test for the {@link DataApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {"rs.data.upload.limit.accounts=10", "rs.data.upload.limit.documents=1", "rs.data.customer_info.enabled=true"})
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
    }

    @Test
    public void shouldCreateNewData() throws Exception {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(account, new OBCashBalance1()));
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
        when(userClientService.getUserByName(anyString())).thenReturn(user);

        ResponseEntity<FRUserData> response = restTemplate.postForEntity(dataUrl(), userData, FRUserData.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        FRCustomerInfo customerInfoResponse = response.getBody().getCustomerInfo();
        validateCustomerInfoResponse(customerInfoResponse, userData.getCustomerInfo());
    }

    @Test
    public void shouldRaiseUserNotFoundRejectCreationData() throws Exception {
        // Given
        OBAccount6 account = new OBAccount6().accountId(UUID.randomUUID().toString());
        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(account, new OBCashBalance1()));
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

        List<FRAccountData> accountDataList = List.of(accountDataWithBalances(account, new OBCashBalance1()));
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
        when(userClientService.getUserByName(anyString())).thenReturn(user);

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
        List<FRAccountData> accountDatas = List.of(accountDataWithBalances(account, new OBCashBalance1()));
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
                account,
                new OBCashBalance1().type(OBBalanceType1Code.INTERIMAVAILABLE),
                new OBCashBalance1().type(OBBalanceType1Code.INTERIMBOOKED)));
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

    private FRAccountData accountDataWithBalances(OBAccount6 account, OBCashBalance1... obCashBalance1s) {
        FRAccountData accountData = new FRAccountData();
        accountData.setAccount(account);
        accountData.setBalances(Arrays.asList(obCashBalance1s));
        return accountData;
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