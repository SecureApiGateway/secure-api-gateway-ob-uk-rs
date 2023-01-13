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
package com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.accounts;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRAccount;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRScheduledPayment;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FRAccountRepository extends MongoRepository<FRAccount, String>, FRAccountRepositoryCustom {

    Collection<FRAccount> findByUserID(@Param("userID") String userID);

    @Query("{ 'userID': ?0 , 'account.accounts.name' : ?1, 'account.accounts.identification' : ?2, 'account.accounts.schemeName': ?3}")
    FRAccount findByUserIdAndAccountIdentifiers(
            @Param("userID") String userID,
            @Param("account.accounts.name") String accountIdentifierName,
            @Param("account.accounts.identification") String accountIdentifierIdentification,
            @Param("account.accounts.schemeName") String accountIdentifierSchemeName
    );

    @Query("{ 'account.accounts.name' : ?0, 'account.accounts.identification' : ?1, 'account.accounts.schemeName': ?2}")
    FRAccount findByAccountIdentifiers(
            @Param("account.accounts.name") String accountIdentifierName,
            @Param("account.accounts.identification") String accountIdentifierIdentification,
            @Param("account.accounts.schemeName") String accountIdentifierSchemeName
    );
}
