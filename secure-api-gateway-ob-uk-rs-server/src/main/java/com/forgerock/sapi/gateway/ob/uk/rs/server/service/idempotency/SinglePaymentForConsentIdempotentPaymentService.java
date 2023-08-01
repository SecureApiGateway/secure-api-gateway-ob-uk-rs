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

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.PaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.PaymentSubmissionRepository;

/**
 * This implementation is aimed at Payments which can have at most a single payment per consent (all payments except for VRP)
 *
 * See {@link IdempotentPaymentService} documentation for known limitations of this approach.
 */
public class SinglePaymentForConsentIdempotentPaymentService<T extends PaymentSubmission<R>, R, REPO extends PaymentSubmissionRepository<T>> implements IdempotentPaymentService<T, R> {

    private final REPO paymentRepo;

    public SinglePaymentForConsentIdempotentPaymentService(REPO paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public Optional<T> findExistingPayment(R frPaymentRequest, String consentId, String apiClientId, String idempotencyKey) throws OBErrorException {
        final Optional<T> existingPayment = paymentRepo.findByConsentId(consentId);
        validateExistingPayment(frPaymentRequest, idempotencyKey, existingPayment);
        return existingPayment;
    }
}
