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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.v4.accounts.scheduledpayments;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRScheduledPayment;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component("V4.0.0FRScheduledPaymentRepository")
public interface FRScheduledPaymentRepository extends MongoRepository<FRScheduledPayment, String>, FRScheduledPaymentRepositoryCustom {

    Page<FRScheduledPayment> findByAccountId(@Param("accountId") String accountId, Pageable pageable);

    Page<FRScheduledPayment> findByAccountIdIn(@Param("accountIds") List<String> accountIds, Pageable pageable);

    Long deleteFRScheduledPaymentV4ByAccountId(@Param("accountId") String accountId);

    @Query("{ 'status' : ?0 }, { 'scheduledPayment.ScheduledPaymentDateTime' : { $lt: ?1 } }")
    List<FRScheduledPayment> findByStatus(@Param("status") FRScheduledPayment.ScheduledPaymentStatus status, @Param("scheduledPayment.ScheduledPaymentDateTime") DateTime maxDateTime);

    Long countByAccountIdIn(Set<String> accountIds);

}
