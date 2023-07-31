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

/**
 * Service which can find any existing payment for a particular idempotencyKey.
 *
 * This provides a simplified payment submission idempotency implementation. It is not guaranteed to work when there
 * are concurrent requests with the same idempotencyKey. This service is aimed at allowing TPPs to test idempotent
 * behaviour in a sequential fashion.
 *
 * A proper impl of idempotency should be handled in a gateway or as a request filter, a separate datastore is required
 * which tracks requests and responses.
 *
 * @param <T> PaymentSubmission<R> type - the entity stored in the datastore for a particular payment type
 * @param <R> The FR data-model representation of the OB payment request
 */
public interface IdempotentPaymentService<T extends PaymentSubmission<R>, R> {

    Optional<T> findExistingPayment(R frPaymentRequest, String consentId, String idempotencyKey) throws OBErrorException;

}
