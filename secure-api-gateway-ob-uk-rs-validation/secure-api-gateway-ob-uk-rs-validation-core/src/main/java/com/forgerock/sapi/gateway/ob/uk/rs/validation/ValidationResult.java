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
package com.forgerock.sapi.gateway.ob.uk.rs.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class which represents the outcome of a {@link Validator} applying validation to an object.
 *
 * {@link #isValid()} indicates whether the outcome is successful or not, when true this indicates that {@link #getErrors()}
 * will return an empty list else it returns all the errors reported by the validator.
 *
 * @param <E> type of error objects used to indicate validation errors
 */
public class ValidationResult<E>  {

    /**
     * Factory method to create a ValidationResult representing a failure
     *
     * @param errors the validation errors that caused this failure, must not be null or empty.
     * @return ValidationResult representing a failure, containing the errors passed as a param
     */
    public static <E> ValidationResult<E> failureResult(List<E> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("A failure ValidationResult must contain 1 or more errors");
        }
        return new ValidationResult<>(errors);
    }

    /**
     * Factory method to create a ValidationResult representing a success.
     *
     * Note: The returned object can be turned into a failure by adding errors to it.
     *
     * @return ValidationResult which is valid
     */
    public static <E> ValidationResult<E> successResult() {
        return new ValidationResult<>();
    }

    private final List<E> errors;

    public ValidationResult() {
        this.errors = new ArrayList<>();
    }

    private ValidationResult(List<E> errors) {
        this.errors = new ArrayList<>(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public void addError(E error) {
        this.errors.add(error);
    }

    public List<E> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "DefaultValidationResult{" +
                "valid=" + isValid() +
                ", errors=" + errors +
                '}';
    }

    /**
     * Merge data from a secondResult into this result. Allows 2 results to be combined into a single result.
     *
     * @param secondResult the ValidationResult to merge into this
     * @return ValidationResult this instance which now contains its original errors plus those from secondResult
     */
    public ValidationResult<E> mergeResults(ValidationResult<? extends E> secondResult) {
        Objects.requireNonNull(secondResult);
        this.errors.addAll(secondResult.errors);
        return this;
    }
}
