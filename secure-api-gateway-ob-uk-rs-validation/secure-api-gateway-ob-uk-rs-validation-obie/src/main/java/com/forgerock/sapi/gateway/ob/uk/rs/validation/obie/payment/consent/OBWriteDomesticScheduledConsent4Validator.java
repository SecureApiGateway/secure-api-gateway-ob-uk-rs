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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent;

import java.util.Objects;

import org.joda.time.DateTime;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4;

/**
 * Validator of OBWriteDomesticScheduledConsent4 objects (Domestic Scheduled Payment Consents)
 */
public class OBWriteDomesticScheduledConsent4Validator extends BaseOBValidator<OBWriteDomesticScheduledConsent4> {

    private final Validator<OBWriteDomestic2DataInitiationInstructedAmount, OBError1> instructedAmountValidator;

    public OBWriteDomesticScheduledConsent4Validator(Validator<OBWriteDomestic2DataInitiationInstructedAmount, OBError1> instructedAmountValidator) {
        this.instructedAmountValidator = Objects.requireNonNull(instructedAmountValidator, "instructedAmountValidator must be supplied");
    }

    @Override
    protected void validate(OBWriteDomesticScheduledConsent4 consent, ValidationResult<OBError1> validationResult) {
        validationResult.mergeResults(instructedAmountValidator.validate(consent.getData().getInitiation().getInstructedAmount()));

        final DateTime requestedExecutionDateTime = consent.getData().getInitiation().getRequestedExecutionDateTime();
        if (!requestedExecutionDateTime.isAfterNow()) {
            validationResult.addError(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("RequestedExecutionDateTime must be in the future"));
        }
    }

}
