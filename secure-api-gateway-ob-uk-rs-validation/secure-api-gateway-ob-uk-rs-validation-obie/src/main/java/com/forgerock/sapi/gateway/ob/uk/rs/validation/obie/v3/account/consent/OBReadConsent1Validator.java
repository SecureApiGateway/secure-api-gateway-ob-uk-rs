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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.account.consent;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;

import uk.org.openbanking.datamodel.v3.account.OBReadConsent1;
import uk.org.openbanking.datamodel.v3.account.OBReadConsent1Data;
import uk.org.openbanking.datamodel.v3.error.OBError1;

/**
 * Validator of OBReadConsent1 objects (Accounts Access Consents)
 */
public class OBReadConsent1Validator extends BaseOBValidator<OBReadConsent1> {

    @Override
    protected void validate(OBReadConsent1 accountConsent, ValidationResult<OBError1> validationResult) {
        final OBReadConsent1Data accountConsentData = accountConsent.getData();
        validateExpirationDateTime(accountConsentData, validationResult);

        // TODO add any other validation as required
    }

    private void validateExpirationDateTime(OBReadConsent1Data accountConsentData, ValidationResult<OBError1> validationResult) {
        final DateTime expirationDateTime = accountConsentData.getExpirationDateTime();
        if (expirationDateTime != null) {
            if (expirationDateTime.isBeforeNow()) {
                validationResult.addError(OBRIErrorType.REQUEST_FIELD_INVALID.toOBError1("ExpirationDateTime must be in the future"));
            }
        }
    }
}
