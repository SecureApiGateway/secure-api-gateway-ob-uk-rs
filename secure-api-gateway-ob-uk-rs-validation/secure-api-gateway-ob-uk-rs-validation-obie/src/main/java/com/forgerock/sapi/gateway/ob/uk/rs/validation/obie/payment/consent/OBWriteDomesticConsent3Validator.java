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

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidator;

import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent3;

public class OBWriteDomesticConsent3Validator extends PaymentConsentValidator<OBWriteDomesticConsent3> {

    public OBWriteDomesticConsent3Validator(OBValidator<OBRisk1> riskValidator) {
        super(riskValidator);
    }

    @Override
    protected void validate(OBWriteDomesticConsent3 domesticPaymentConsent, ValidationResult<OBError1> validationResult) {
        validateOBRisk1(domesticPaymentConsent.getRisk(), validationResult);
    }
}
