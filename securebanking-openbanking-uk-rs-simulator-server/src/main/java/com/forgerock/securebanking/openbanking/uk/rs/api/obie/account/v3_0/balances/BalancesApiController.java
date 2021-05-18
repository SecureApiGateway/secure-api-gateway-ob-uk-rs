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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_0.balances;

import com.forgerock.securebanking.openbanking.uk.rs.common.util.PaginationUtil;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRBalance;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.balances.FRBalanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadBalance1;
import uk.org.openbanking.datamodel.account.OBReadBalance1Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRCashBalanceConverter.toOBReadBalance1DataBalance;

@Controller("BalancesApiV3.0")
@Slf4j
public class BalancesApiController implements BalancesApi {

    @Value("${rs.page.default.balances.size:10}")
    private int PAGE_LIMIT_BALANCES;

    private final FRBalanceRepository frBalanceRepository;

    public BalancesApiController(FRBalanceRepository frBalanceRepository) {
        this.frBalanceRepository = frBalanceRepository;
    }

    @Override
    public ResponseEntity<OBReadBalance1> getAccountBalances(String accountId,
                                                             int page,
                                                             String xFapiFinancialId,
                                                             String authorization,
                                                             DateTime xFapiCustomerLastLoggedTime,
                                                             String xFapiCustomerIpAddress,
                                                             String xFapiInteractionId,
                                                             String xCustomerUserAgent,
                                                             List<OBExternalPermissions1Code> permissions,
                                                             String httpUrl
    ) {
        log.info("Read balances for account  {} with minimumPermissions {}", accountId, permissions);
        Page<FRBalance> balances = frBalanceRepository.findByAccountId(accountId, PageRequest.of(page, PAGE_LIMIT_BALANCES));
        int totalPage = balances.getTotalPages();

        return ResponseEntity.ok(new OBReadBalance1()
                .data(new OBReadBalance1Data().balance(balances.getContent().stream()
                        .map(b -> toOBReadBalance1DataBalance(b.getBalance()))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPage))
                .meta(PaginationUtil.generateMetaData(totalPage)));
    }

    @Override
    public ResponseEntity<OBReadBalance1> getBalances(String xFapiFinancialId,
                                                      int page,
                                                      String authorization,
                                                      DateTime xFapiCustomerLastLoggedTime,
                                                      String xFapiCustomerIpAddress,
                                                      String xFapiInteractionId,
                                                      String xCustomerUserAgent,
                                                      List<String> accountIds,
                                                      List<OBExternalPermissions1Code> permissions,
                                                      String httpUrl
    ) {
        log.info("Reading balances from account ids {}", accountIds);
        Page<FRBalance> balances = frBalanceRepository.findByAccountIdIn(accountIds, PageRequest.of(page, PAGE_LIMIT_BALANCES));

        int totalPage = balances.getTotalPages();

        return ResponseEntity.ok(new OBReadBalance1()
                .data(new OBReadBalance1Data().balance(balances.getContent().stream()
                        .map(b -> toOBReadBalance1DataBalance(b.getBalance()))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPage))
                .meta(PaginationUtil.generateMetaData(totalPage)));
    }
}
