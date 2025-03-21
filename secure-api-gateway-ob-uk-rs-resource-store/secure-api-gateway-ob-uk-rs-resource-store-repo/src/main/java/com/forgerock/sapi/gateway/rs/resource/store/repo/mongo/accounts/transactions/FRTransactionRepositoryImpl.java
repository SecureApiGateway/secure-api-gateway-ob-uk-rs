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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.transactions;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRTransaction;

import uk.org.openbanking.datamodel.v3.account.OBCreditDebitCode0;
import uk.org.openbanking.datamodel.v3.common.OBExternalAccountIdentification4Code;

public class FRTransactionRepositoryImpl implements FRTransactionRepositoryCustom {
    private static final Logger LOGGER = LoggerFactory.getLogger(FRTransactionRepositoryImpl.class);

    @Autowired
    @Lazy
    private FRTransactionRepository transactionRepository;

    @Override
    public Page<FRTransaction> byAccountIdAndBookingDateTimeBetweenWithPermissions(String accountId, Date
            fromBookingDateTime, Date toBookingDateTime, List<FRExternalPermissionsCode> permissions,
                                                                                   Pageable pageable) {

        if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSCREDITS)
                && permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSDEBITS)) {
            return filterTransition(transactionRepository
                    .findByAccountIdAndBookingDateTimeBetween(accountId, fromBookingDateTime, toBookingDateTime, pageable), permissions);
        } else if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSCREDITS)) {
            return filterTransition(transactionRepository
                            .findByAccountIdAndTransactionCreditDebitIndicatorAndBookingDateTimeBetween(accountId,
                                    OBCreditDebitCode0.CREDIT, fromBookingDateTime, toBookingDateTime, pageable),
                    permissions);
        } else if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSDEBITS)) {
            return filterTransition(transactionRepository
                            .findByAccountIdAndTransactionCreditDebitIndicatorAndBookingDateTimeBetween(accountId,
                                    OBCreditDebitCode0.DEBIT, fromBookingDateTime, toBookingDateTime, pageable),
                    permissions);
        } else {
            LOGGER.warn("Need at least one of the following permissions: " +
                    FRExternalPermissionsCode.READTRANSACTIONSCREDITS + " or " + FRExternalPermissionsCode
                    .READTRANSACTIONSDEBITS);
            return new PageImpl<>(Collections.emptyList());
        }
    }

    @Override
    public Page<FRTransaction> byAccountIdAndStatementIdAndBookingDateTimeBetweenWithPermissions(
            String accountId,
            String statementId,
            Date fromBookingDateTime,
            Date toBookingDateTime,
            List<FRExternalPermissionsCode> permissions,
            Pageable pageable) {

        if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSCREDITS)
                && permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSDEBITS)) {
            return filterTransition(transactionRepository
                    .findByAccountIdAndStatementIdsAndBookingDateTimeBetween(accountId, statementId, fromBookingDateTime, toBookingDateTime, pageable), permissions);
        } else if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSCREDITS)) {
            return filterTransition(transactionRepository
                            .findByAccountIdAndStatementIdsAndTransactionCreditDebitIndicatorAndBookingDateTimeBetween(accountId, statementId,
                                    OBCreditDebitCode0.CREDIT, fromBookingDateTime, toBookingDateTime, pageable),
                    permissions);
        } else if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSDEBITS)) {
            return filterTransition(transactionRepository
                            .findByAccountIdAndStatementIdsAndTransactionCreditDebitIndicatorAndBookingDateTimeBetween(accountId, statementId,
                                    OBCreditDebitCode0.DEBIT, fromBookingDateTime, toBookingDateTime, pageable),
                    permissions);
        } else {
            LOGGER.warn("Need at least one of the following permissions: " +
                    FRExternalPermissionsCode.READTRANSACTIONSCREDITS + " or " + FRExternalPermissionsCode
                    .READTRANSACTIONSDEBITS);
            return new PageImpl<>(Collections.emptyList());
        }
    }

    @Override
    public Page<FRTransaction> byAccountIdInWithPermissions(List<String> accountIds, List<FRExternalPermissionsCode>
            permissions, Pageable pageable) {

        if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSCREDITS)
                && permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSDEBITS)) {
            return filterTransition(transactionRepository
                    .findByAccountIdIn(accountIds, pageable), permissions);
        } else if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSCREDITS)) {
            return filterTransition(transactionRepository
                            .findByAccountIdInAndTransactionCreditDebitIndicator(accountIds, OBCreditDebitCode0.CREDIT,
                                    pageable),
                    permissions);
        } else if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSDEBITS)) {
            return filterTransition(transactionRepository
                            .findByAccountIdInAndTransactionCreditDebitIndicator(accountIds, OBCreditDebitCode0.DEBIT,
                                    pageable),
                    permissions);
        } else {
            LOGGER.warn("Need at least one of the following permissions: " +
                    FRExternalPermissionsCode.READTRANSACTIONSCREDITS + " or " + FRExternalPermissionsCode
                    .READTRANSACTIONSDEBITS);
            return new PageImpl<>(Collections.emptyList());
        }
    }


    @Override
    public Page<FRTransaction> byAccountIdInAndBookingDateTimeBetweenWithPermissions(List<String> accountIds,
            Date fromBookingDateTime, Date toBookingDateTime, List<FRExternalPermissionsCode> permissions,
                                                                                     Pageable pageable) {

        if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSCREDITS)
                && permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSDEBITS)) {
            return filterTransition(transactionRepository
                    .findByAccountIdInAndBookingDateTimeBetween(accountIds, fromBookingDateTime, toBookingDateTime,
                            pageable), permissions);
        } else if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSCREDITS)) {
            return filterTransition(transactionRepository
                            .findByAccountIdInAndTransactionCreditDebitIndicatorAndBookingDateTimeBetween(accountIds,
                                    OBCreditDebitCode0.CREDIT, fromBookingDateTime, toBookingDateTime, pageable),
                    permissions);
        } else if (permissions.contains(FRExternalPermissionsCode.READTRANSACTIONSDEBITS)) {
            return filterTransition(transactionRepository
                            .findByAccountIdInAndTransactionCreditDebitIndicatorAndBookingDateTimeBetween(accountIds,
                                    OBCreditDebitCode0.DEBIT, fromBookingDateTime, toBookingDateTime, pageable),
                    permissions);
        } else {
            LOGGER.warn("Need at least one of the following permissions: " +
                    FRExternalPermissionsCode.READTRANSACTIONSCREDITS + " or " + FRExternalPermissionsCode
                    .READTRANSACTIONSDEBITS);
            return new PageImpl<>(Collections.emptyList());
        }
    }

    private Page<FRTransaction> filterTransition(Page<FRTransaction> transactions, List<FRExternalPermissionsCode> permissions) {
        for (FRTransaction transaction : transactions) {
            for (FRExternalPermissionsCode permission : permissions) {
                switch (permission) {
                    case READTRANSACTIONSBASIC:
                        transaction.getTransaction().setTransactionInformation("");
                        transaction.getTransaction().setBalance(null);
                        transaction.getTransaction().setMerchantDetails(null);
                        transaction.getTransaction().setCreditorAgent(null);
                        transaction.getTransaction().setDebtorAgent(null);
                        break;
                }
                if (!permissions.contains(FRExternalPermissionsCode.READPAN)
                        && transaction.getTransaction().getDebtorAccount() != null
                        && OBExternalAccountIdentification4Code.PAN.toString().equals(transaction.getTransaction().getDebtorAccount().getSchemeName())) {
                    transaction.getTransaction().getDebtorAccount().setIdentification("xxx");
                }
                if (!permissions.contains(FRExternalPermissionsCode.READPAN)
                        && transaction.getTransaction().getCreditorAccount() != null
                        && OBExternalAccountIdentification4Code.PAN.toString().equals(transaction.getTransaction().getCreditorAccount().getSchemeName())) {
                    transaction.getTransaction().getCreditorAccount().setIdentification("xxx");
                }
            }
        }
        return transactions;
    }
}
