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
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.vrp.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


import java.util.ArrayList;
import java.util.List;

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

    public void validate(OBDomesticVRPInitiation initiation, OBDomesticVRPInstruction instruction, OBRisk1 risk) {

        //checkRequestAndConsentInitiationMatch();
        //checkRequestAndConsentRiskMatch();
        //validateRisk(Request.getRisk());
        //checkControlParameters();
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

    }


}
