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

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;

import uk.org.openbanking.datamodel.error.OBError1;

class BaseOBValidatorTest {

    // Impl which always throws a RuntimeException
    private static class BuggyOBValidator extends BaseOBValidator {
        @Override
        protected void validate(Object obj, ValidationResult validationResult) {
            throw new IllegalStateException("coding error");
        }
    }

    @Test
    void convertsUnexpectedRuntimeExceptionsToObErrors() {
        final BaseOBValidator buggyValidator = new BuggyOBValidator();
        final OBError1 expectedError = createOBError("OBRI.Server.InternalError", "Internal Server Error");
        validateErrorResult(buggyValidator.validate(new Object()), List.of(expectedError));
    }

    public static OBError1 createOBError(String code, String message) {
        return new OBError1().errorCode(code).message(message);
    }
}