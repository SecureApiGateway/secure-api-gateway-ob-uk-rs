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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBVRPFundsConfirmationRequestValidator.VRPFundsConfirmationValidationContext;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponseData.StatusEnum;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequest;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequestData;

/**
 * Validator of OBVRPFundsConfirmationRequest objects.
 *
 * Validates the following:
 * - consentId from the access_token matches the consentId inside the OBVRPFundsConfirmationRequest
 * - consentStatus is Authorised
 */
public class OBVRPFundsConfirmationRequestValidator extends BaseOBValidator<VRPFundsConfirmationValidationContext> {

    public static class VRPFundsConfirmationValidationContext {
        /**
         * This is the consentId obtained from the access_token
         */
        private final String consentId;
        private final String consentStatus;
        private final OBVRPFundsConfirmationRequest vrpFundsConfirmationRequest;

        public VRPFundsConfirmationValidationContext(String consentId, String consentStatus,
                                                     OBVRPFundsConfirmationRequest vrpFundsConfirmationRequest) {
            this.consentId = consentId;
            this.consentStatus = consentStatus;
            this.vrpFundsConfirmationRequest = vrpFundsConfirmationRequest;
        }

        public String getConsentId() {
            return consentId;
        }

        public String getConsentStatus() {
            return consentStatus;
        }

        public OBVRPFundsConfirmationRequest getVrpFundsConfirmationRequest() {
            return vrpFundsConfirmationRequest;
        }
    }

    @Override
    protected void validate(VRPFundsConfirmationValidationContext fundsConfValidationCtxt, ValidationResult<OBError1> validationResult) {
        final OBVRPFundsConfirmationRequestData fundsConfirmationRequestData = fundsConfValidationCtxt.getVrpFundsConfirmationRequest().getData();
        if (!fundsConfirmationRequestData.getConsentId().equals(fundsConfValidationCtxt.consentId)) {
            validationResult.addError(OBRIErrorType.REQUEST_OBJECT_INVALID.toOBError1("The consentId provided in the body doesn't match with the consent id provided as parameter"));
        }

        final String consentStatus = fundsConfValidationCtxt.getConsentStatus();
        if (!StatusEnum.AUTHORISED.toString().equals(consentStatus)) {
            validationResult.addError(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(consentStatus));
        }
    }
}
