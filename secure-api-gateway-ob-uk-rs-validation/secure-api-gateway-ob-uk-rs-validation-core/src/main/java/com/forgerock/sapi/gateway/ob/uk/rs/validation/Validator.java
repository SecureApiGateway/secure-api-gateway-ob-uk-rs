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


/**
 * Validator takes an object of T type and applies validation rules to it.
 *
 * Calls to validate should never throw an exception, instead errors should be added to the ValidationResult. This gives
 * the implementor the option of failing fast (on the first error encountered) or collecting all the errors and
 * reporting them in a single result.
 *
 * @param <T> type of the object to validate
 * @param <E> type of the errors returned by the {@link ValidationResult}
 */
public interface Validator<T, E> {

    ValidationResult<E> validate(T obj);

}
