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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.simulations.vrp;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVRPConsent;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVRPConsentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVRPControlParameters;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRPeriodicLimits;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRPeriodicLimits.PeriodAlignmentEnum;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRPeriodicLimits.PeriodTypeEnum;
import com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;


import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link PeriodicLimitBreachResponseSimulatorService}
 */
public class PeriodicLimitBreachResponseSimulatorServiceTest {

    private final PeriodicLimitBreachResponseSimulatorService limitBreachSimulator = new PeriodicLimitBreachResponseSimulatorService();

    @Test
    public void testSimulateLimitBreach() {
        final FRDomesticVRPConsent consent = createConsent(createPeriodicLimit("GBP", "20", PeriodTypeEnum.WEEK, PeriodAlignmentEnum.CONSENT),
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

    private FRDomesticVRPConsent createConsent(FRPeriodicLimits... periodicLimits) {
        return FRDomesticVRPConsent.builder().data(
                FRDomesticVRPConsentData.builder().controlParameters(
                        FRDomesticVRPControlParameters.builder().periodicLimits(Arrays.asList(periodicLimits)).
                                build())
                        .build())
                .build();
    }

    private FRPeriodicLimits createPeriodicLimit(String currency, String amount, PeriodTypeEnum periodType,
                                                 PeriodAlignmentEnum periodAlignment) {
        final FRPeriodicLimits limit = new FRPeriodicLimits();
        limit.setPeriodAlignment(periodAlignment);
        limit.setPeriodType(periodType);
        limit.setCurrency(currency);
        limit.setAmount(amount);
        return limit;
    }
}
