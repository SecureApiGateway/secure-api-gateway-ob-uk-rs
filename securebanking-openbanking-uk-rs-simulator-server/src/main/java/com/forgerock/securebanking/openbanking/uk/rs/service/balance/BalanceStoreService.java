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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRBalanceType;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRBalance;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.balances.FRBalanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for saving and retrieving {@link FRBalance} objects to/from the MongoDB repository.
 */
@Service
@Slf4j
public class BalanceStoreService {

    private final FRBalanceRepository balanceRepository;

    public BalanceStoreService(FRBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    /**
     * Retrieves an {@link FRBalance} based on it's ID and type.
     *
     * @param accountId The ID of the {@link FRBalance} to retrieve.
     * @param balanceType The type of the {@link FRBalance} to retrieve.
     * @return An {@link Optional} containing the {@link FRBalance} if it's found.
     */
    public Optional<FRBalance> getBalance(String accountId, FRBalanceType balanceType) {
        log.debug("Read balances for account {}", accountId);
        return balanceRepository.findByAccountIdAndBalanceType(accountId, balanceType);
    }

    /**
     * Updates a {@link FRBalance} instance in the Repository.
     *
     * @param balance The {@link FRBalance} to update.
     */
    public void updateBalance(FRBalance balance) {
        log.debug("Save balance {}", balance);
        balanceRepository.save(balance);
    }
}
