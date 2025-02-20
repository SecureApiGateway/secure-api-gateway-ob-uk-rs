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
package com.forgerock.sapi.gateway.ob.uk.rs.validation;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class ValidateAllTest {

    private static final SuccessValidator<Object, String> SUCCESS_VALIDATOR = new SuccessValidator<>();

    private static class AlwaysErrorsValidator<T> implements Validator<T, String> {

        private final String errorMessage;

        private AlwaysErrorsValidator(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public ValidationResult<String> validate(T obj) {
            return ValidationResult.failureResult(List.of(errorMessage));
        }
    }

    @Test
    void failToConstructValidateAll() {
        assertThrows(IllegalArgumentException.class, () -> new ValidateAll<>(null));
        assertThrows(IllegalArgumentException.class, () -> new ValidateAll<>(List.of()));
    }

    @Test
    void validateAllValid() {
        final List<Validator<Object, String>> validators = Collections.nCopies(5, SUCCESS_VALIDATOR);

        final Validator<Object, String> validateAll = new ValidateAll<>(validators);
        validateSuccessResult(validateAll.validate(new Object()));
    }

    @Test
    void validateAllError() {
        final List<String> errorMessages = List.of("error1", "boom", "fail", "?!");
        final List<Validator<Object, String>> validators = errorMessages.stream()
                                                                        .map(AlwaysErrorsValidator::new)
                                                                        .collect(Collectors.toList());

        final Validator<Object, String> validateAll = new ValidateAll<>(validators);
        validateErrorResult(validateAll.validate(new Object()), errorMessages);
    }

    @Test
    void validateSomeError() {
        final List<Validator<Object, String>> validators = List.of(SUCCESS_VALIDATOR, new AlwaysErrorsValidator<>("boom"),
                SUCCESS_VALIDATOR, new AlwaysErrorsValidator<>("another error"));

        final ValidateAll<Object, String> validateAll = new ValidateAll<>(validators);
        validateErrorResult(validateAll.validate(new Object()), List.of("boom", "another error"));
    }
}