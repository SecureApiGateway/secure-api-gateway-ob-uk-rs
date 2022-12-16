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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.vrp.FRWriteDomesticVrpDataInitiation;
import com.forgerock.securebanking.openbanking.uk.rs.validator.OBRisk1Validator;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;
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

    public DomesticVrpValidationService clearErrors() {
        this.errors.clear();
        return this;
    }

    public List<OBError1> getErrors() {
        return errors;
    }

    private OBRisk1Validator riskValidator;

    public void validate(OBDomesticVRPInitiation initiation, OBDomesticVRPInstruction instruction, OBRisk1 risk) {
        this.riskValidator = riskValidator;

        //checkRequestAndConsentInitiationMatch();
        //checkRequestAndConsentRiskMatch();
        //validateRisk(Request.getRisk());
        //checkControlParameters();
        //checkCreditorAccountExistence();
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
    public void checkRequestAndConsentRiskMatch(OBDomesticVRPRequest request, FRDomesticVrpRequest consent)
            throws OBErrorException {
        OBRisk1 requestRisk = request.getRisk();
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

    //controlParameters - validation
    public void checkControlParameters(OBDomesticVRPRequest request, FRDomesticVrpRequest consent) throws OBErrorException {
        validateMaximumIndividualAmount(request, consent);
        Object limit = new Object();//when a payment would breach a limitation set by one or more ControlParameters case
        if (limit != null) {
            //checkLimitations.makeRequest(limit, consent);
        }
    }

    private void validateMaximumIndividualAmount(OBDomesticVRPRequest request, FRDomesticVrpRequest consent) throws OBErrorException {
        String instructionAmount = String.valueOf(request.getData().getInstruction().getInstructedAmount().getAmount());
        String instructionCurrency = String.valueOf(request.getData().getInstruction().getInstructedAmount().getCurrency());
        validateMaximumIndividualAmount(consent, Double.valueOf(instructionAmount), Double.valueOf(instructionCurrency));
    }

    private void validateMaximumIndividualAmount(FRDomesticVrpRequest consent, Double instructionAmount, Double instructionCurrency) {
        /*get controlParameters
        String consentAmount = String.valueOf(consent.controlParameters.getMaximumIndividualAmount().getAmount());
        String currencyAmount = String.valueOf(consent.controlParameters.getMaximumIndividualAmount().getCurrency());
        if (instructionAmount.compareTo(consentAmount)) {
            throw new OBErrorException(
                    OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_RULES,
                    pass in the control parameter field that caused the error in the Field field of the error message;
        );
        }
    }*/
    }

    //if the CreditorAccount was not specified in the the consent, the CreditorAccount must be specified in the instruction
    public void checkCreditorAccountExistence(OBDomesticVRPRequest request, FRDomesticVrpRequest consent) throws OBErrorException {
        if (consent.getData().getInitiation().getCreditorAccount() == null) {
            if (request.getData().getInstruction().getCreditorAccount() == null) {
                throw new OBErrorException(OBRIErrorType.REQUEST_VRP_CREDITOR_ACCOUNT_NOT_SPECIFIED);
            }
        }
    }
}
