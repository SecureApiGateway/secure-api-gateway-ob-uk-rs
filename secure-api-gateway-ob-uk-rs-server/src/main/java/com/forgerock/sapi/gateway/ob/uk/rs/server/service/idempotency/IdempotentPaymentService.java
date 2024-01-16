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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency;

import java.util.Optional;


import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.PaymentSubmission;

/**
 * Service which can find an existing payment for a particular idempotencyKey.
 * If a payment already exists, then it is returned to the client as if it had just been created.
 * Otherwise, the calling code may continue to validate the request and create the payment.
 *
 * This provides a simplified payment submission idempotency implementation, that is good enough for a test facility.
 * The TPP is able to test the idempotent behaviour of the API serially, including error conditions such as idempotency
 * key being reused but the request body being changed.
 *
 * There is no idempotency guarantee when idempotent requests are submitted concurrently.
 *
 * A proper implementation should occur in a layer or component before the biz logic, and needs to handle concurrent
 * access.
 *
 * @param <T> PaymentSubmission<R> type - the entity stored in the datastore for a particular payment type
 * @param <R> The FR data-model representation of the OB payment request
 */
public interface IdempotentPaymentService<T extends PaymentSubmission<R>, R> {


    /**
     * Finds any existing payments for the consentId + apiClientId + idempotencyKey combination.
     *
     * This can be used to check if a payment exists before running the biz logic.
     */
    Optional<T> findExistingPayment(R frPaymentRequest, String consentId, String apiClientId, String idempotencyKey) throws OBErrorException;

    /**
     * Saves a new payment into the repository
     */
    T savePayment(T paymentSubmission) throws OBErrorException;

    /**
     * Validates that any existing payment found has an idempotencyKey and OB data-model request object that matches
     * with the newly submitted payment.
     *
     * Validation errors result in OBErrorException being raised.
     *
     * @param newPaymentRequest the new payment that is being submitted
     * @param newPaymentIdempotencyKey the idempotency key used in the new payment request
     * @param existingPayment the payment that has been found in the repo that we are validating against
     * @throws OBErrorException with either PAYMENT_SUBMISSION_ALREADY_EXISTS or IDEMPOTENCY_KEY_REQUEST_BODY_CHANGED error
     */
    default void validateExistingPaymentIdempotencyData(R newPaymentRequest, String newPaymentIdempotencyKey, T existingPayment) throws OBErrorException {
        if (!existingPayment.getIdempotencyKey().equals(newPaymentIdempotencyKey)) {
            throw new OBErrorException(OBRIErrorType.PAYMENT_SUBMISSION_ALREADY_EXISTS, existingPayment.getId());
        } else {
            if (!existingPayment.getPayment().equals(newPaymentRequest)) {
                throw new OBErrorException(OBRIErrorType.IDEMPOTENCY_KEY_REQUEST_BODY_CHANGED, newPaymentIdempotencyKey);
            }
        }
    }

}
