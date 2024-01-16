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
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.ApiConstants;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.PaymentSubmission;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType.IDEMPOTENCY_KEY_INVALID;
import static com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBHeaders.X_IDEMPOTENCY_KEY;

/**
 * Performs validation of idempotent requests.
 */
@Component
@Slf4j
public class IdempotencyValidator {

    private static final int X_IDEMPOTENCY_MAX_KEY_LENGTH = 40;
    private static final int X_IDEMPOTENCY_KEY_EXPIRY_HOURS = 24;

    public void verifyIdempotencyKeyLength(String xIdempotencyKey) throws OBErrorException {
        if (!isIdempotencyKeyHeaderValid(xIdempotencyKey)) {
            log.warn("Header value for {} must be between 1 and 40 characters. Provided header {} : {}'",
                    X_IDEMPOTENCY_KEY, X_IDEMPOTENCY_KEY, xIdempotencyKey);
            throw new OBErrorException(IDEMPOTENCY_KEY_INVALID, xIdempotencyKey, (xIdempotencyKey == null) ? 0 :
                    xIdempotencyKey.length());
        }
        log.debug("xIdempotency key '{}' is valid length", xIdempotencyKey);
    }

    private static boolean isIdempotencyKeyHeaderValid(String xIdempotencyKey) {
        return !StringUtils.isEmpty(xIdempotencyKey)
                && xIdempotencyKey.length() <= X_IDEMPOTENCY_MAX_KEY_LENGTH;
    }

    /**
     * For payment submissions.
     * <p>
     * Idempotency key must be the same for existing and new requests. Idempotency key must be less than expiry time. (X_IDEMPOTENCY_KEY_EXPIRY_HOURS)
     */
    public static <T> void validateIdempotencyRequest(PaymentSubmission submittedPayment, PaymentSubmission existingPayment)
            throws OBErrorResponseException {
        checkMatchingIdempotencyKey(submittedPayment.getIdempotencyKey(), existingPayment);
        checkIdempotencyKeyExpiry(submittedPayment.getIdempotencyKey(), existingPayment.getId(), existingPayment.getCreated());
        // We don't need to check if body changed since previous request as that is not possible because submission
        // data/risk cannot be changed from the consent anyway.
    }

    private static void checkMatchingIdempotencyKey(String xIdempotencyKey, PaymentSubmission existingPayment
    ) throws OBErrorResponseException {
        if (!xIdempotencyKey.equals(existingPayment.getIdempotencyKey())) {
            log.warn("An existing payment submission with the same consent id but a different idempotency key was found. " +
                            "Cannot create this payment. Consent id: {}, Payment id: {}, idempotency key of request: {}, " +
                            "idempotency key of existing payment: {}", existingPayment.getConsentId(), existingPayment.getId(),
                    xIdempotencyKey, existingPayment.getIdempotencyKey());
            throw new OBErrorResponseException(
                    HttpStatus.FORBIDDEN,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.PAYMENT_SUBMISSION_ALREADY_EXISTS.toOBError1(existingPayment.getId())
            );
        }
        log.info("Existing payment '{}' has the same x-idempotency-key '{}'.", existingPayment.getId(), xIdempotencyKey);
    }

    // https://openbanking.atlassian.net/wiki/spaces/DZ/pages/937656404/Read+Write+Data+API+Specification+-+v3.1#Read/WriteDataAPISpecification-v3.1-Idempotency.1
    private static void checkIdempotencyKeyExpiry(String xIdempotencyKey, String paymentId, DateTime paymentCreated
    ) throws OBErrorResponseException {
        if ((DateTime.now().minusHours(X_IDEMPOTENCY_KEY_EXPIRY_HOURS).isAfter(paymentCreated))) {
            log.debug("Matching idempotency key '{}' provided but previous use was more than '{}' hours ago so it has " +
                            "expired so rejecting request. Previous use was on id: '{}'",
                    xIdempotencyKey, X_IDEMPOTENCY_KEY_EXPIRY_HOURS, paymentId);
            throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.IDEMPOTENCY_KEY_EXPIRED.toOBError1(
                            xIdempotencyKey,
                            paymentId,
                            paymentCreated.toString(ApiConstants.BOOKED_TIME_DATE_FORMAT),
                            X_IDEMPOTENCY_KEY_EXPIRY_HOURS)
            );
        }
    }
}
