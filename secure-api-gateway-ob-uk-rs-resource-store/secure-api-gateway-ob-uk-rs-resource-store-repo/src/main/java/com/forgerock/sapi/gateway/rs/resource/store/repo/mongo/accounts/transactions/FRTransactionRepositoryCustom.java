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

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.ApiConstants;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRTransaction;

public interface FRTransactionRepositoryCustom {

    Page<FRTransaction> byAccountIdAndBookingDateTimeBetweenWithPermissions(
            @Param("accountId") String accountId,
            @Param(ApiConstants.ParametersFieldName.FROM_BOOKING_DATE_TIME) Date  fromBookingDateTime,
            @Param(ApiConstants.ParametersFieldName.TO_BOOKING_DATE_TIME) Date toBookingDateTime,
            @Param("permissions") List<FRExternalPermissionsCode> permissions,
            Pageable pageable);

    Page<FRTransaction> byAccountIdAndStatementIdAndBookingDateTimeBetweenWithPermissions(
            @Param("accountId") String accountId,
            @Param("statementId") String statementId,
            @Param(ApiConstants.ParametersFieldName.FROM_BOOKING_DATE_TIME) Date fromBookingDateTime,
            @Param(ApiConstants.ParametersFieldName.TO_BOOKING_DATE_TIME) Date toBookingDateTime,
            @Param("permissions") List<FRExternalPermissionsCode> permissions,
            Pageable pageable);


    Page<FRTransaction> byAccountIdInWithPermissions(List<String> accountIds,
                                                     List<FRExternalPermissionsCode> permissions,
                                                     Pageable pageable);

    Page<FRTransaction> byAccountIdInAndBookingDateTimeBetweenWithPermissions(List<String> accountIds,
                                                                              Date fromBookingDateTime,
                                                                              Date toBookingDateTime,
                                                                              List<FRExternalPermissionsCode> permissions,
                                                                              Pageable pageable);
}
