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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_1_3.directdebits;

import com.forgerock.securebanking.openbanking.uk.rs.common.util.AccountDataInternalIdFilter;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.PaginationUtil;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRDirectDebit;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.directdebits.FRDirectDebitRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadDirectDebit2;
import uk.org.openbanking.datamodel.account.OBReadDirectDebit2Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRDirectDebitConverter.toOBReadDirectDebit2DataDirectDebit;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;

@Controller("DirectDebitsApiV3.1.3")
@Slf4j
public class DirectDebitsApiController implements DirectDebitsApi {

    private final int pageLimitDirectDebits;

    private final FRDirectDebitRepository frDirectDebitRepository;

    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    public DirectDebitsApiController(@Value("${rs.page.default.direct-debits.size:10}") int pageLimitDirectDebits,
                                     FRDirectDebitRepository frDirectDebitRepository,
                                     AccountDataInternalIdFilter accountDataInternalIdFilter) {
        this.pageLimitDirectDebits = pageLimitDirectDebits;
        this.frDirectDebitRepository = frDirectDebitRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
    }

    @Override
    public ResponseEntity<OBReadDirectDebit2> getAccountDirectDebits(String accountId,
                                                                     int page,
                                                                     String authorization,
                                                                     DateTime xFapiAuthDate,
                                                                     String xFapiCustomerIpAddress,
                                                                     String xFapiInteractionId,
                                                                     String xCustomerUserAgent,
                                                                     List<OBExternalPermissions1Code> permissions,
                                                                     String httpUrl) {
        log.info("Read direct debits for account  {} with minimumPermissions {}", accountId, permissions);

        Page<FRDirectDebit> directDebits = frDirectDebitRepository.byAccountIdWithPermissions(accountId, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, pageLimitDirectDebits));
        return packageResponse(page, httpUrl, directDebits);
    }

    @Override
    public ResponseEntity<OBReadDirectDebit2> getDirectDebits(int page,
                                                              String authorization,
                                                              DateTime xFapiAuthDate,
                                                              String xFapiCustomerIpAddress,
                                                              String xFapiInteractionId,
                                                              String xCustomerUserAgent,
                                                              List<String> accountIds,
                                                              List<OBExternalPermissions1Code> permissions,
                                                              String httpUrl) {
        log.info("DirectDebits fron account ids {} ", accountIds);

        Page<FRDirectDebit> directDebits = frDirectDebitRepository.byAccountIdInWithPermissions(accountIds, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, pageLimitDirectDebits));
        return packageResponse(page, httpUrl, directDebits);
    }

    private ResponseEntity<OBReadDirectDebit2> packageResponse(int page, String httpUrl, Page<FRDirectDebit> directDebits) {
        int totalPages = directDebits.getTotalPages();

        return ResponseEntity.ok(new OBReadDirectDebit2()
                .data(new OBReadDirectDebit2Data().directDebit(directDebits.getContent()
                        .stream()
                        .map(dd -> toOBReadDirectDebit2DataDirectDebit(dd.getDirectDebit()))
                        .map(accountDataInternalIdFilter::apply)
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }
}
