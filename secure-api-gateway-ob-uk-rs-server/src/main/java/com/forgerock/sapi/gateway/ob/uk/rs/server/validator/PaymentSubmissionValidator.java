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
package com.forgerock.sapi.gateway.ob.uk.rs.server.validator;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.org.openbanking.datamodel.v3.common.OBRisk1;

/**
 * Performs validation that is common across the Payments API.
 */
@Component
@Slf4j
public class PaymentSubmissionValidator {

    private final IdempotencyValidator idempotencyValidator;
    private final OBRisk1Validator riskValidator;

    public PaymentSubmissionValidator(IdempotencyValidator idempotencyValidator, OBRisk1Validator riskValidator) {
        this.idempotencyValidator = idempotencyValidator;
        this.riskValidator = riskValidator;
    }

    /**
     * Validates the provided Idempotency Key and Risk. If no error is thrown then the values are valid.
     *
     * @param xIdempotencyKey The 'x-idempotency-key' header ensuring every request is processed only once per key.
     * @param obRisk1 Additional details for risk scoring a Payment.
     * @throws OBErrorResponseException if a validation error occurs.
     * @deprecated obRisk1 should be validated as part of consent validation when the consent is submitted, this should
     * be done by a validator in the rs-validation module.
     * Example validator: {@link com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBWriteDomesticConsent4Validator}
     */
    @Deprecated
    public void validateIdempotencyKeyAndRisk(String xIdempotencyKey, OBRisk1 obRisk1) throws OBErrorResponseException {
        try {
            idempotencyValidator.verifyIdempotencyKeyLength(xIdempotencyKey);
            riskValidator.validate(obRisk1);
        } catch (OBErrorException e) {
            log.warn("Verification failed", e);
            throw new OBErrorResponseException(
                    e.getObriErrorType().getHttpStatus(),
                    OBRIErrorResponseCategory.REQUEST_FILTER,
                    e.getOBError());
        }
    }

    /**
     * Validates the provided Idempotency Key. If no error is thrown then the key is valid.
     *
     * @param xIdempotencyKey The 'x-idempotency-key' header ensuring every request is processed only once per key.
     * @throws OBErrorResponseException if a validation error occurs.
     */
    public void validateIdempotencyKey(String xIdempotencyKey) throws OBErrorResponseException {
        try {
            idempotencyValidator.verifyIdempotencyKeyLength(xIdempotencyKey);
        } catch (OBErrorException e) {
            log.warn("Verification failed", e);
            throw new OBErrorResponseException(
                    e.getObriErrorType().getHttpStatus(),
                    OBRIErrorResponseCategory.REQUEST_FILTER,
                    e.getOBError());
        }
    }
}
