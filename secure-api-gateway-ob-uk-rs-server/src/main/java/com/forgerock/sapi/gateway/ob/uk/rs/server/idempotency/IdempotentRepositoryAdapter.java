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
package com.forgerock.sapi.gateway.ob.uk.rs.server.idempotency;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.PaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.PaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.IdempotencyValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.idempotency.IdempotentRepositoryAdapter.IdempotentSaveResult.existingPayment;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.idempotency.IdempotentRepositoryAdapter.IdempotentSaveResult.newPayment;
import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType.DOMESTIC_VRP_PAYMENT_CONSENT;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * This class allows reuse of idempotent save logic for payment submissions.
 * <p>
 * It wraps the relevant repository but adds logic to check if this is a valid idempotent request.
 * <ul>
 * <li/> If valid and repeated -> return existing object and do nothing
 * <li/> If valid and not repeated -> save and return new object
 * <li/> If invalid -> throw OBErrorResponseException
 * </ul>
 *
 * @param <T> The type of the {@link PaymentSubmission} (e.g. FRDomesticPaymentSubmission).
 */
@Slf4j
public class IdempotentRepositoryAdapter<T extends PaymentSubmission, R extends PaymentSubmissionRepository<T> & MongoRepository<T, String>> {

    public static class IdempotentSaveResult<T extends PaymentSubmission> {
        final T savedPayment;
        final boolean isNewPayment;

        public static <T extends PaymentSubmission> IdempotentSaveResult newPayment(T savedPayment) {
            return new IdempotentSaveResult(savedPayment, true);
        }

        public static <T extends PaymentSubmission> IdempotentSaveResult existingPayment(T savedPayment) {
            return new IdempotentSaveResult(savedPayment, false);
        }

        private IdempotentSaveResult(T savedPayment, boolean isNewPayment) {
            this.savedPayment = savedPayment;
            this.isNewPayment = isNewPayment;
        }

        public T getSavedPayment() {
            return savedPayment;
        }

        public boolean isNewPayment() {
            return isNewPayment;
        }
    }

    private final R repository;

    public IdempotentRepositoryAdapter(R repository) {
        this.repository = repository;
    }

    public IdempotentSaveResult idempotentSave(T paymentSubmission) throws OBErrorResponseException {

        IntentType intentType = IntentType.identify(paymentSubmission.getConsentId());

        Optional<T> isPaymentSubmission = repository.findByConsentId(paymentSubmission.getConsentId());
        if (isPaymentSubmission.isPresent() && (intentType == null || !intentType.equals(DOMESTIC_VRP_PAYMENT_CONSENT))) {
            log.info("A payment with this consent id '{}' was already found. Checking idempotency key.", isPaymentSubmission.get().getConsentId());
            IdempotencyValidator.validateIdempotencyRequest(paymentSubmission, isPaymentSubmission.get());
            log.info("Idempotent request is valid. Returning [201 CREATED] but take no further action.");
            return existingPayment(isPaymentSubmission.get());
        } else {
            log.info("No payment with this consent id '{}' exists. Proceed to create it.", paymentSubmission.getConsentId());
            log.debug("Saving new payment submission: {}", paymentSubmission);
            paymentSubmission = repository.save(paymentSubmission);
            log.info("Created new Payment Submission: {}", paymentSubmission.getId());
            return newPayment(paymentSubmission);
        }
    }
}
