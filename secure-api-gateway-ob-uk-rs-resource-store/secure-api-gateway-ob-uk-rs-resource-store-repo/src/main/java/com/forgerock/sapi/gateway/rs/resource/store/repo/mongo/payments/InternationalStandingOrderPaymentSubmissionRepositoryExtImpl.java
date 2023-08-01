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

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRInternationalStandingOrderPaymentSubmission;

public class InternationalStandingOrderPaymentSubmissionRepositoryExtImpl implements InternationalStandingOrderPaymentSubmissionRepositoryExt {

    private static final String CONSENT_ID_FIELD_NAME = "standingOrder.data.consentId";
    private final FindPaymentByConsentIdQuery findPaymentByConsentIdQuery;

    @Autowired
    public InternationalStandingOrderPaymentSubmissionRepositoryExtImpl(MongoTemplate mongoTemplate) {
        this.findPaymentByConsentIdQuery = new FindPaymentByConsentIdQuery(mongoTemplate, CONSENT_ID_FIELD_NAME,
                FRInternationalStandingOrderPaymentSubmission.class);
    }

    @Override
    public Optional<FRInternationalStandingOrderPaymentSubmission> findByConsentId(String consentId) {
        return findPaymentByConsentIdQuery.findByConsentId(consentId);
    }
}