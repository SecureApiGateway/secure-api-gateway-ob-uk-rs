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

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRDomesticPaymentSubmission;

/**
 * Implementation of {@link DomesticPaymentSubmissionRepositoryExt}
 *
 * NOTE: Spring data has a naming convention, custom implementations of repository interfaces must live in the same
 * package and has a class name matching the interface with "Impl" suffix
 */
public class DomesticPaymentSubmissionRepositoryExtImpl implements DomesticPaymentSubmissionRepositoryExt {

    public static final String CONSENT_ID_FIELD_NAME = "payment.data.consentId";
    private final FindPaymentByConsentIdQuery findPaymentByConsentIdQuery;

    @Autowired
    public DomesticPaymentSubmissionRepositoryExtImpl(MongoTemplate mongoTemplate) {
        this.findPaymentByConsentIdQuery = new FindPaymentByConsentIdQuery(mongoTemplate,
                CONSENT_ID_FIELD_NAME, FRDomesticPaymentSubmission.class);
    }

    @Override
    public Optional<FRDomesticPaymentSubmission> findByConsentId(String consentId) {
        return findPaymentByConsentIdQuery.findByConsentId(consentId);
    }
}
