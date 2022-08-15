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
package com.forgerock.securebanking.openbanking.uk.rs.service.balance;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRBalance;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRBalanceType.INTERIMAVAILABLE;

@Service
@Slf4j
public class FundsAvailabilityService {

    private final BalanceStoreService balanceStoreService;

    public FundsAvailabilityService(BalanceStoreService balanceStoreService) {
        this.balanceStoreService = balanceStoreService;
    }

    /**
     * Determines if an account has the provided amount available.
     *
     * @param accountId The ID of the account in questio
     * @param amount A {@link String} representing a decimal amount (e.g. 10.00).
     * @return {@code true} if the account has the required funds.
     */
    public boolean isFundsAvailable(String accountId, String amount) {
        Preconditions.checkArgument(!StringUtils.isEmpty(accountId), "Account Id cannot be empty");
        Preconditions.checkArgument(!StringUtils.isEmpty(amount), "Amount cannot be empty");

        Optional<FRBalance> balanceIf = balanceStoreService.getBalance(accountId, INTERIMAVAILABLE);

        // Verify account for a balance
        FRBalance balance = balanceIf.orElseThrow(() -> new IllegalStateException("No balance found of type '"
                + INTERIMAVAILABLE + "' for account id '" + accountId + "'"));
        BigDecimal currentBalance = balance.getAmount();
        BigDecimal requestAmount = new BigDecimal(amount);

        log.debug("Check if balance: '{}' from accountId: '{}' is sufficient to cover the amount: '{}'",
                currentBalance.toPlainString(), accountId, amount);
        return (currentBalance.compareTo(requestAmount) >= 0);
    }
}
