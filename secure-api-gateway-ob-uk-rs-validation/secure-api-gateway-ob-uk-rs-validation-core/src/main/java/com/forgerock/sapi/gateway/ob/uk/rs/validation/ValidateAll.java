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

import java.util.List;

public class ValidateAll<T, E> implements Validator<T, E> {

    private final List<Validator<T, E>> validators;

    public ValidateAll(List<Validator<T, E>> validators) {
        if (validators == null || validators.isEmpty()) {
            throw new IllegalArgumentException("validators must not be null or empty");
        }
        this.validators = validators;
    }

    @Override
    public ValidationResult<E> validate(T obj) {
        ValidationResult<E> combinedValidationResult = null;
        for (Validator<T, E> validator : validators) {
            final ValidationResult<E> validationResult = validator.validate(obj);
            if (combinedValidationResult == null) {
                combinedValidationResult = validationResult;
            } else {
                combinedValidationResult.mergeResults(validationResult);
            }
        }
        return combinedValidationResult;
    }
}
