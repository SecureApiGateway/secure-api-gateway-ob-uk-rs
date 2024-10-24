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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.SuccessValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.account.consent.OBReadConsent1Validator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.account.OBReadConsent1;
import uk.org.openbanking.datamodel.v4.account.OBReadConsent1Data;

import java.util.Collections;
import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.account.consent.OBReadConsent1ValidatorTest.expectedConsentExpirationInPastError;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.account.consent.OBReadConsent1ValidatorTest.invalidConsentExpirationInPast;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OBValidationServiceTest {

    @Test
    void failsToConstructWithMissingParams() {
        assertThrows(NullPointerException.class, () -> new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<>((Validator) null));
        assertThrows(IllegalArgumentException.class, () -> new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<>((List) null));
    }

    @Test
    void failsToCustomiseExceptionFactory() {
        assertEquals("exceptionFactory must be supplied",
                assertThrows(NullPointerException.class,
                             () -> new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<>(new SuccessValidator<>()).setExceptionFactory(null))
                .getMessage());
    }

    @Test
    void mustNotRaiseExceptionIfValidationSucceeds() throws Exception {
        new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<>(new SuccessValidator<>()).validate(new Object());
    }

    @Test
    void mustRaiseExceptionIfValidationFails() {
        final com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<OBReadConsent1> accountConsentValidationService = new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<>(new OBReadConsent1Validator());
        final OBErrorResponseException responseException = assertThrows(OBErrorResponseException.class,
                () -> accountConsentValidationService.validate(invalidConsentExpirationInPast()));
        assertEquals(HttpStatus.BAD_REQUEST, responseException.getStatus());
        assertEquals(OBRIErrorResponseCategory.REQUEST_INVALID, responseException.getCategory());
        assertEquals(expectedConsentExpirationInPastError(), responseException.getErrors());
    }

    @Test
    void testCustomisingExceptionFactory() {
        final com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<OBReadConsent1> accountConsentValidationService = new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<>(new OBReadConsent1Validator());
        accountConsentValidationService.setExceptionFactory(result -> new OBErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, OBRIErrorResponseCategory.SERVER_INTERNAL_ERROR, new OBError1()));

        final OBErrorResponseException responseException = assertThrows(OBErrorResponseException.class,
                () -> accountConsentValidationService.validate(invalidConsentExpirationInPast()));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseException.getStatus());
        assertEquals(OBRIErrorResponseCategory.SERVER_INTERNAL_ERROR, responseException.getCategory());
        assertEquals(List.of(new OBError1()), responseException.getErrors());
    }

    @Test
    void testListOfValidators() throws Exception {
        final com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService<OBReadConsent1> validationService = new OBValidationService<>(List.of(new SuccessValidator<>(),
                new OBReadConsent1Validator(), new SuccessValidator<>(), new OBReadConsent1Validator())); // Account validator included twice, errors will be duplicated

        validationService.validate(new OBReadConsent1().data(new OBReadConsent1Data())); // valid consent

        // Invalid consent, verify we get 2 error objects
        final OBErrorResponseException responseException = assertThrows(OBErrorResponseException.class,
                () -> validationService.validate(invalidConsentExpirationInPast()));
        assertEquals(HttpStatus.BAD_REQUEST, responseException.getStatus());
        assertEquals(OBRIErrorResponseCategory.REQUEST_INVALID, responseException.getCategory());
        assertEquals(Collections.nCopies(2, expectedConsentExpirationInPastError().stream().findFirst().get()), responseException.getErrors());
    }
}