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
package com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments;

import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRDomesticVrpPaymentSubmission;


public interface DomesticVrpPaymentSubmissionRepository extends MongoRepository<FRDomesticVrpPaymentSubmission, String> {

    /**
     * Find if a payment already exists with the same idempontencyKey within the expiration window.
     * <p>
     * The keys are scoped to particular apiClientId.
     *
     * @param apiClientId    the id of the API Client making the request
     * @param idempotencyKey the idempotency key to find
     * @param currentTime    the current time to compare against the idempotencyKeyExpiration field
     * @return Optional<FRDomesticVrpPaymentSubmission> a payment if one exists or an empty Optional.
     */
    @Query("{ 'apiClientId': ?0, 'idempotencyKey' : ?1, 'idempotencyKeyExpiration': {$gt: ?2 } }")
    Optional<FRDomesticVrpPaymentSubmission> findByIdempotencyData(String apiClientId, String idempotencyKey, DateTime currentTime);

}
