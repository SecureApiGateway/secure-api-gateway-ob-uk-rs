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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_0.accounts;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRFinancialAccountConverter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_0.accounts.AccountsApi;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBAccount2;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadAccount2;
import uk.org.openbanking.datamodel.account.OBReadAccount2Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRFinancialAccountConverter.toOBAccount2;
import static java.util.Collections.singletonList;

@Controller("AccountsApiV3.0")
@Slf4j
public class AccountsApiController implements AccountsApi {

    private final FRAccountRepository frAccountRepository;

    public AccountsApiController(FRAccountRepository frAccountRepository) {
        this.frAccountRepository = frAccountRepository;
    }

    @Override
    public ResponseEntity<OBReadAccount2> getAccount(String accountId,
                                                     String authorization,
                                                     DateTime xFapiCustomerLastLoggedTime,
                                                     String xFapiCustomerIpAddress,
                                                     String xFapiInteractionId,
                                                     String xCustomerUserAgent,
                                                     List<OBExternalPermissions1Code> permissions,
                                                     String httpUrl
    ) {
        log.info("Read account {} with permission {}", accountId, permissions);
        FRAccount response = frAccountRepository.byAccountId(accountId, toFRExternalPermissionsCodeList(permissions));
        List<OBAccount2> obAccount2s = singletonList(toOBAccount2(response.getAccount()));

        return ResponseEntity.ok(new OBReadAccount2()
                .data(new OBReadAccount2Data().account(obAccount2s))
                .links(PaginationUtil.generateLinksOnePager(httpUrl))
                .meta(PaginationUtil.generateMetaData(1)));
    }

    @Override
    public ResponseEntity<OBReadAccount2> getAccounts(String page,
                                                      String authorization,
                                                      DateTime xFapiCustomerLastLoggedTime,
                                                      String xFapiCustomerIpAddress,
                                                      String xFapiInteractionId,
                                                      String xCustomerUserAgent,
                                                      List<String> accountIds,
                                                      List<OBExternalPermissions1Code> permissions,
                                                      String httpUrl
    ) {
        log.info("Accounts from account ids {}", accountIds);

        List<OBAccount2> accounts = frAccountRepository.byAccountIds(accountIds, toFRExternalPermissionsCodeList(permissions))
                .stream()
                .map(FRAccount::getAccount)
                .map(FRFinancialAccountConverter::toOBAccount2)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new OBReadAccount2()
                .data(new OBReadAccount2Data().account(accounts))
                .links(PaginationUtil.generateLinksOnePager(httpUrl))
                .meta(PaginationUtil.generateMetaData(1)));
    }
}
