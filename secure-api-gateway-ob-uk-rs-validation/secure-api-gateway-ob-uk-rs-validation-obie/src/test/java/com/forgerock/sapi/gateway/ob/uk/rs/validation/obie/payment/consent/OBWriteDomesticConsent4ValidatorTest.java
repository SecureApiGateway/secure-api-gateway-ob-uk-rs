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
import java.util.Set;

import org.junit.jupiter.api.Test;

import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4Data;

class OBWriteDomesticConsent4ValidatorTest {

    final OBWriteDomesticConsent4Validator validator = new OBWriteDomesticConsent4Validator(Set.of("GBP", "EUR", "USD"));

    private static OBWriteDomesticConsent4 createValidConsent() {
        final OBWriteDomesticConsent4 consent = new OBWriteDomesticConsent4();
        final OBWriteDomesticConsent4Data consentData = new OBWriteDomesticConsent4Data();
        final OBWriteDomestic2DataInitiation initiation = new OBWriteDomestic2DataInitiation();
        initiation.setInstructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount().amount("12.99").currency("GBP"));
        consentData.setInitiation(initiation);
        consent.setData(consentData);
        consent.setRisk(new OBRisk1());
        return consent;
    }

    @Test
    public void testValidDomesticConsent() {
        validateSuccessResult(validator.validate(createValidConsent()));
    }

    @Test
    public void failsValidationWhenInstructedAmountCurrencyInvalid() {
        final String invalidCcy = "ZZZ";
        final OBWriteDomesticConsent4 consent = createValidConsent();
        consent.getData().getInitiation().getInstructedAmount().setCurrency(invalidCcy);

        validateErrorResult(validator.validate(consent), List.of(new OBError1().errorCode("OBRI.Data.Request.Invalid")
                .message("Your data request is invalid: reason The currency " + invalidCcy + " provided is not supported")));
    }

    @Test
    public void failsValidationWhenInstructedAmountLessThanOrEqualToZero() {
        final String[] invalidAmounts = new String[] {
                "-1000.12", "-0.01", "0", "0.0"
        };

        for (String invalidAmount : invalidAmounts) {
            final OBWriteDomesticConsent4 consent = createValidConsent();
            consent.getData().getInitiation().getInstructedAmount().setAmount(invalidAmount);

            validateErrorResult(validator.validate(consent), List.of(new OBError1().errorCode("OBRI.Data.Request.Invalid")
                    .message("Your data request is invalid: reason The amount " + invalidAmount + " provided must be greater than 0")));
        }
    }

}