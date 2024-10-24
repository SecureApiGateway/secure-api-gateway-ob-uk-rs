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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.v4.accounts.statements;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("V4.0.0FRStatementRepositoryImpl")
public class FRStatementRepositoryImpl implements FRStatementRepositoryCustom {

    @Autowired
    @Lazy
    private FRStatementRepository statement1Repository;

    @Override
    public Page<FRStatement> byAccountIdWithPermissions(
            String accountId,
            Date fromStatementDateTime,
            Date toStatementDateTime,
            List<FRExternalPermissionsCode> permissions, Pageable pageable) {

        if (fromStatementDateTime == null && toStatementDateTime == null) {
            return filter(statement1Repository.findByAccountId(accountId, pageable), permissions);
        } else {
            return filter(statement1Repository.findByAccountIdAndStartDateTimeBetweenAndEndDateTimeBetween(accountId, fromStatementDateTime, toStatementDateTime, fromStatementDateTime, toStatementDateTime, pageable), permissions);
        }
    }

    @Override
    public List<FRStatement> byAccountIdAndStatementIdWithPermissions(
            String accountId,
            String statementId,
            List<FRExternalPermissionsCode> permissions) {

        return filter(statement1Repository.findByAccountIdAndId(accountId, statementId), permissions);
    }

    @Override
    public Page<FRStatement> byAccountIdInWithPermissions(List<String> accountIds, List<FRExternalPermissionsCode> permissions, Pageable pageable) {
        return filter(statement1Repository.findByAccountIdIn(accountIds, pageable), permissions);
    }

    private Page<FRStatement> filter(Page<FRStatement> statements, List<FRExternalPermissionsCode> permissions) {
        for (FRExternalPermissionsCode permission : permissions) {
            for (FRStatement statement : statements) {
                switch (permission) {
                    case READSTATEMENTSBASIC:
                        statement.getStatement().setStatementAmounts(null);
                        break;
                }
            }
        }
        return statements;
    }

    private List<FRStatement> filter(List<FRStatement> statements, List<FRExternalPermissionsCode> permissions) {
        for (FRExternalPermissionsCode permission : permissions) {
            for (FRStatement statement : statements) {
                switch (permission) {
                    case READSTATEMENTSBASIC:
                        statement.getStatement().setStatementAmounts(null);
                        break;
                }
            }
        }
        return statements;
    }
}
