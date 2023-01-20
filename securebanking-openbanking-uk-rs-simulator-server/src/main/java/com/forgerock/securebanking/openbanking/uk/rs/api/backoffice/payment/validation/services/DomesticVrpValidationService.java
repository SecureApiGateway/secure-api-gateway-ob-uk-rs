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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation.services;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRDomesticVrpInstruction;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRDomesticVrpRequest;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRWriteDomesticVrpDataInitiation;
import com.forgerock.securebanking.openbanking.uk.rs.validator.OBRisk1Validator;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.common.OBCashAccountCreditor3;
import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.vrp.*;


import java.lang.*;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.common.FRRiskConverter.toOBRisk1;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.vrp.FRDomesticVrpConverters.toOBDomesticVRPInitiation;


@Service
@Slf4j
public class DomesticVrpValidationService {

    private static final String MAX_INDIVIDUAL_AMOUNT = "MaximumIndividualAmount";
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
        OBDomesticVRPInitiation initiation = consent.getData().getInitiation();
        checkRequestAndConsentInitiationMatch(initiation, frDomesticVRPRequest);

        // Control parameters validation
        OBDomesticVRPControlParameters controlParameters = consent.getData().getControlParameters();
        checkControlParameters(frDomesticVRPRequest.getData().getInstruction(), controlParameters);

        // Risk validation
        OBRisk1 risk = consent.getRisk();
        validateRisk(risk);
        checkRequestAndConsentRiskMatch(risk, frDomesticVRPRequest);

        // Creditor account validation
        OBCashAccountCreditor3 requestCreditorAccount = consent.getData().getInitiation().getCreditorAccount();
        checkCreditorAccountPresentInInstructionIfNotPresentInConsent(requestCreditorAccount);

    }

    /**
     * Validate the initiation from the request against the parameters from the consent
     *
     * @param requestInitiation the initiation from the current submit domestic vrp request
     * @param consent           the consent as saved during the consent creation step
     * @throws OBErrorException
     */
    public void checkRequestAndConsentInitiationMatch(OBDomesticVRPInitiation requestInitiation, FRDomesticVrpRequest consent)
            throws OBErrorException {
        FRWriteDomesticVrpDataInitiation consentFRInitiation = consent.getData().getInitiation();
        OBDomesticVRPInitiation consentOBInitiation = toOBDomesticVRPInitiation(consentFRInitiation);
        if (!consentOBInitiation.equals(requestInitiation)) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_INITIATION_DOESNT_MATCH_CONSENT);
        }
    }

    /**
     * Validate the risk object from the request against the risk from the consent
     *
     * @param requestRisk the risk from the current submit domestic vrp request
     * @param consent     the consent as saved during the consent creation step
     * @throws OBErrorException
     */
    public void checkRequestAndConsentRiskMatch(OBRisk1 requestRisk, FRDomesticVrpRequest consent)
            throws OBErrorException {
        OBRisk1 consentRisk = toOBRisk1(consent.getRisk());
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

    //if the CreditorAccount was not specified in the the consent, the CreditorAccount must be specified in the instruction

    /**
     * Check if the CreditorAccount was specified or not in the consent. If the CreditorAccount was
     * not specified in the consent, the CreditorAccount must be specified in the instruction
     *
     * @param requestCreditorAccount
     * @throws OBErrorException
     */
    public void checkCreditorAccountPresentInInstructionIfNotPresentInConsent(OBCashAccountCreditor3 requestCreditorAccount) throws OBErrorException {
        if (requestCreditorAccount == null) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_CREDITOR_ACCOUNT_NOT_SPECIFIED);
        } else {
            if (requestCreditorAccount.getIdentification() == null || requestCreditorAccount.getName() == null || requestCreditorAccount.getSchemeName() == null) {
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

    private void validateMaximumIndividualAmount(FRDomesticVrpInstruction instruction, OBDomesticVRPControlParameters controlParameters) throws OBErrorException {
        String instructionAmount = instruction.getInstructedAmount().getAmount();
        String instructionCurrency = instruction.getInstructedAmount().getCurrency();

        Double consentAmount = Double.valueOf(controlParameters.getMaximumIndividualAmount().getAmount());
        String consentCurrency = controlParameters.getMaximumIndividualAmount().getCurrency();
        if (!(Double.valueOf(instructionAmount).compareTo(consentAmount) == 0) || !(instructionCurrency.compareTo(consentCurrency) == 0)) {
            throw new OBErrorException(
                    OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_RULES,
                    MAX_INDIVIDUAL_AMOUNT, MAX_INDIVIDUAL_AMOUNT);
        }
    }

}
