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

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;

class OBDomesticVRPConsentRequestValidatorTest {

    private final OBDomesticVRPConsentRequestValidator validator = new OBDomesticVRPConsentRequestValidator();

    private static OBDomesticVRPConsentRequest createValidConsent() {
        return OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequestMandatoryFields();
    }

    @Test
    void validConsentPasses() {
        validateSuccessResult(validator.validate(createValidConsent()));
    }

    @Test
    void failsForUnsupportedVrpType() {
        List<List<String>> unsupportedVrpTypes = List.of(
                List.of(),
                List.of("UK.OBIE.VRPType.Other"),
                List.of("Some other type", "UK.OBIE.VRPType.Other"),
                List.of("UK.OBIE.VRPType.Sweeping", "UK.OBIE.VRPType.Other"),
                List.of("UK.OBIE.VRPType.Sweeping", "UK.OBIE.VRPType.Sweeping")
        );

        final OBDomesticVRPConsentRequest consent = createValidConsent();
        for (final List<String> unsupportedVrpType : unsupportedVrpTypes) {
            consent.getData().getControlParameters().setVrPType(unsupportedVrpType);
            validateErrorResult(validator.validate(consent), List.of(OBRIErrorType.DATA_INVALID_REQUEST
                    .toOBError1("VRPType specified is not supported, only the following types are supported: UK.OBIE.VRPType.Sweeping")));
        }
        consent.getData().getControlParameters().setVrPType(null);
        validateErrorResult(validator.validate(consent), List.of(OBRIErrorType.DATA_INVALID_REQUEST
                .toOBError1("VRPType specified is not supported, only the following types are supported: UK.OBIE.VRPType.Sweeping")));
    }

}