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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.http.HttpStatus;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidateAll;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.Validator;

import uk.org.openbanking.datamodel.error.OBError1;

/**
 * A service which invokes 1 or more validators and throws an exception if there are any errors in the results.
 *
 * This service is intended to be used in OB Services and Controllers providing a convenient mechanism for reporting
 * errors as exceptions. This guards against developers forgetting to check the ValidationResult returned by the
 * validators, and provides a consistent way of reporting errors to the end user.
 *
 * @param <T> type of object that can be validated by this service.
 */
public class OBValidationService<T> {

    /**
     * Default Exception Factory which produces OBErrorResponseExceptions with:
     * - HTTP Status 400 (Bad Request)
     * - OBRI.Request.Invalid error category
     * - All OBError1 objects from the ValidationResult
     */
    private static final Function<ValidationResult<OBError1>, OBErrorResponseException> DEFAULT_EXCEPTION_FACTORY =
            validationResult -> new OBErrorResponseException(HttpStatus.BAD_REQUEST, OBRIErrorResponseCategory.REQUEST_INVALID, validationResult.getErrors());

    private final Validator<T, OBError1> delegate;

    /**
     * Function which converts a ValidationResult into an OBErrorResponseException
     *
     * Uses {@link #DEFAULT_EXCEPTION_FACTORY} by default, behaviour can be changed by calling {@link #setExceptionFactory(Function)}
     * and supplying a custom implementation.
     */
    private Function<ValidationResult<OBError1>, OBErrorResponseException> exceptionFactory = DEFAULT_EXCEPTION_FACTORY;

    public OBValidationService(Validator<T, OBError1> validator) {
        Objects.requireNonNull(validator, "validator must be supplied");
        this.delegate = validator;
    }

    public OBValidationService(List<Validator<T, OBError1>> validators) {
        if (validators == null || validators.isEmpty()) {
            throw new IllegalArgumentException("one or more validators must be supplied");
        }
        this.delegate = new ValidateAll<>(validators);
    }

    public void setExceptionFactory(Function<ValidationResult<OBError1>, OBErrorResponseException> exceptionFactory) {
        Objects.requireNonNull(exceptionFactory, "exceptionFactory must be supplied");
        this.exceptionFactory = exceptionFactory;
    }

    public void validate(T object) throws OBErrorResponseException {
        final ValidationResult<OBError1> validationResult = delegate.validate(object);
        if (!validationResult.isValid()) {
            throw exceptionFactory.apply(validationResult);
        }
    }
}
