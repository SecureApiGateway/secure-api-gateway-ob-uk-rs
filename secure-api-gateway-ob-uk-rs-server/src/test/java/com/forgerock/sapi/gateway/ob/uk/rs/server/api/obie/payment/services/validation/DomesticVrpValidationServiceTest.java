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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVrpInstruction;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVrpInstruction.FRDomesticVrpInstructionBuilder;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;

import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParameters;

class DomesticVrpValidationServiceTest {

    @Test
    void testMaxIndividualAmountValid() throws Exception {
        final String maxIndividualAmount = "1.01";
        final String[] validPaymentInstructionAmounts = new String[]{
                maxIndividualAmount,
                "1.00",
                "0.99",
                "0.51",
                "0.01"
        };
        final String currency = "GBP";
        final OBDomesticVRPControlParameters vrpControlParameters = createControlParameters(maxIndividualAmount, currency);

        final DomesticVrpValidationService vrpValidationService = new DomesticVrpValidationService();
        for (String validPaymentInstructionAmount : validPaymentInstructionAmounts) {
            final FRDomesticVrpInstructionBuilder vrpInstructionBuilder = FRDomesticVrpInstruction.builder().instructedAmount(
                    FRAmount.builder().amount(validPaymentInstructionAmount).currency(currency).build());

            // Valid - no exception thrown
            vrpValidationService.validateMaximumIndividualAmount(vrpInstructionBuilder.build(), vrpControlParameters);
        }
    }

    @Test
    void testBreachMaxIndividualAmountThrowsException() {
        final String maxIndividualAmount = "100.99";
        final String[] validPaymentInstructionAmounts = new String[]{
                "101.00",
                "121212.33",
                "3333333.99"
        };
        final String currency = "GBP";
        final OBDomesticVRPControlParameters vrpControlParameters = createControlParameters(maxIndividualAmount, currency);

        final DomesticVrpValidationService vrpValidationService = new DomesticVrpValidationService();
        for (String validPaymentInstructionAmount : validPaymentInstructionAmounts) {
            final FRDomesticVrpInstructionBuilder vrpInstructionBuilder = FRDomesticVrpInstruction.builder().instructedAmount(
                    FRAmount.builder().amount(validPaymentInstructionAmount).currency(currency).build());

            final OBErrorException obErrorException = assertThrows(OBErrorException.class,
                    () -> vrpValidationService.validateMaximumIndividualAmount(vrpInstructionBuilder.build(), vrpControlParameters));
            assertEquals("The field 'InstructedAmount' breaches a limitation set by 'MaximumIndividualAmount'",
                         obErrorException.getMessage());
        }
    }

    @Test
    void testMaxIndividualAmountCurrencyMismatchThrowsException() {
        final String maxIndividualAmount = "100.99";
        final OBDomesticVRPControlParameters vrpControlParameters = createControlParameters(maxIndividualAmount, "GBP");

        final DomesticVrpValidationService vrpValidationService = new DomesticVrpValidationService();
        final FRDomesticVrpInstructionBuilder vrpInstructionBuilder = FRDomesticVrpInstruction.builder().instructedAmount(
                FRAmount.builder().amount(maxIndividualAmount).currency("EUR").build());

        final OBErrorException obErrorException = assertThrows(OBErrorException.class,
                () -> vrpValidationService.validateMaximumIndividualAmount(vrpInstructionBuilder.build(), vrpControlParameters));
        assertEquals("The currency of field 'InstructedAmount' must match the currency of consent control parameter field 'MaximumIndividualAmount'",
                     obErrorException.getMessage());
    }

    private static OBDomesticVRPControlParameters createControlParameters(String maxIndividualAmount, String currency) {
        final OBDomesticVRPControlParameters vrpControlParameters = new OBDomesticVRPControlParameters();
        final OBActiveOrHistoricCurrencyAndAmount maximumIndividualAmount = new OBActiveOrHistoricCurrencyAndAmount();
        maximumIndividualAmount.setCurrency(currency);
        maximumIndividualAmount.amount(maxIndividualAmount);
        vrpControlParameters.setMaximumIndividualAmount(maximumIndividualAmount);
        return vrpControlParameters;
    }
}
