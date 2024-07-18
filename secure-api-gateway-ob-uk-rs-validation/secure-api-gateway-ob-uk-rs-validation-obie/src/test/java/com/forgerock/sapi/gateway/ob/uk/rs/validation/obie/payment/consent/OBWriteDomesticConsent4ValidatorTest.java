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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBRisk1ValidatorTest.createDefaultRiskValidator;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBRisk1ValidatorTest.createPaymentContextCodeRiskValidator;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomestic2DataInitiationInstructedAmountValidatorTest.createInstructedAmountValidator;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBRisk1Validator;

import uk.org.openbanking.datamodel.v3.common.OBExternalPaymentContext1Code;
import uk.org.openbanking.datamodel.v3.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiation;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4Data;

class OBWriteDomesticConsent4ValidatorTest {

    final OBWriteDomesticConsent4Validator validator = new OBWriteDomesticConsent4Validator(
            createInstructedAmountValidator("GBP", "EUR", "USD"), createDefaultRiskValidator());

    private static OBWriteDomesticConsent4 createValidConsent() {
        final OBWriteDomesticConsent4 consent = new OBWriteDomesticConsent4();
        final OBWriteDomesticConsent4Data consentData = new OBWriteDomesticConsent4Data();
        final OBWriteDomestic2DataInitiation initiation = new OBWriteDomestic2DataInitiation();
        initiation.setInstructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount().amount("12.99").currency("GBP"));
        consentData.setInitiation(initiation);
        consent.setData(consentData);
        consent.setRisk(new OBRisk1().paymentContextCode(OBExternalPaymentContext1Code.BILLPAYMENT));
        return consent;
    }

    @Test
    public void testValidDomesticConsent() {
        validateSuccessResult(validator.validate(createValidConsent()));
    }

    @Test
    public void failsValidationWhenInstructedAmountIsInvalid() {
        final OBError1 expectedValidationError = OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("invalid instructed amount");
        final ValidationResult<OBError1> validationResult = new OBWriteDomesticConsent4Validator(new BaseOBValidator<>() {
            @Override
            protected void validate(OBWriteDomestic2DataInitiationInstructedAmount obj, ValidationResult<OBError1> validationResult) {
                validationResult.addError(expectedValidationError);
            }
        }, new OBRisk1Validator(false)).validate(createValidConsent());

        validateErrorResult(validationResult, List.of(expectedValidationError));
    }

    @Test
    public void failsRiskValidation() {
        final OBWriteDomesticConsent4 consent = createValidConsent();
        consent.getRisk().setPaymentContextCode(null);
        final ValidationResult<OBError1> validationResult = new OBWriteDomesticConsent4Validator(createInstructedAmountValidator("GBP"),
                createPaymentContextCodeRiskValidator()).validate(consent);

        validateErrorResult(validationResult, List.of(OBRIErrorType.PAYMENT_CODE_CONTEXT_INVALID.toOBError1()));
    }
}