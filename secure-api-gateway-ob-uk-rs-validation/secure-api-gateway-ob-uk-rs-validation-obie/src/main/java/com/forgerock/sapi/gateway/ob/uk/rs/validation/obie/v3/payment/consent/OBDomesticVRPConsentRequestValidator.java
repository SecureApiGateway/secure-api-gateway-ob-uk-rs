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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent;

import java.util.List;
import java.util.Objects;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;

import uk.org.openbanking.datamodel.v3.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPControlParameters;

public class OBDomesticVRPConsentRequestValidator extends BaseOBValidator<OBDomesticVRPConsentRequest> {

    private static final String SWEEPING_VRP_TYPE = "UK.OBIE.VRPType.Sweeping";

    private final BaseOBValidator<OBRisk1> riskValidator;

    public OBDomesticVRPConsentRequestValidator(BaseOBValidator<OBRisk1> riskValidator) {
        this.riskValidator = Objects.requireNonNull(riskValidator, "riskValidator must be supplied");
    }

    @Override
    protected void validate(OBDomesticVRPConsentRequest obj, ValidationResult<OBError1> validationResult) {
        validationResult.mergeResults(riskValidator.validate(obj.getRisk()));

        final OBDomesticVRPControlParameters controlParameters = obj.getData().getControlParameters();
        validateVrpType(controlParameters.getVrPType(), validationResult);
    }

    /**
     * Only Sweeping VRPs are currently supported, the VRPType list must contain only this type.
     */
    private void validateVrpType(List<String> vrpType, ValidationResult<OBError1> validationResult) {
        if (vrpType == null || vrpType.size() != 1 || !vrpType.get(0).equals(SWEEPING_VRP_TYPE)) {
            validationResult.addError(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                    String.format("VRPType specified is not supported, only the following types are supported: %s", SWEEPING_VRP_TYPE)));
        }
    }
}
