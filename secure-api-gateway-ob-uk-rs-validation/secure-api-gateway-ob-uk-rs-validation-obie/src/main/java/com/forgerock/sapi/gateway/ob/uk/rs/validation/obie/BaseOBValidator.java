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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.Validator;

import uk.org.openbanking.datamodel.v3.error.OBError1;

/**
 * Implementation of Validator which can be extended to provide Open Banking specific validation.
 *
 * Common functionality provided by this class:
 * - converting unexpected RuntimeExceptions to OBError1 objects
 * - logging of failures
 *
 * @param <T> type of the object to validate
 */
public abstract class BaseOBValidator<T> implements Validator<T, OBError1> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public final ValidationResult<OBError1> validate(T obj) {
        final ValidationResult<OBError1> validationResult = new ValidationResult<>();
        try {
            validate(obj, validationResult);
            if (!validationResult.isValid()) {
                logger.debug("Validation failed for object: {}, errors: {}", obj, validationResult.getErrors());
            }
        } catch (RuntimeException rte) {
            logger.error("Validation failed for object: {} due to unexpected exception thrown by validator: {} " +
                         "- Validator implementations must not throw exceptions", obj, getClass(), rte);
            validationResult.addError(OBRIErrorType.SERVER_ERROR.toOBError1());
        }
        return validationResult;
    }

    /**
     * Method to apply implementation specific validation rules
     *
     * @param obj T the object to validate
     * @param validationResult the validationResult to add errors to if the implementation detects that the obj param
     *                         does not meet the validation requirements.
     */
    protected abstract void validate(T obj, ValidationResult<OBError1> validationResult);

}
