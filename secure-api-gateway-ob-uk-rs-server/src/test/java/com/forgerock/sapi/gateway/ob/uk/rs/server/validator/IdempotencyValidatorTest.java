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
package com.forgerock.sapi.gateway.ob.uk.rs.server.validator;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Unit test for {@link IdempotencyValidator}.
 */
public class IdempotencyValidatorTest {

    private final IdempotencyValidator idempotencyValidator = new IdempotencyValidator();

    @Test
    public void shouldVerifyIdempotencyKeyGivenValidKey() throws OBErrorException {
        // Given
        String xIdempotencyKey = UUID.randomUUID().toString();

        // When
        idempotencyValidator.verifyIdempotencyKeyLength(xIdempotencyKey);

        // Then No Exception is thrown
    }

    @Test
    public void shouldFailToVerifyIdempotencyKeyGivenEmptyKey() {
        // Given
        String xIdempotencyKey = "";

        // When
        OBErrorException e = catchThrowableOfType(() -> idempotencyValidator.verifyIdempotencyKeyLength(xIdempotencyKey), OBErrorException.class);

        // Then
        assertThat(e).hasMessage("Invalid Idempotency Key provided in header. The x-idempotency-key in the request " +
                "headers must be between 1 and 40 characters. Provided value: '' has length: 0");
    }

    @Test
    public void shouldFailToVerifyIdempotencyKeyGivenKeyTooLong() {
        // Given
        String xIdempotencyKey = "a".repeat(41);

        // When
        OBErrorException e = catchThrowableOfType(() -> idempotencyValidator.verifyIdempotencyKeyLength(xIdempotencyKey), OBErrorException.class);

        // Then
        assertThat(e).hasMessage("Invalid Idempotency Key provided in header. The x-idempotency-key in the request " +
                "headers must be between 1 and 40 characters. Provided value: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' has length: 41");
    }
}