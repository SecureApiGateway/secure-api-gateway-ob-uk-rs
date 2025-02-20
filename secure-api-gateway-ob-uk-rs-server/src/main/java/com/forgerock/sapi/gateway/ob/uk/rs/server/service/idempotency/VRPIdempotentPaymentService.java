/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVrpRequest;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRDomesticVrpPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticVrpPaymentSubmissionRepository;

/**
 * This implementation works with VRP payments, there can be multiple of these payments per consent.
 *
 * As we are not able to know the id ahead of time (unlike {@link SinglePaymentForConsentIdempotentPaymentService} then
 * we cannot guard against duplicate payments being inserted concurrently. This is deemed good enough for a test
 * facility as no payments are actually being made.
 *
 * See {@link IdempotentPaymentService} documentation for known limitations of this approach.
 */
public class VRPIdempotentPaymentService implements IdempotentPaymentService<FRDomesticVrpPaymentSubmission, FRDomesticVrpRequest> {

    private final DomesticVrpPaymentSubmissionRepository repository;

    public VRPIdempotentPaymentService(DomesticVrpPaymentSubmissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FRDomesticVrpPaymentSubmission> findExistingPayment(FRDomesticVrpRequest frPaymentRequest, String consentId, String apiClientId, String idempotencyKey) throws OBErrorException {
        final Optional<FRDomesticVrpPaymentSubmission> existingPayment = repository.findByIdempotencyData(apiClientId, idempotencyKey, DateTime.now());
        if (existingPayment.isPresent()) {
            validateExistingPaymentIdempotencyData(frPaymentRequest, idempotencyKey, existingPayment.get());
        }
        return existingPayment;
    }

    @Override
    public FRDomesticVrpPaymentSubmission savePayment(FRDomesticVrpPaymentSubmission paymentSubmission) {
        requireNonNull(paymentSubmission.getIdempotencyKey());
        requireNonNull(paymentSubmission.getIdempotencyKeyExpiration());
        requireNonNull(paymentSubmission.getApiClientId());
        return repository.save(paymentSubmission);
    }
}
