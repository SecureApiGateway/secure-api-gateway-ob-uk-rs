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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRDomesticVrpRequest;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRDomesticVrpRequestData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRWriteDomesticVrpDataInitiation;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.validator.OBRisk1Validator;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.error.StandardErrorCode;
import uk.org.openbanking.datamodel.vrp.*;


import java.util.ArrayList;
import java.util.List;
import java.lang.*;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.common.FRRiskConverter.toOBRisk1;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.vrp.FRDomesticVrpConverters.toOBDomesticVRPInitiation;


@Service
@Slf4j
public class DomesticVrpValidationService {

    protected List<OBError1> errors = new ArrayList<>();
    private OBDomesticVRPConsentRequest request;
    private OBDomesticVRPRequest requestCreditorAccount;

    public DomesticVrpValidationService clearErrors() {
        this.errors.clear();
        return this;
    }

    public List<OBError1> getErrors() {
        return errors;
    }

    private OBRisk1Validator riskValidator;

    public void validate(OBDomesticVRPInitiation initiation, OBDomesticVRPInstruction instruction, OBRisk1 risk, FRDomesticVrpRequest frDomesticVRPRequest) throws OBErrorException {
        this.riskValidator = riskValidator;

        checkRequestAndConsentInitiationMatch(initiation, frDomesticVRPRequest);
        checkRequestAndConsentRiskMatch(risk, frDomesticVRPRequest);
        validateRisk(risk);
        checkCreditorAccountPresentInInstructionIfNotPresentInConsent(requestCreditorAccount, frDomesticVRPRequest);
        //checkControlParameters(instruction, frDomesticVRPRequest);
        // TODO - implement check on creditor account
    }

    //initiation - request vs consent
    public void checkRequestAndConsentInitiationMatch(OBDomesticVRPInitiation requestInitiation, FRDomesticVrpRequest consent)
            throws OBErrorException {
        FRWriteDomesticVrpDataInitiation consentFRInitiation = consent.getData().getInitiation();
        OBDomesticVRPInitiation consentOBInitiation = toOBDomesticVRPInitiation(consentFRInitiation);
        if (!consentOBInitiation.equals(requestInitiation)) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_INITIATION_DOESNT_MATCH_CONSENT);
        }
    }

    //risk - request vs consent
    public void checkRequestAndConsentRiskMatch(OBRisk1 requestRisk, FRDomesticVrpRequest consent)
            throws OBErrorException {
        OBRisk1 consentRisk = toOBRisk1(consent.getRisk());
        if (!requestRisk.equals(consentRisk)) {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_RISK_DOESNT_MATCH_CONSENT);
        }
    }

    //risk - validation
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
    public void checkCreditorAccountPresentInInstructionIfNotPresentInConsent(OBDomesticVRPRequest requestCreditorAccount, FRDomesticVrpRequest consent) throws OBErrorException {
        if (consent.getData().getInitiation().getCreditorAccount() == null) {
            if (requestCreditorAccount.getData().getInstruction().getCreditorAccount() == null) {
                throw new OBErrorException(OBRIErrorType.REQUEST_VRP_CREDITOR_ACCOUNT_NOT_SPECIFIED);
            }
        }
    }

    //controlParameters - validation
    public void checkControlParameters(OBDomesticVRPControlParameters controlParameters, OBDomesticVRPInstruction instruction, FRDomesticVrpRequest consent) throws OBErrorException {
        validatePsuAuthenticationMethods(controlParameters.getPsUAuthenticationMethods());
        validatePsuInteractionTypes(controlParameters.getPsUInteractionTypes());
        validateVRPType(controlParameters.getVrPType());
        validateFromToDateTimes(controlParameters.getValidFromDateTime(), controlParameters.getValidToDateTime());
        validateMaximumIndividualAmount(instruction, consent);
        validatePeriodicLimits(controlParameters.getPeriodicLimits());
    }

    private void validatePeriodicLimits(List<OBDomesticVRPControlParametersPeriodicLimits> periodicLimits) {
    }

    private void validateFromToDateTimes(DateTime validFromDateTime, DateTime validToDateTime) {
    }

    private void validateVRPType(List<String> vrPType) {
    }

    private void validatePsuInteractionTypes(List<OBVRPInteractionTypes> psUInteractionTypes) {
    }

    private void validatePsuAuthenticationMethods(List<String> psUAuthenticationMethods) throws OBErrorException {
    }


    private void validateMaximumIndividualAmount(OBDomesticVRPInstruction instruction, FRDomesticVrpRequest consent) throws OBErrorException {
        String instructionAmount = String.valueOf(instruction.getInstructedAmount().getAmount());
        String instructionCurrency = String.valueOf(instruction.getInstructedAmount().getCurrency());
        // TODO - next method must be implemented correctly depending on the control parameters rules set for the payment periodic limits and maximum
        // individual ammount
        validateMaximumIndividualAmount(request, consent, Double.valueOf(instructionAmount), Double.valueOf(instructionCurrency));
    }

    //MaximumIndividualAmount - validation
    private void validateMaximumIndividualAmount(OBDomesticVRPConsentRequest request, FRDomesticVrpRequest consent, Double instructionAmount, Double instructionCurrency) throws OBErrorException {
        OBDomesticVRPControlParameters controlParameters = request.getData().getControlParameters();
        Double consentAmount = Double.valueOf(controlParameters.getMaximumIndividualAmount().getAmount());
        Double consentCurrency = Double.valueOf(controlParameters.getMaximumIndividualAmount().getCurrency());
        if ((instructionAmount.compareTo(consentAmount) > 0) && (instructionCurrency.compareTo(consentCurrency) > 0)) {
            throw new OBErrorException(
                    OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_RULES);
        }
    }
}
