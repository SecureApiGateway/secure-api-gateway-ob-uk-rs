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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBVRPFundsConfirmationRequestValidator.VRPFundsConfirmationValidationContext;

import uk.org.openbanking.datamodel.v3.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentResponseDataStatus;
import uk.org.openbanking.datamodel.v3.vrp.OBVRPFundsConfirmationRequest;
import uk.org.openbanking.datamodel.v3.vrp.OBVRPFundsConfirmationRequestData;

class OBVRPFundsConfirmationRequestValidatorTest {

    private final OBVRPFundsConfirmationRequestValidator validator = new OBVRPFundsConfirmationRequestValidator();

    private OBVRPFundsConfirmationRequest createValidFundsConfirmationRequest(String consentId, BigDecimal amount) {
        return new OBVRPFundsConfirmationRequest()
                .data(new OBVRPFundsConfirmationRequestData()
                            .consentId(consentId)
                            .reference("reference")
                            .instructedAmount(new OBActiveOrHistoricCurrencyAndAmount()
                                                    .amount(amount.toPlainString())
                                                    .currency("GBP")));
    }


    @Test
    void testValidConsentPasses() {
        final String consentId = "1234";
        final OBVRPFundsConfirmationRequest fundsConfRequest = createValidFundsConfirmationRequest(consentId, new BigDecimal("99.21"));
        validateSuccessResult(validator.validate(new VRPFundsConfirmationValidationContext(consentId, "Authorised", fundsConfRequest)));
    }

    @Test
    void failsWhenConsentIdMismatch() {
        final OBVRPFundsConfirmationRequest fundsConfRequest = createValidFundsConfirmationRequest("9999", new BigDecimal("99.21"));
        final ValidationResult<OBError1> validationResult = validator.validate(new VRPFundsConfirmationValidationContext("1111", "Authorised", fundsConfRequest));
        validateErrorResult(validationResult, List.of(OBRIErrorType.REQUEST_OBJECT_INVALID.toOBError1("The consentId provided in the body doesn't match with the consent id provided as parameter")));
    }

    @Test
    void failsWhenConsentNotAuthorised() {
        final String consentId = "1234";
        final OBVRPFundsConfirmationRequest fundsConfRequest = createValidFundsConfirmationRequest(consentId, new BigDecimal("99.21"));
        final String[] invalidStatuses = new String[] {
                OBDomesticVRPConsentResponseDataStatus.AWAITINGAUTHORISATION.toString(), OBDomesticVRPConsentResponseDataStatus.REJECTED.toString()
        };

        for (final String invalidStatus : invalidStatuses) {
            final ValidationResult<OBError1> validationResult = validator.validate(new VRPFundsConfirmationValidationContext(consentId, invalidStatus, fundsConfRequest));
            validateErrorResult(validationResult, List.of(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(invalidStatus)));
        }
    }

}