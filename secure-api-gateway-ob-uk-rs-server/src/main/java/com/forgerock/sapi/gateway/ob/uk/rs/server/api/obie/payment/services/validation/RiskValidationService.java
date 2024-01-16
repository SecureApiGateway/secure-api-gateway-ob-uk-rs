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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.OBRisk1Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.common.OBExternalPaymentContext1Code;
import uk.org.openbanking.datamodel.common.OBRisk1;

import java.util.Objects;

@Service
@Slf4j
public class RiskValidationService {

    private OBRisk1Validator riskValidator;

    /**
     * Validate the submit Payment/Standing Order request Risk object against the consent Risk object
     *
     * @param consentRisk the consent as saved during the consent creation step
     * @param requestRisk the risk from the current submit Payment/Standing Order request
     * @throws OBErrorException
     */

    public void validate(OBRisk1 consentRisk, OBRisk1 requestRisk) throws OBErrorException {
        this.riskValidator = new OBRisk1Validator(true);

        // Validate Risk object
        if (requestRisk == null) {
            throw new OBErrorException(OBRIErrorType.PAYMENT_INVALID_RISK, "Risk is mandatory and cannot be null");
        }

        checkRequestAndConsentRiskPaymentContext1CodeMatch(consentRisk.getPaymentContextCode(), requestRisk.getPaymentContextCode());
        checkRequestAndConsentRiskMerchantCategoryCodeMatch(consentRisk.getMerchantCategoryCode(), requestRisk.getMerchantCategoryCode());
        checkRequestAndConsentRiskPaymentMerchantCustomerIdentificationMatch(consentRisk.getMerchantCustomerIdentification(), requestRisk.getMerchantCustomerIdentification());
    }
    
    private void checkRequestAndConsentRiskPaymentContext1CodeMatch(OBExternalPaymentContext1Code paymentContextCode, OBExternalPaymentContext1Code paymentContextCode1) throws OBErrorException {
        String reason = "The property 'paymentContextCode' value does not match with the value provided in the consent";
        if (paymentContextCode != null && paymentContextCode1 != null && !paymentContextCode.equals(paymentContextCode1)) {
            throwError(reason);
        } else if (paymentContextCode == null && paymentContextCode1 != null) {
            throwError(reason);
        } else if (paymentContextCode != null && paymentContextCode1 == null) {
            throwError(reason);
        }
    }

    private void checkRequestAndConsentRiskMerchantCategoryCodeMatch(String merchantCategoryCode, String merchantCategoryCode1) throws OBErrorException {
        String reason = "The property 'merchantCategoryCode' value does not match with the value provided in the consent";
        if (merchantCategoryCode != null && merchantCategoryCode1 != null && !merchantCategoryCode.equals(merchantCategoryCode1)) {
            throwError(reason);
        } else if (merchantCategoryCode == null && merchantCategoryCode1 != null) {
            throwError(reason);
        } else if (merchantCategoryCode != null && merchantCategoryCode1 == null) {
            throwError(reason);
        }
    }

    private void checkRequestAndConsentRiskPaymentMerchantCustomerIdentificationMatch(String merchantCustomerIdentification, String merchantCustomerIdentification1) throws OBErrorException {
        String reason = "The property 'merchantCustomerIdentification' value does not match with the value provided in the consent";
        if (merchantCustomerIdentification != null && merchantCustomerIdentification1 != null && !merchantCustomerIdentification.equals(merchantCustomerIdentification1)) {
            throwError(reason);
        } else if (merchantCustomerIdentification == null && merchantCustomerIdentification1 != null) {
            throwError(reason);
        } else if (merchantCustomerIdentification != null && merchantCustomerIdentification1 == null) {
            throwError(reason);
        }
    }

    protected void throwError(String reason) throws OBErrorException {
        log.error(reason);
        throw new OBErrorException(OBRIErrorType.PAYMENT_INVALID_RISK, reason);
    }
}
