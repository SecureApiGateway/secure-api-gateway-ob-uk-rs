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
package com.forgerock.securebanking.openbanking.uk.rs.service.balance;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRCashBalance;
import com.forgerock.securebanking.openbanking.uk.rs.testsupport.FRCashBalanceTestDataFactory;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRBalance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRBalanceType.INTERIMAVAILABLE;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit test for {@link FundsAvailabilityService}.
 */
@ExtendWith(MockitoExtension.class)
public class FundsAvailabilityServiceTest {

    @Mock
    private BalanceStoreService balanceStoreService;

    @InjectMocks
    private FundsAvailabilityService fundsAvailabilityService;

    @Test
    public void shouldBeFundsAvailableGivenSufficientBalance() {
        // Given
        FRCashBalance cashBalance = FRCashBalanceTestDataFactory.aValidFRCashBalance();
        String accountId = cashBalance.getAccountId();
        FRBalance balance = FRBalance.builder()
                .accountId(accountId)
                .balance(cashBalance)
                .build();
        given(balanceStoreService.getBalance(accountId, cashBalance.getType())).willReturn(Optional.of(balance));

        // When
        boolean isFundsAvailable = fundsAvailabilityService.isFundsAvailable(accountId, "9.00");

        // Then
        assertThat(isFundsAvailable).isTrue();
    }

    @Test
    public void shouldNotBeFundsAvailableGivenInsufficientBalance() {
        // Given
        FRCashBalance cashBalance = FRCashBalanceTestDataFactory.aValidFRCashBalance();
        String accountId = cashBalance.getAccountId();
        FRBalance balance = FRBalance.builder()
                .accountId(accountId)
                .balance(cashBalance)
                .build();
        given(balanceStoreService.getBalance(accountId, cashBalance.getType())).willReturn(Optional.of(balance));

        // When
        boolean isFundsAvailable = fundsAvailabilityService.isFundsAvailable(accountId, "11.00");

        // Then
        assertThat(isFundsAvailable).isFalse();
    }

    @Test
    public void shouldFailToVerifyFundsAvailableGivenNoBalanceFound() {
        // Given
        String accountId = "1234";
        given(balanceStoreService.getBalance(accountId, INTERIMAVAILABLE)).willReturn(Optional.empty());

        // When
        IllegalStateException exception = catchThrowableOfType(() ->
                fundsAvailabilityService.isFundsAvailable(accountId, "10.00"), IllegalStateException.class);

        // Then
        assertThat(exception.getMessage()).isEqualTo("No balance found of type 'InterimAvailable' for account id '1234'");
    }
}