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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.PaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.PaymentSubmissionRepository;

/**
 * See {@link IdempotentPaymentService} documentation for known limitations of this approach.
 */
public class SimpleIdempotentPaymentService<T extends PaymentSubmission<R>, R, REPO extends PaymentSubmissionRepository<T>> implements IdempotentPaymentService<T, R> {

    private final REPO paymentRepo;

    public SimpleIdempotentPaymentService(REPO paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public Optional<T> findExistingPayment(R frPaymentRequest, String consentId, String idempotencyKey) throws OBErrorException {
        final Optional<T> existingPayment = paymentRepo.findByConsentId(consentId);
        if (existingPayment.isPresent()) {
            final T payment = existingPayment.get();
            if (!payment.getIdempotencyKey().equals(idempotencyKey)) {
                throw new OBErrorException(OBRIErrorType.PAYMENT_SUBMISSION_ALREADY_EXISTS, payment.getId());
            } else {
                if (!payment.getPayment().equals(frPaymentRequest)) {
                    throw new OBErrorException(OBRIErrorType.IDEMPOTENCY_KEY_REQUEST_BODY_CHANGED, payment.getId());
                }
            }
        }
        return existingPayment;
    }
}
