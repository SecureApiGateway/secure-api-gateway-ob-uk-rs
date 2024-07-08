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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBDomesticVRPRequestValidator.OBDomesticVRPRequestValidationContext;

import uk.org.openbanking.datamodel.v3.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPControlParameters;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPInstruction;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPRequest;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpRequestTestDataFactory;

class OBDomesticVRPRequestValidatorTest {

    private final OBDomesticVRPRequestValidator validator = new OBDomesticVRPRequestValidator();

    @Test
    void testCreditorAccountValidationConsentWithoutCreditorAcc() {
        final OBDomesticVRPRequest vrpRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        final OBDomesticVRPConsentRequest consent = OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequestMandatoryFields();
        assertNull(consent.getData().getInitiation().getCreditorAccount());

        final ValidationResult<OBError1> validationResult = new ValidationResult<>();
        validator.checkCreditorAccount(new OBDomesticVRPRequestValidationContext(vrpRequest, consent, "Authorised"), validationResult);

        validateSuccessResult(validationResult);
    }

    @Test
    void testCreditorAccountMatchesConsent() {
        final OBDomesticVRPRequest vrpRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        final OBDomesticVRPConsentRequest consent = OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest();
        assertNotNull(consent.getData().getInitiation().getCreditorAccount());

        final ValidationResult<OBError1> validationResult = new ValidationResult<>();
        validator.checkCreditorAccount(new OBDomesticVRPRequestValidationContext(vrpRequest, consent, "Authorised"), validationResult);

        validateSuccessResult(validationResult);
    }

    @Test
    void testCreditorAccountDoesNotMatchConsent() {
        final OBDomesticVRPRequest vrpRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        vrpRequest.getData().getInstruction().getCreditorAccount().setName("name does not match consent");
        final OBDomesticVRPConsentRequest consent = OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest();

        final ValidationResult<OBError1> validationResult = new ValidationResult<>();
        validator.checkCreditorAccount(new OBDomesticVRPRequestValidationContext(vrpRequest, consent, "Authorised"), validationResult);

        validateErrorResult(validationResult, List.of(OBRIErrorType.REQUEST_VRP_CREDITOR_ACCOUNT_DOES_NOT_MATCH_CONSENT.toOBError1()));
    }

    @Test
    void testMaxIndividualAmountValid() {
        final String maxIndividualAmount = "1.01";
        final String[] validPaymentInstructionAmounts = new String[]{
                maxIndividualAmount,
                "1.00",
                "0.99",
                "0.51",
                "0.01"
        };
        final String currency = "GBP";
        final OBDomesticVRPControlParameters vrpControlParameters = createControlParameters(maxIndividualAmount, currency);

        for (String validPaymentInstructionAmount : validPaymentInstructionAmounts) {
            final OBDomesticVRPInstruction paymentInstruction = createVRPInstruction(validPaymentInstructionAmount, currency);

            final ValidationResult<OBError1> validationResult = new ValidationResult<>();
            validator.validateMaxIndividualAmountControlParam(paymentInstruction, vrpControlParameters, validationResult);
            validateSuccessResult(validationResult);
        }
    }

    @Test
    void testBreachMaxIndividualAmountThrowsException() {
        final String maxIndividualAmount = "100.99";
        final String[] invalidPaymentInstructionAmounts = new String[]{
                "101.00",
                "121212.33",
                "3333333.99"
        };
        final String currency = "GBP";
        final OBDomesticVRPControlParameters vrpControlParameters = createControlParameters(maxIndividualAmount, currency);

        for (String invalidAmount : invalidPaymentInstructionAmounts) {
            final OBDomesticVRPInstruction paymentInstruction = createVRPInstruction(invalidAmount, currency);

            final ValidationResult<OBError1> validationResult = new ValidationResult<>();
            validator.validateMaxIndividualAmountControlParam(paymentInstruction, vrpControlParameters, validationResult);
            validateErrorResult(validationResult, List.of(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_RULES.toOBError1("InstructedAmount", "MaximumIndividualAmount")));
        }
    }

    @Test
    void testMaxIndividualAmountCurrencyMismatchThrowsException() {
        final String maxIndividualAmount = "100.99";
        final OBDomesticVRPControlParameters vrpControlParameters = createControlParameters(maxIndividualAmount, "GBP");

        final OBDomesticVRPInstruction instruction = createVRPInstruction(maxIndividualAmount, "USD");

        final ValidationResult<OBError1> validationResult = new ValidationResult<>();
        validator.validateMaxIndividualAmountControlParam(instruction, vrpControlParameters, validationResult);
        validateErrorResult(validationResult, List.of(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETER_CURRENCY_MISMATCH.toOBError1("InstructedAmount", "MaximumIndividualAmount")));
    }

    private static OBDomesticVRPInstruction createVRPInstruction(String amount, String currency) {
        return new OBDomesticVRPInstruction().instructedAmount(new OBActiveOrHistoricCurrencyAndAmount().amount(amount).currency(currency));
    }

    private static OBDomesticVRPControlParameters createControlParameters(String maxIndividualAmount, String currency) {
        final OBDomesticVRPControlParameters vrpControlParameters = new OBDomesticVRPControlParameters();
        final OBActiveOrHistoricCurrencyAndAmount maximumIndividualAmount = new OBActiveOrHistoricCurrencyAndAmount();
        maximumIndividualAmount.setCurrency(currency);
        maximumIndividualAmount.amount(maxIndividualAmount);
        vrpControlParameters.setMaximumIndividualAmount(maximumIndividualAmount);
        return vrpControlParameters;
    }

}