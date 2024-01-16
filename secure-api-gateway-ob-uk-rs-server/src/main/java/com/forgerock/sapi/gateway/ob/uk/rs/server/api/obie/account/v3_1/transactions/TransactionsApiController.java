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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1.transactions;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRTransactionConverter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRTransaction;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.transactions.FRTransactionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1.transactions.TransactionsApi;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadTransaction4;
import uk.org.openbanking.datamodel.account.OBReadTransaction4Data;
import uk.org.openbanking.datamodel.account.OBTransaction4;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;

@Controller("TransactionsApiV3.1")
@Slf4j
public class TransactionsApiController implements TransactionsApi {

    private final FRTransactionRepository frTransactionRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;
    @Value("${rs.page.default.transaction.size:120}")
    private int PAGE_LIMIT_TRANSACTIONS;

    public TransactionsApiController(FRTransactionRepository frTransactionRepository,
                                     AccountDataInternalIdFilter accountDataInternalIdFilter) {
        this.frTransactionRepository = frTransactionRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
    }

    @Override
    public ResponseEntity<OBReadTransaction4> getAccountTransactions(String accountId,
                                                                     int page,
                                                                     String authorization,
                                                                     DateTime fromBookingDateTime,
                                                                     DateTime toBookingDateTime,
                                                                     DateTime xFapiCustomerLastLoggedTime,
                                                                     String xFapiCustomerIpAddress,
                                                                     String xFapiInteractionId,
                                                                     String xCustomerUserAgent,
                                                                     DateTime firstAvailableDate,
                                                                     DateTime lastAvailableDate,
                                                                     List<OBExternalPermissions1Code> permissions,
                                                                     String httpUrl
    ) {
        log.info("Read transactions for account  {} with minimumPermissions {}", accountId, permissions);
        log.debug("transactionStore request transactionFrom {} transactionTo {} ", fromBookingDateTime, toBookingDateTime);

        if (toBookingDateTime == null) {
            toBookingDateTime = DateTime.now();
        }
        if (fromBookingDateTime == null) {
            fromBookingDateTime = toBookingDateTime.minusYears(100);
        }

        Page<FRTransaction> response = frTransactionRepository.byAccountIdAndBookingDateTimeBetweenWithPermissions(accountId,
                fromBookingDateTime, toBookingDateTime, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_TRANSACTIONS, Sort.Direction.ASC, "bookingDateTime"));

        List<OBTransaction4> transactions = response.getContent()
                .stream()
                .map(FRTransaction::getTransaction)
                .map(FRTransactionConverter::toOBTransaction4)
                .map(t -> accountDataInternalIdFilter.apply(t))
                .collect(Collectors.toList());

        //Package the answer
        int totalPages = response.getTotalPages();

        return ResponseEntity.ok(new OBReadTransaction4()
                .data(new OBReadTransaction4Data().transaction(transactions))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages, firstAvailableDate, lastAvailableDate)));
    }

    @Override
    public ResponseEntity<OBReadTransaction4> getTransactions(int page,
                                                              String authorization,
                                                              DateTime xFapiCustomerLastLoggedTime,
                                                              String xFapiCustomerIpAddress,
                                                              String xFapiInteractionId,
                                                              DateTime fromBookingDateTime,
                                                              DateTime toBookingDateTime,
                                                              String xCustomerUserAgent,
                                                              DateTime firstAvailableDate,
                                                              DateTime lastAvailableDate,
                                                              List<String> accountIds,
                                                              List<OBExternalPermissions1Code> permissions,
                                                              String httpUrl
    ) {
        log.info("Reading transations from account ids {}, fromBookingDate {} toBookingDate {} " +
                        "minimumPermissions {} pageNumber {} ", accountIds,
                fromBookingDateTime, toBookingDateTime, permissions, page);

        if (toBookingDateTime == null) {
            toBookingDateTime = DateTime.now();
        }
        if (fromBookingDateTime == null) {
            fromBookingDateTime = toBookingDateTime.minusYears(100);
        }

        Page<FRTransaction> body = frTransactionRepository.byAccountIdInAndBookingDateTimeBetweenWithPermissions(accountIds,
                fromBookingDateTime, toBookingDateTime, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_TRANSACTIONS, Sort.Direction.ASC, "bookingDateTime"));

        List<OBTransaction4> transactions = body.getContent()
                .stream()
                .map(FRTransaction::getTransaction)
                .map(FRTransactionConverter::toOBTransaction4)
                .map(t -> accountDataInternalIdFilter.apply(t))
                .collect(Collectors.toList());

        //Package the answer
        int totalPages = body.getTotalPages();

        return ResponseEntity.ok(new OBReadTransaction4().data(new OBReadTransaction4Data().transaction(transactions))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages, firstAvailableDate, lastAvailableDate)));
    }

    @Override
    public ResponseEntity<OBReadTransaction4> getAccountStatementTransactions(String accountId,
                                                                              int page,
                                                                              String statementId,
                                                                              String authorization,
                                                                              DateTime fromBookingDateTime,
                                                                              DateTime toBookingDateTime,
                                                                              DateTime xFapiCustomerLastLoggedTime,
                                                                              String xFapiCustomerIpAddress,
                                                                              String xFapiInteractionId,
                                                                              String xCustomerUserAgent,
                                                                              DateTime firstAvailableDate,
                                                                              DateTime lastAvailableDate,
                                                                              List<OBExternalPermissions1Code> permissions,
                                                                              String httpUrl
    ) {
        log.info("Reading transations from account id {}, statement id {}, fromBookingDate {} toBookingDate {} " +
                        "minimumPermissions {} pageNumber {} ", accountId, statementId,
                fromBookingDateTime, toBookingDateTime, permissions, page);

        if (toBookingDateTime == null) {
            toBookingDateTime = DateTime.now();
        }
        if (fromBookingDateTime == null) {
            fromBookingDateTime = toBookingDateTime.minusYears(100);
        }

        Page<FRTransaction> response = frTransactionRepository.byAccountIdAndStatementIdAndBookingDateTimeBetweenWithPermissions(accountId, statementId,
                fromBookingDateTime, toBookingDateTime, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_TRANSACTIONS, Sort.Direction.ASC, "bookingDateTime"));

        List<OBTransaction4> transactions = response.getContent()
                .stream()
                .map(FRTransaction::getTransaction)
                .map(FRTransactionConverter::toOBTransaction4)
                .map(t -> accountDataInternalIdFilter.apply(t))
                .collect(Collectors.toList());

        //Package the answer
        int totalPages = response.getTotalPages();

        return ResponseEntity.ok(new OBReadTransaction4().data(new OBReadTransaction4Data().transaction(transactions))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages, firstAvailableDate, lastAvailableDate)));
    }
}
