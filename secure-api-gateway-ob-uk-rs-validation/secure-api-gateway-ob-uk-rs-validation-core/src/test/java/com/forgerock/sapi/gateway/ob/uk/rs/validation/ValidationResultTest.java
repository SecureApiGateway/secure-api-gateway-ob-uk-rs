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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ValidationResultTest {

    @Test
    void testSuccessResult() {
        validateSuccessResult(ValidationResult.successResult());
        validateSuccessResult(new ValidationResult<>());
    }

    @Test
    void testAddingErrors() {
        final ValidationResult<Exception> result = new ValidationResult<>();
        validateSuccessResult(result);

        final List<Exception> errors = List.of(new NullPointerException(), new IllegalArgumentException(), new IllegalStateException());

        for (int i = 0; i < errors.size(); i++) {
            final Exception error = errors.get(i);
            result.addError(error);
            validateErrorResult(result, errors.subList(0, i + 1));
        }
    }

    @Test
    void testCreatingFailureResult() {
        final List<String> errors = List.of("error1", "error2", "error3");
        final ValidationResult<String> result = ValidationResult.failureResult(errors);
        validateErrorResult(result, errors);

        final String additionalError = "additionalError";
        result.addError(additionalError);
        final List<String> errorsPlusAdditional = new ArrayList<>(errors);
        errorsPlusAdditional.add(additionalError);
        validateErrorResult(result, errorsPlusAdditional);
    }

    @Test
    void testFailureToCreateFailureResult() {
        assertThrows(IllegalArgumentException.class, () -> ValidationResult.failureResult(null));
        assertThrows(IllegalArgumentException.class, () -> ValidationResult.failureResult(List.of()));
    }

    @Test
    void testMergingSuccessResults() {
        final ValidationResult successResult1 = new ValidationResult<>();
        final ValidationResult successResult2 = new ValidationResult<>();
        validateSuccessResult(successResult1.mergeResults(successResult2));
        validateSuccessResult(successResult2.mergeResults(successResult1));
        validateSuccessResult(successResult1.mergeResults(new ValidationResult()));
    }

    @Test
    void testMergingSuccessAndFailureResults() {
        final List<String> errors = List.of("err1", "sdfsfsdfsd", "err3");
        final ValidationResult<String> failureResult = ValidationResult.failureResult(errors);

        validateErrorResult(new ValidationResult<String>().mergeResults(failureResult), errors);
        validateErrorResult(failureResult.mergeResults(new ValidationResult<>()), errors);
    }

    @Test
    void testMergingErrorResults() {
        final List<String> errors1 = List.of("err1");
        final ValidationResult<String> errorResult1 = ValidationResult.failureResult(errors1);

        final List<String> errors2 = List.of("err2", "err3");
        final ValidationResult<String> errorResult2 = ValidationResult.failureResult(errors2);

        final List<String> mergedErrors = new ArrayList<>(errors1);
        mergedErrors.addAll(errors2);
        validateErrorResult(errorResult1.mergeResults(errorResult2), mergedErrors);
    }

    public static void validateSuccessResult(ValidationResult<?> result) {
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    public static <E> void validateErrorResult(ValidationResult<E> result, List<E> expectedErrors) {
        assertFalse(result.isValid());
        assertEquals(expectedErrors, result.getErrors());
    }
}
