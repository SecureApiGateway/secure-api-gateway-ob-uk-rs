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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.account.consent;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.account.OBReadConsent1;
import uk.org.openbanking.datamodel.v4.account.OBReadConsent1Data;

public class OBReadConsent1ValidatorTest {

    private final OBReadConsent1Validator obReadConsent1Validator = new OBReadConsent1Validator();

    @Test
    void consentMeetsValidationRequirements() {
        final List<OBReadConsent1> validConsents = List.of(new OBReadConsent1().data(new OBReadConsent1Data()),
                                                           new OBReadConsent1().data(new OBReadConsent1Data().expirationDateTime(
                                                                   DateTime.now().plusDays(2))));

        assertTrue(validConsents.stream().map(obReadConsent1Validator::validate).allMatch(ValidationResult::isValid));
    }

    @Test
    void consentExpirationInPast() {
        final ValidationResult<OBError1> consentExpirationInPast = obReadConsent1Validator.validate(
                invalidConsentExpirationInPast());
        validateErrorResult(consentExpirationInPast, expectedConsentExpirationInPastError());
    }

    public static List<OBError1> expectedConsentExpirationInPastError() {
        return List.of(new OBError1().errorCode("UK.OBIE.Field.Invalid")
                                     .message(
                                             "The field received is invalid. Reason 'ExpirationDateTime must be in "
                                                     + "the future'"));
    }

    public static OBReadConsent1 invalidConsentExpirationInPast() {
        return new OBReadConsent1().data(new OBReadConsent1Data().expirationDateTime(DateTime.now().minusMinutes(2)));
    }
}