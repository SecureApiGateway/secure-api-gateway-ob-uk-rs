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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.PaymentSubmission;

/**
 * Reusable query implementation which finds a PaymentSubmission by its ConsentId
 * @param <T>
 */
public class FindPaymentByConsentIdQuery<T extends PaymentSubmission> {

    private final MongoTemplate mongoTemplate;
    private final String consentIdFieldName;

    private final Class<T> paymentSubmissionClass;

    @Autowired
    public FindPaymentByConsentIdQuery(MongoTemplate mongoTemplate, String consentIdFieldName, Class<T> paymentSubmissionClass) {
        this.mongoTemplate = mongoTemplate;
        this.consentIdFieldName = consentIdFieldName;
        this.paymentSubmissionClass = paymentSubmissionClass;
    }

    public Optional<T> findByConsentId(String consentId) {
        final T paymentResult = mongoTemplate.findOne(new Query(Criteria.where(consentIdFieldName).is(consentId)),
                                                      paymentSubmissionClass);

        return Optional.ofNullable(paymentResult);
    }
}
