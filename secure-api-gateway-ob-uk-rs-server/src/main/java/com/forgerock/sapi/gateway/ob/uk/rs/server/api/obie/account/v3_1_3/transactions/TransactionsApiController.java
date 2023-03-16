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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_3.transactions;

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_3.transactions.TransactionsApi;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadTransaction5;

import java.util.List;

@Controller("TransactionsApiV3.1.3")
public class TransactionsApiController implements TransactionsApi {

    private final com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_2.transactions.TransactionsApiController previousVersionController;

    public TransactionsApiController(
            @Qualifier("TransactionsApiV3.1.2") com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_2.transactions.TransactionsApiController previousVersionController
    ) {
        this.previousVersionController = previousVersionController;
    }

    @Override
    public ResponseEntity<OBReadTransaction5> getAccountTransactions(String accountId,
                                                                     int page,
                                                                     String authorization,
                                                                     DateTime xFapiAuthDate,
                                                                     DateTime fromBookingDateTime,
                                                                     DateTime toBookingDateTime,
                                                                     DateTime firstAvailableDate,
                                                                     DateTime lastAvailableDate,
                                                                     String xFapiCustomerIpAddress,
                                                                     String xFapiInteractionId,
                                                                     String xCustomerUserAgent,
                                                                     List<OBExternalPermissions1Code> permissions,
                                                                     String httpUrl) {
        return previousVersionController.getAccountTransactions(
                accountId,
                page,
                authorization,
                fromBookingDateTime,
                toBookingDateTime,
                xFapiAuthDate,
                xFapiCustomerIpAddress,
                xFapiInteractionId,
                xCustomerUserAgent,
                firstAvailableDate,
                lastAvailableDate,
                permissions,
                httpUrl);
    }

    @Override
    public ResponseEntity<OBReadTransaction5> getAccountStatementTransactions(String statementId,
                                                                              String accountId,
                                                                              int page,
                                                                              String authorization,
                                                                              DateTime xFapiAuthDate,
                                                                              DateTime fromBookingDateTime,
                                                                              DateTime toBookingDateTime,
                                                                              DateTime firstAvailableDate,
                                                                              DateTime lastAvailableDate,
                                                                              String xFapiCustomerIpAddress,
                                                                              String xFapiInteractionId,
                                                                              String xCustomerUserAgent,
                                                                              List<OBExternalPermissions1Code> permissions,
                                                                              String httpUrl) {
        return previousVersionController.getAccountStatementTransactions(
                accountId,
                page,
                statementId,
                authorization,
                fromBookingDateTime,
                toBookingDateTime,
                xFapiAuthDate,
                xFapiCustomerIpAddress,
                xFapiInteractionId,
                xCustomerUserAgent,
                firstAvailableDate,
                lastAvailableDate,
                permissions,
                httpUrl);
    }

    @Override
    public ResponseEntity<OBReadTransaction5> getTransactions(int page,
                                                              String authorization,
                                                              DateTime xFapiAuthDate,
                                                              DateTime fromBookingDateTime,
                                                              DateTime toBookingDateTime,
                                                              DateTime firstAvailableDate,
                                                              DateTime lastAvailableDate,
                                                              String xFapiCustomerIpAddress,
                                                              String xFapiInteractionId,
                                                              String xCustomerUserAgent,
                                                              List<String> accountIds,
                                                              List<OBExternalPermissions1Code> permissions,
                                                              String httpUrl) {
        return previousVersionController.getTransactions(
                page,
                authorization,
                xFapiAuthDate,
                xFapiCustomerIpAddress,
                xFapiInteractionId,
                fromBookingDateTime,
                toBookingDateTime,
                xCustomerUserAgent,
                firstAvailableDate,
                lastAvailableDate,
                accountIds,
                permissions,
                httpUrl);
    }
}
