/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.account;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccountWithBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCashBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances.FRBalanceRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.backoffice.api.account.AccountsApi;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Controller
@Slf4j
public class AccountsApiController implements AccountsApi {
    private final FRAccountRepository accountsRepository;
    private final FRBalanceRepository balanceRepository;

    @Autowired
    public AccountsApiController(FRAccountRepository accountsRepository, FRBalanceRepository balanceRepository) {
        this.accountsRepository = accountsRepository;
        this.balanceRepository = balanceRepository;
    }

    @Override
    public ResponseEntity<List<FRAccountWithBalance>> getUserAccountsWithBalance(String userId, boolean withBalance) {
        log.info("Read all accounts for user ID '{}', with Balances: {}", userId, withBalance);
        Collection<FRAccount> accountsByUserID = accountsRepository.findByUserID(userId);

        if (!withBalance || accountsByUserID.isEmpty()) {
            log.debug("No balances required so returning {} accounts for userId: {}", accountsByUserID.size(), userId);
            return ResponseEntity.ok(accountsByUserID.stream()
                                 .map(account -> new FRAccountWithBalance(toFRAccountDto(account), emptyList()))
                                 .collect(toList()));
        }

        List<String> accountIds = accountsByUserID.stream()
                .map(FRAccount::getId)
                .collect(toList());
        Collection<FRBalance> balances = balanceRepository.findByAccountIdIn(accountIds);

        Map<String, List<FRCashBalance>> balancesByAccountId = balances.stream()
                .collect(Collectors.groupingBy(
                        FRBalance::getAccountId,
                        HashMap::new,
                        mapping(FRBalance::getBalance, Collectors.toCollection(ArrayList::new))));
        log.debug("Balances by accountId: {}", balancesByAccountId);

        return ResponseEntity.ok(accountsByUserID.stream()
                .map(account -> new FRAccountWithBalance(toFRAccountDto(account), balancesByAccountId.get(account.getId())))
                .collect(toList())
        );

    }

    @Override
    public ResponseEntity<FRAccountWithBalance> getAccountWithBalanceByIdentifiers(
            String userId,
            String accountIdentifierIdentification,
            String accountIdentifierSchemeName
    ) {
        log.info(
                "Read all account identifier for user ID '{}', identification {} and schema name {}",
                userId, accountIdentifierIdentification, accountIdentifierSchemeName
        );

        final FRAccount account = accountsRepository.findByUserIdAndAccountIdentifiers(userId,
                                                                                       accountIdentifierIdentification,
                                                                                       accountIdentifierSchemeName);
        if (account != null) {
            final List<FRCashBalance> balances = balanceRepository.findByAccountId(account.getId())
                                                                  .stream()
                                                                  .map(FRBalance::getBalance)
                                                                  .toList();
            return ResponseEntity.ok(new FRAccountWithBalance(toFRAccountDto(account), balances));
        }
        return ResponseEntity.ok(null);
    }


    private com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccount toFRAccountDto(FRAccount account) {
        return account == null ? null : com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRAccount.builder()
                .id(account.getId())
                .userId(account.getUserID())
                .account(account.getAccount())
                .latestStatementId(account.getLatestStatementId())
                .created(new DateTime(account.getCreated()))
                .updated(new DateTime(account.getUpdated()))
                .build();
    }
}
