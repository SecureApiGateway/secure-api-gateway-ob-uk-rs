/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.common.util;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBVersion;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.PaymentSubmission;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpStatus.CONFLICT;

public class PaymentApiResponseUtil {

    /**
     * Creates a {@link ResponseEntity} with a 409 (Conflict) response for when a {@link PaymentSubmission} is being
     * accessed via an older API version.
     *
     * @param frPaymentSubmission The {@link PaymentSubmission} resource that is being accessed.
     * @param apiVersion The version of the API being called.
     * @return A {@link ResponseEntity} containing a 409 (Conflict) status and error message.
     */
    public static ResponseEntity<String> resourceConflictResponse(PaymentSubmission frPaymentSubmission,
                                                                  OBVersion apiVersion) {
        return ResponseEntity
                .status(CONFLICT)
                .body(String.format(
                        "Resource saved against API version [%s] cannot be accessed via older version [%s].",
                        frPaymentSubmission.getObVersion(),
                        apiVersion));
    }
}
