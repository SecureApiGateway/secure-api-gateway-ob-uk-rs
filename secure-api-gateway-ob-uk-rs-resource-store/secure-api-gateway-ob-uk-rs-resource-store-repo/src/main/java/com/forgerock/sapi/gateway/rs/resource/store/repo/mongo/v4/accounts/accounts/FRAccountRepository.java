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

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Collection;
@Component("V4.0.0FRAccountRepository")
public interface FRAccountRepository extends MongoRepository<FRAccount, String>, FRAccountRepositoryCustom {

    Collection<FRAccount> findByUserID(@Param("userID") String userID);

    @Query("{ 'userID': ?0 , 'account.accounts.identification' : ?1, 'account.accounts.schemeName': ?2}")
    FRAccount findByUserIdAndAccountIdentifiers(
            @Param("userID") String userID,
            @Param("account.accounts.identification") String accountIdentifierIdentification,
            @Param("account.accounts.schemeName") String accountIdentifierSchemeName
    );
}
