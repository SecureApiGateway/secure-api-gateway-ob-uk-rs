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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment;

import java.math.BigDecimal;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBDomesticVRPRequestValidator.OBDomesticVRPRequestValidationContext;

import uk.org.openbanking.datamodel.v3.common.OBCashAccountCreditor3;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPControlParameters;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPInitiation;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPInstruction;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPRequest;

/**
 * Validator of OBDomesticVRPRequest objects (VRP Payment Requests).
 *
 * VRP specific validation implemented:
 * - If the consent supplied a CreditorAccount, then verify that the Payment Instruction CreditorAccount matches
 * - Validate Payment Instruction against Consent ControlParameters
 *   - Validate MaxIndividualAmount
 */
public class OBDomesticVRPRequestValidator extends BasePaymentRequestValidator<OBDomesticVRPRequestValidationContext, OBDomesticVRPRequest, OBDomesticVRPInitiation> {

    private static final String INSTRUCTED_AMOUNT_FIELD = "InstructedAmount";
    private static final String MAX_INDIVIDUAL_AMOUNT_FIELD = "MaximumIndividualAmount";

    public static class OBDomesticVRPRequestValidationContext extends PaymentRequestValidationContext<OBDomesticVRPRequest, OBDomesticVRPInitiation> {

        private final OBDomesticVRPControlParameters controlParameters;

        public OBDomesticVRPRequestValidationContext(OBDomesticVRPRequest paymentRequest, OBDomesticVRPConsentRequest consentRequest, String consentStatus) {
            super(paymentRequest, () -> paymentRequest.getData().getInitiation(), paymentRequest::getRisk,
                    consentStatus, () -> consentRequest.getData().getInitiation(), consentRequest::getRisk);
            this.controlParameters = consentRequest.getData().getControlParameters();
        }

        public OBDomesticVRPControlParameters getControlParameters() {
            return controlParameters;
        }
    }

    @Override
    protected void doPaymentSpecificValidation(OBDomesticVRPRequestValidationContext paymentReqValidationCtxt, ValidationResult<OBError1> validationResult) {
        // No additional rules for OBDomesticVRPRequest currently, add validation rules as required.
       checkCreditorAccount(paymentReqValidationCtxt, validationResult);
       validatePaymentInstructionAgainstControlParams(paymentReqValidationCtxt.getPaymentRequest().getData().getInstruction(),
                              paymentReqValidationCtxt.getControlParameters(), validationResult);
    }

    /**
     * CreditorAccount is optional in the Consent Request, if it has been set then verify that it matches the Payment Instruction's CreditorAccount
     */
    void checkCreditorAccount(OBDomesticVRPRequestValidationContext paymentReqValidationCtxt, ValidationResult<OBError1> validationResult) {
        final OBCashAccountCreditor3 consentInitiationCreditorAccount = paymentReqValidationCtxt.getConsentInitiation().getCreditorAccount();
        if (consentInitiationCreditorAccount != null) {
            if (!consentInitiationCreditorAccount.equals(paymentReqValidationCtxt.getPaymentRequest().getData().getInstruction().getCreditorAccount())) {
                validationResult.addError(OBRIErrorType.REQUEST_VRP_CREDITOR_ACCOUNT_DOES_NOT_MATCH_CONSENT.toOBError1());
            }
        }
       
    }

    void validatePaymentInstructionAgainstControlParams(OBDomesticVRPInstruction paymentInstruction,
                                                        OBDomesticVRPControlParameters controlParameters,
                                                        ValidationResult<OBError1> validationResult) {

        validateMaxIndividualAmountControlParam(paymentInstruction, controlParameters, validationResult);
    }

    void validateMaxIndividualAmountControlParam(OBDomesticVRPInstruction paymentInstruction,
                                                 OBDomesticVRPControlParameters controlParameters,
                                                 ValidationResult<OBError1> validationResult) {

        final BigDecimal instructionAmount = new BigDecimal(paymentInstruction.getInstructedAmount().getAmount());
        final BigDecimal consentAmount = new BigDecimal(controlParameters.getMaximumIndividualAmount().getAmount());

        // InstructionAmount must be less than or equal to the MaximumIndividualAmount consented to
        if (instructionAmount.compareTo(consentAmount) > 0) {
            validationResult.addError(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_RULES.toOBError1(INSTRUCTED_AMOUNT_FIELD, MAX_INDIVIDUAL_AMOUNT_FIELD));
        }

        final String consentCurrency = controlParameters.getMaximumIndividualAmount().getCurrency();
        final String instructionCurrency = paymentInstruction.getInstructedAmount().getCurrency();
        if (!consentCurrency.equals(instructionCurrency)) {
            validationResult.addError(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETER_CURRENCY_MISMATCH.toOBError1(INSTRUCTED_AMOUNT_FIELD, MAX_INDIVIDUAL_AMOUNT_FIELD));
        }
    }

}
