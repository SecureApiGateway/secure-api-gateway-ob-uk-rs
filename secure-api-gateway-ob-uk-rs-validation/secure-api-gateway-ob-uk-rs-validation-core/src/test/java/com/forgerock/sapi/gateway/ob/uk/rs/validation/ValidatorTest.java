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

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidatorTest.DateRangeValidator.DateRange;


class ValidatorTest {

    /**
     * Validator which demonstrates returning a result with at most one error
     */
    private static class EvenNumberValidator implements Validator<Integer, String> {
        @Override
        public ValidationResult<String> validate(Integer obj) {
            if (obj.intValue() % 2 != 0) {
                return ValidationResult.failureResult(List.of(obj + " is not even"));
            }
            return ValidationResult.successResult();
        }
    }

    /**
     * Validator which demonstrates how multiple Errors can be added to a single result
     */
    static class DateRangeValidator implements Validator<DateRange, String> {

        static class DateRange {
            private final LocalDateTime startDate;
            private final LocalDateTime endDate;

            public DateRange(LocalDateTime startDate, LocalDateTime endDate) {
                this.startDate = startDate;
                this.endDate = endDate;
            }
        }

        @Override
        public ValidationResult<String> validate(DateRange obj) {
            final ValidationResult<String> result = new ValidationResult<>();
            if (obj.startDate == null) {
                result.addError("startDate must be supplied");
            }
            if (obj.endDate == null) {
                result.addError("endDate must be supplied");
            }
            if (obj.startDate != null && obj.endDate != null && obj.endDate.isBefore(obj.startDate)) {
                result.addError("endDate must be >= startDate");
            }
            return result;
        }
    }

    @Test
    void testAlwaysSuccessValidator() {
        final SuccessValidator validator = new SuccessValidator<>();
        for (int i = 0; i < 10; i++) {
            validateSuccessResult(validator.validate(new Object()));
        }
    }

    @Test
    void testEvenNumberValidator() {
        final EvenNumberValidator evenNumberValidator = new EvenNumberValidator();
        for (int i = 0; i <= 10; i+=2) {
            validateSuccessResult(evenNumberValidator.validate(i));
        }
        for (int i = 1; i < 10; i+=2) {
            validateErrorResult(evenNumberValidator.validate(i), List.of(i + " is not even"));
        }
    }

    @Test
    void testDateRangeValidator() {
        final DateRangeValidator dateRangeValidator = new DateRangeValidator();
        validateErrorResult(dateRangeValidator.validate(new DateRange(null, null)), List.of("startDate must be supplied", "endDate must be supplied"));
        validateErrorResult(dateRangeValidator.validate(new DateRange(LocalDateTime.now(), LocalDateTime.now().minusDays(1))), List.of("endDate must be >= startDate"));
    }
}