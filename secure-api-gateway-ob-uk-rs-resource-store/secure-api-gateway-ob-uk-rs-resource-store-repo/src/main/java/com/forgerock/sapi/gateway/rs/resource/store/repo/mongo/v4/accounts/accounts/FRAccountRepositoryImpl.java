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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.v4.accounts.accounts;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRAccount;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import uk.org.openbanking.datamodel.v3.common.OBExternalAccountIdentification4Code;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Repository("V4.0.0FRAccountRepositoryImpl")
public class FRAccountRepositoryImpl implements FRAccountRepositoryCustom {
    @Autowired
    @Lazy
    private FRAccountRepository accountsRepository;

    private MongoTemplate mongoTemplate;

    public FRAccountRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Collection<FRAccount> byUserIDWithPermissions(String userID, List<FRExternalPermissionsCode> permissions, Pageable
            pageable) {
        Collection<FRAccount> accounts = accountsRepository.findByUserID(userID);
        try {
            for (FRAccount account : accounts) {
                filterAccount(account, permissions);
            }
            return accounts;
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public FRAccount byAccountId(String accountId, List<FRExternalPermissionsCode> permissions) {
        Optional<FRAccount> isAccount = accountsRepository.findById(accountId);
        if (!isAccount.isPresent()) {
            return null;
        }
        FRAccount account = isAccount.get();
        try {
            filterAccount(account, permissions);
            return account;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public FRAccount byAccountId(String accountId) {
        Optional<FRAccount> isAccount = accountsRepository.findById(accountId);
        if (!isAccount.isPresent()) {
            return null;
        }
        return isAccount.get();
    }

    @Override
    public List<FRAccount> byAccountIds(List<String> accountIds, List<FRExternalPermissionsCode> permissions) {
        Iterable<FRAccount> accounts = accountsRepository.findAllById(accountIds);
        try {
            for (FRAccount account : accounts) {
                filterAccount(account, permissions);
            }
            return StreamSupport.stream(accounts.spliterator(), false).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    private void filterAccount(FRAccount account, List<FRExternalPermissionsCode> permissions) {
        if(permissions.contains(FRExternalPermissionsCode.READACCOUNTSDETAIL)){
            if (!CollectionUtils.isEmpty(account.getAccount().getAccounts())) {
                for (FRAccountIdentifier subAccount : account.getAccount().getAccounts()) {
                    if (!permissions.contains(FRExternalPermissionsCode.READPAN)
                            && OBExternalAccountIdentification4Code.PAN.toString().equals(subAccount.getSchemeName())) {
                        subAccount.setIdentification("xxx");
                    }
                }
            }
        } else if (permissions.contains(FRExternalPermissionsCode.READACCOUNTSBASIC)){
            account.getAccount().setAccounts(null);
            account.getAccount().setServicer(null);
        }
    }


    @Override
    public List<String> getUserIds(DateTime from, DateTime to) {
        Aggregation aggregation = newAggregation(
                Aggregation.match(
                        Criteria.where("created").gt(from)),
                Aggregation.match(
                        Criteria.where("created").lt(to)),
                Aggregation.group("userID")
                        .first("userID").as("userID"),
                project("userID")
        );
        //Convert the aggregation result into a List
        AggregationResults<UserIds> groupResults
                = mongoTemplate.aggregate(aggregation, FRAccount.class, UserIds.class);
        return groupResults.getMappedResults().stream().map(UserIds::getUserID).collect(Collectors.toList());
    }

    @Builder
    @Data
    @EqualsAndHashCode
    public static class UserIds {
        private String userID;

    }
}
