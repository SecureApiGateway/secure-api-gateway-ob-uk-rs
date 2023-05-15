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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAccountIdentifierConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVrpInstruction;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVrpRequest;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.OBRisk1Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.common.OBCashAccountCreditor3;
import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponse;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParameters;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPInitiation;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRRiskConverter.toOBRisk1;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVrpConverters.toOBDomesticVRPInitiation;

import java.math.BigDecimal;


@Service
@Slf4j
public class DomesticVrpValidationService {

    private static final String INSTRUCTED_AMOUNT_FIELD = "InstructedAmount";
    private static final String MAX_INDIVIDUAL_AMOUNT_FIELD = "MaximumIndividualAmount";
    private OBRisk1Validator riskValidator;

    /**
     * Validate the submit Domestic VRP request parameters
     *
     * @param consent              the consent as saved during the consent creation step
     * @param frDomesticVRPRequest the current submit domestic VRP request
     * @throws OBErrorException
     */
    public void validate(OBDomesticVRPConsentResponse consent, FRDomesticVrpRequest frDomesticVRPRequest) throws OBErrorException {
        this.riskValidator = new OBRisk1Validator(true);

        // Initiation validation
        OBDomesticVRPInitiation consentInitiation = consent.getData().getInitiation();
        OBDomesticVRPInitiation requestInitiation = toOBDomesticVRPInitiation(frDomesticVRPRequest.getData().getInitiation());
        checkInitiationMatch(consentInitiation, requestInitiation);

        // Control parameters validation
        OBDomesticVRPControlParameters controlParameters = consent.getData().getControlParameters();
        checkControlParameters(frDomesticVRPRequest.getData().getInstruction(), controlParameters);

        // Risk validation
        OBRisk1 requestRisk = toOBRisk1(frDomesticVRPRequest.getRisk());
        validateRisk(requestRisk);
        checkRequestAndConsentRiskMatch(consent.getRisk(), requestRisk);

        // Creditor account validation:
        // If the CreditorAccount was not specified in the consent, the CreditorAccount must be specified in the instruction
        OBCashAccountCreditor3 consentCreditorAccount = consent.getData().getInitiation().getCreditorAccount();
        if (consentCreditorAccount == null) {
            checkCreditorAccount(
                    FRAccountIdentifierConverter.toOBCashAccountCreditor3(
                            frDomesticVRPRequest.getData().getInstruction().getCreditorAccount()
                    )
            );
        }

    }

    /**
     * Validate if initiation from consent match with the initiation from the vrp payment submission request
     * @param initiation
     * @param initiationToCompare
     * @throws OBErrorException
     */
    public void checkInitiationMatch(OBDomesticVRPInitiation initiation, OBDomesticVRPInitiation initiationToCompare)
            throws OBErrorException {
        if (!initiation.equals(initiationToCompare)) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_INITIATION_DOESNT_MATCH_CONSENT);
        }
    }

    /**
     * Validate the risk object from the request against the risk from the consent
     * @param consentRisk the consent as saved during the consent creation step
     * @param requestRisk the risk from the current submit domestic vrp request
     * @throws OBErrorException
     */
    public void checkRequestAndConsentRiskMatch(OBRisk1 consentRisk, OBRisk1 requestRisk)
            throws OBErrorException {
        if (!requestRisk.equals(consentRisk)) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_RISK_DOESNT_MATCH_CONSENT);
        }
    }

    //risk - validation

    /**
     * Check if the risk object is valid
     *
     * @param risk the risk from the current submit domestic vrp request
     * @throws OBErrorException
     */
    public void validateRisk(OBRisk1 risk) throws OBErrorException {
        if (riskValidator != null) {
            riskValidator.validate(risk);
        } else {
            String errorString = "No validator!";
            log.error(errorString);
            throw new NullPointerException(errorString);
        }
    }

    /**
     * Validate Creditor account
     *
     * @param creditorAccount creditor account {@link OBCashAccountCreditor3}
     * @throws OBErrorException
     */
    public void checkCreditorAccount(OBCashAccountCreditor3 creditorAccount) throws OBErrorException {
        if (creditorAccount == null) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_CREDITOR_ACCOUNT_NOT_SPECIFIED);
        } else {
            if (creditorAccount.getIdentification() == null || creditorAccount.getName() == null || creditorAccount.getSchemeName() == null) {
                throw new OBErrorException(OBRIErrorType.REQUEST_VRP_CREDITOR_ACCOUNT_NOT_SPECIFIED);
            }
        }
    }

    /**
     * Validates the payment submission control parameters
     *
     * @param instruction       the instruction object for the current submit payment request
     * @param controlParameters the controlParameters from the consent
     * @throws OBErrorException
     */
    public void checkControlParameters(FRDomesticVrpInstruction instruction, OBDomesticVRPControlParameters controlParameters) throws OBErrorException {
        validateMaximumIndividualAmount(instruction, controlParameters);
    }

    /**
     * Validates that payment InstructionAmount is less than or equal to the MaxIndividualAmount controlParameter
     * and that the currency of both amounts is the same.
     *
     * @param paymentInstruction FRDomesticVrpInstruction to validate
     * @param controlParameters OBDomesticVRPControlParameters from the consent to validate against
     * @throws OBErrorException if the paymentInstruction breaches the maxIndividualAmount
     */
    void validateMaximumIndividualAmount(FRDomesticVrpInstruction paymentInstruction, OBDomesticVRPControlParameters controlParameters) throws OBErrorException {
        final BigDecimal instructionAmount = new BigDecimal(paymentInstruction.getInstructedAmount().getAmount());
        final BigDecimal consentAmount = new BigDecimal(controlParameters.getMaximumIndividualAmount().getAmount());

        // InstructionAmount must be less than or equal to the MaximumIndividualAmount consented to
        if (instructionAmount.compareTo(consentAmount) == 1) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_RULES,
                                       INSTRUCTED_AMOUNT_FIELD, MAX_INDIVIDUAL_AMOUNT_FIELD);
        }

        final String consentCurrency = controlParameters.getMaximumIndividualAmount().getCurrency();
        final String instructionCurrency = paymentInstruction.getInstructedAmount().getCurrency();
        if (!consentCurrency.equals(instructionCurrency)) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETER_CURRENCY_MISMATCH,
                    INSTRUCTED_AMOUNT_FIELD, MAX_INDIVIDUAL_AMOUNT_FIELD);
        }
    }

}
