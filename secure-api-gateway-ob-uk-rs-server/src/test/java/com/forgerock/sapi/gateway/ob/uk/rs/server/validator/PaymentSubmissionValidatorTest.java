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

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.v3.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.error.OBError1;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static uk.org.openbanking.testsupport.v3.payment.OBRisk1TestDataFactory.aValidOBRisk1;

/**
 * Unit test for {@link PaymentSubmissionValidator}.
 */
public class PaymentSubmissionValidatorTest {

    private PaymentSubmissionValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new PaymentSubmissionValidator(new IdempotencyValidator(), new OBRisk1Validator(true));
    }

    @Test
    public void shouldVerifyValidIdempotencyKeyAndRisk() throws OBErrorResponseException {
        // Given
        String idempotencyKey = UUID.randomUUID().toString();
        OBRisk1 risk = aValidOBRisk1();

        // When
        validator.validateIdempotencyKeyAndRisk(idempotencyKey, risk);

        // Then No Exception is thrown
    }

    @Test
    public void shouldFailToVerifyInvalidIdempotencyKeyAndRisk() {
        // Given
        String idempotencyKey = "";
        OBRisk1 risk = aValidOBRisk1();

        // When
        OBErrorResponseException e = catchThrowableOfType(() -> validator.validateIdempotencyKeyAndRisk(idempotencyKey, risk), OBErrorResponseException.class);

        // Then
        assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(e.getCategory()).isEqualTo(OBRIErrorResponseCategory.REQUEST_FILTER);
        OBError1 error = new OBError1()
                .errorCode("FR.OBRI.idempotency.key.Invalid")
                .message("Invalid Idempotency Key provided in header. The x-idempotency-key in the request headers must " +
                        "be between 1 and 40 characters. Provided value: '' has length: 0");
        assertThat(e.getErrors().get(0)).isEqualTo(error);
    }
}
