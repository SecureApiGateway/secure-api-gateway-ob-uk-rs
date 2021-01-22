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
package com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.transactions;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRTransaction;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRExternalPermissionsCode;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.ApiConstants.BOOKED_TIME_DATE_FORMAT;
import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.ApiConstants.ParametersFieldName.FROM_BOOKING_DATE_TIME;
import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.ApiConstants.ParametersFieldName.TO_BOOKING_DATE_TIME;

public interface FRTransactionRepositoryCustom {

    Page<FRTransaction> byAccountIdAndBookingDateTimeBetweenWithPermissions(
            @Param("accountId") String accountId,
            @Param(FROM_BOOKING_DATE_TIME) @DateTimeFormat(pattern = BOOKED_TIME_DATE_FORMAT) DateTime fromBookingDateTime,
            @Param(TO_BOOKING_DATE_TIME) @DateTimeFormat(pattern = BOOKED_TIME_DATE_FORMAT) DateTime
                    toBookingDateTime,
            @Param("permissions") List<FRExternalPermissionsCode> permissions,
            Pageable pageable);

    Page<FRTransaction> byAccountIdAndStatementIdAndBookingDateTimeBetweenWithPermissions(
            @Param("accountId") String accountId,
            @Param("statementId") String statementId,
            @Param(FROM_BOOKING_DATE_TIME) @DateTimeFormat(pattern = BOOKED_TIME_DATE_FORMAT) DateTime fromBookingDateTime,
            @Param(TO_BOOKING_DATE_TIME) @DateTimeFormat(pattern = BOOKED_TIME_DATE_FORMAT) DateTime
                    toBookingDateTime,
            @Param("permissions") List<FRExternalPermissionsCode> permissions,
            Pageable pageable);


    Page<FRTransaction> byAccountIdInWithPermissions(List<String> accountIds, List<FRExternalPermissionsCode>
            permissions, Pageable pageable);

    Page<FRTransaction> byAccountIdInAndBookingDateTimeBetweenWithPermissions(List<String> accountIds,
                                                                              DateTime fromBookingDateTime, DateTime toBookingDateTime, List<FRExternalPermissionsCode> permissions,
                                                                              Pageable pageable);
}
