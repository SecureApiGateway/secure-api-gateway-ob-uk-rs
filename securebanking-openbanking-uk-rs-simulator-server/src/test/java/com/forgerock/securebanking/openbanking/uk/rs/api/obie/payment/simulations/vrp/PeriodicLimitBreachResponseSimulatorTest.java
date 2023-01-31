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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.simulations.vrp;

import com.forgerock.securebanking.openbanking.uk.error.ErrorCode;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.vrp.*;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParametersPeriodicLimits.PeriodTypeEnum;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParametersPeriodicLimits.PeriodAlignmentEnum;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link PeriodicLimitBreachResponseSimulator}
 */
public class PeriodicLimitBreachResponseSimulatorTest {

    private final PeriodicLimitBreachResponseSimulator limitBreachSimulator = new PeriodicLimitBreachResponseSimulator();

    @Test
    public void testSimulateLimitBreach() {
        final OBDomesticVRPConsentResponse consent = createConsent(createPeriodicLimit("GBP", "20", PeriodTypeEnum.WEEK, PeriodAlignmentEnum.CONSENT),
                createPeriodicLimit("GBP", "80", PeriodTypeEnum.MONTH, PeriodAlignmentEnum.CONSENT),
                createPeriodicLimit("GBP", "500.00", PeriodTypeEnum.YEAR, PeriodAlignmentEnum.CONSENT));

        OBErrorException obErrorException = assertThrows(OBErrorException.class,
                () -> limitBreachSimulator.processRequest("Year-Consent", consent));
        assertEquals(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_PAYMENT_PERIODIC_LIMIT_BREACH.getCode().getValue(),
                obErrorException.getOBError().getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, obErrorException.getObriErrorType().getHttpStatus());
        assertEquals("Unable to complete payment due to payment limit breach, periodic limit of '500.00' 'GBP' for period 'Year' 'Consent' has been breached",
                obErrorException.getMessage());
    }

    @Test
    public void testUnsupportedHeaderValueFails() {
        String[] badHeaderValues = {
                null, // NOTE: it is the job of the caller to check for nulls
                "",
                " ",
                "a",
                "badValue",
        };
        for (String badHeaderValue : badHeaderValues) {
            OBErrorException obErrorException = assertThrows(OBErrorException.class,
                    () -> limitBreachSimulator.processRequest(badHeaderValue,
                            createConsent(createPeriodicLimit("GBP", "50.0", PeriodTypeEnum.DAY, PeriodAlignmentEnum.CALENDAR))));
            assertEquals(
                    ErrorCode.OBRI_REQUEST_VRP_LIMIT_BREACH_SIMULATION_INVALID_HEADER_VALUE.getValue(),
                    obErrorException.getOBError().getErrorCode(),
                    "Error processing header value: " + badHeaderValue);
            assertEquals(HttpStatus.BAD_REQUEST, obErrorException.getObriErrorType().getHttpStatus());
            assertEquals("Invalid Header value '" + badHeaderValue+ "', unable to simulate the payment limitation breach",
                    obErrorException.getMessage());
        }
    }

    @Test
    public void testSimulateLimitBreachHeaderValueNotInConsentFails() {
        OBErrorException obErrorException = assertThrows(OBErrorException.class,
                () -> limitBreachSimulator.processRequest("Year-Consent",
                        createConsent(createPeriodicLimit("EUR", "50.00", PeriodTypeEnum.DAY, PeriodAlignmentEnum.CALENDAR),
                                createPeriodicLimit("EUR", "100.00", PeriodTypeEnum.MONTH, PeriodAlignmentEnum.CALENDAR))));
        assertEquals(OBRIErrorType.REQUEST_VRP_LIMIT_BREACH_SIMULATION_NO_MATCHING_LIMIT_IN_CONSENT.getCode().getValue(),
                obErrorException.getOBError().getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, obErrorException.getObriErrorType().getHttpStatus());
        assertEquals("No Periodic Limit found in the consent for Header value 'Year-Consent', unable to simulate the payment limitation breach",
                obErrorException.getMessage());
    }

    @Test
    public void testSimulateLimitBreachNoLimitsOnConsentFails() {
        OBErrorException obErrorException = assertThrows(OBErrorException.class,
                () -> limitBreachSimulator.processRequest("Month-Calendar", createConsent()));
        assertEquals(OBRIErrorType.REQUEST_VRP_LIMIT_BREACH_SIMULATION_NO_MATCHING_LIMIT_IN_CONSENT.getCode().getValue(),
                obErrorException.getOBError().getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, obErrorException.getObriErrorType().getHttpStatus());
        assertEquals("No Periodic Limit found in the consent for Header value 'Month-Calendar', unable to simulate the payment limitation breach",
                obErrorException.getMessage());
    }

    private OBDomesticVRPConsentResponse createConsent(OBDomesticVRPControlParametersPeriodicLimits... periodicLimits) {

        final OBDomesticVRPConsentResponse consent = new OBDomesticVRPConsentResponse()
                .data(
                        new OBDomesticVRPConsentResponseData().controlParameters(
                                new OBDomesticVRPControlParameters()
                                        .periodicLimits(Arrays.asList(periodicLimits))
                        )
                );
        return consent;
    }

    private OBDomesticVRPControlParametersPeriodicLimits createPeriodicLimit(String currency, String amount, PeriodTypeEnum periodType,
                                                 PeriodAlignmentEnum periodAlignment) {
        final OBDomesticVRPControlParametersPeriodicLimits limit = new OBDomesticVRPControlParametersPeriodicLimits();
        limit.setPeriodAlignment(periodAlignment);
        limit.setPeriodType(periodType);
        limit.setCurrency(currency);
        limit.setAmount(amount);
        return limit;
    }
}
