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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_1_1.accounts;

import com.forgerock.securebanking.openbanking.uk.rs.common.util.PaginationUtil;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRFinancialAccountConverter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.accounts.FRAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBAccount3;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadAccount3;
import uk.org.openbanking.datamodel.account.OBReadAccount3Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRFinancialAccountConverter.toOBAccount3;

@Controller("AccountsApiV3.1.1")
@Slf4j
public class AccountsApiController implements AccountsApi {

    private final FRAccountRepository frAccountRepository;

    public AccountsApiController(FRAccountRepository frAccountRepository) {
        this.frAccountRepository = frAccountRepository;
    }

    @Override
    public ResponseEntity<OBReadAccount3> getAccount(String accountId,
                                                     String xFapiFinancialId,
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
        final List<OBAccount3> obAccounts = Collections.singletonList(toOBAccount3(response.getAccount()));

        return ResponseEntity.ok(new OBReadAccount3()
                .data(new OBReadAccount3Data().account(obAccounts))
                .links(PaginationUtil.generateLinksOnePager(httpUrl))
                .meta(PaginationUtil.generateMetaData(1)));
    }

    @Override
    public ResponseEntity<OBReadAccount3> getAccounts(String page,
                                                      String xFapiFinancialId,
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

        List<OBAccount3> accounts = frAccountRepository.byAccountIds(accountIds, toFRExternalPermissionsCodeList(permissions))
                .stream()
                .map(FRAccount::getAccount)
                .map(FRFinancialAccountConverter::toOBAccount3)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new OBReadAccount3()
                .data(new OBReadAccount3Data().account(accounts))
                .links(PaginationUtil.generateLinksOnePager(httpUrl))
                .meta(PaginationUtil.generateMetaData(1)));
    }
}
