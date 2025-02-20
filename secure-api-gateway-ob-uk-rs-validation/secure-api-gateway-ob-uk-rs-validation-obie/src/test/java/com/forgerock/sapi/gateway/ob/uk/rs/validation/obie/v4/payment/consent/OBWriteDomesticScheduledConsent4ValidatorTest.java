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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBRisk1ValidatorTest.createDefaultRiskValidator;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBRisk1ValidatorTest.createPaymentContextCodeRiskValidator;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomestic2DataInitiationInstructedAmountValidatorTest.createInstructedAmountValidator;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBRisk1ValidatorV4;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.common.OBRisk1;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduled2DataInitiation;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduledConsent4Data;

class OBWriteDomesticScheduledConsent4ValidatorTest {

    final OBWriteDomesticScheduledConsent4Validator validator = new OBWriteDomesticScheduledConsent4Validator(
            createInstructedAmountValidator("GBP", "EUR", "USD"), createDefaultRiskValidator());

    private static OBWriteDomesticScheduledConsent4 createValidConsent() {
        final OBWriteDomesticScheduledConsent4 consent = new OBWriteDomesticScheduledConsent4();
        final OBWriteDomesticScheduledConsent4Data consentData = new OBWriteDomesticScheduledConsent4Data();
        final OBWriteDomesticScheduled2DataInitiation initiation = new OBWriteDomesticScheduled2DataInitiation();
        initiation.setInstructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount().amount("12.99").currency("GBP"));
        initiation.setRequestedExecutionDateTime(DateTime.now().plusDays(7));
        consentData.setInitiation(initiation);
        consent.setData(consentData);
        consent.setRisk(new OBRisk1());
        return consent;
    }

    @Test
    public void testValidConsent() {
        validateSuccessResult(validator.validate(createValidConsent()));
    }

    @Test
    public void failsValidationWhenRequestExecutionDateTimeInPast() {
        final OBWriteDomesticScheduledConsent4 consent = createValidConsent();
        consent.getData().getInitiation().setRequestedExecutionDateTime(DateTime.now().minusSeconds(1));

        final ValidationResult<OBError1> validationResult = validator.validate(consent);
        validateErrorResult(validationResult, List.of(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("RequestedExecutionDateTime must be in the future")));
    }

    @Test
    public void failsValidationWhenInstructedAmountIsInvalid() {
        final OBError1 expectedValidationError = OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("invalid instructed amount");
        final ValidationResult<OBError1> validationResult = new OBWriteDomesticScheduledConsent4Validator(new BaseOBValidator<>() {
            @Override
            protected void validate(OBWriteDomestic2DataInitiationInstructedAmount obj, ValidationResult<OBError1> validationResult) {
                validationResult.addError(expectedValidationError);
            }
        }, new OBRisk1ValidatorV4(false)).validate(createValidConsent());

        validateErrorResult(validationResult, List.of(expectedValidationError));
    }

    @Test
    public void failsRiskValidation() {
        final OBWriteDomesticScheduledConsent4 consent = createValidConsent();
        consent.getRisk().setPaymentContextCode(null);
        final ValidationResult<OBError1> validationResult = new OBWriteDomesticScheduledConsent4Validator(
                createInstructedAmountValidator("GBP"), createPaymentContextCodeRiskValidator()).validate(createValidConsent());

        validateErrorResult(validationResult, List.of(OBRIErrorType.PAYMENT_CODE_CONTEXT_INVALID.toOBError1()));
    }
}
