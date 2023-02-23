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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.simulations.vrp;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponse;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParametersPeriodicLimits;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParametersPeriodicLimits.PeriodAlignmentEnum;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPControlParametersPeriodicLimits.PeriodTypeEnum;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simulates VRP payment breaches for PeriodicLimits specified in the consent.
 *
 * This allows TPPs to test their error handling for this condition, the simulator can be triggered by specifying
 * a custom header on the payment request.
 */
@Service
public class PeriodicLimitBreachResponseSimulatorService {
    private static final Set<String> LIMIT_BREACH_HEADER_VALUES;

    static {
        final Set<String> limitBreaches = new HashSet<>();
        for (PeriodTypeEnum periodType : PeriodTypeEnum.values()) {
            for (PeriodAlignmentEnum periodAlignment : PeriodAlignmentEnum.values()) {
                limitBreaches.add(periodType.getValue() + "-" + periodAlignment.getValue());
            }
        }
        LIMIT_BREACH_HEADER_VALUES = Collections.unmodifiableSet(limitBreaches);
    }

    public void processRequest(String xVrpLimitBreachResponseSimulation, OBDomesticVRPConsentResponse consent) throws OBErrorException {
        if (LIMIT_BREACH_HEADER_VALUES.contains(xVrpLimitBreachResponseSimulation)) {
            final OBDomesticVRPControlParametersPeriodicLimits periodicLimits = findPeriodicLimitsForHeader(xVrpLimitBreachResponseSimulation, consent);
            simulateLimitBreachResponse(periodicLimits);
        } else {
            throw new OBErrorException(OBRIErrorType.REQUEST_VRP_LIMIT_BREACH_SIMULATION_INVALID_HEADER_VALUE,
                    xVrpLimitBreachResponseSimulation);
        }
    }

    private OBDomesticVRPControlParametersPeriodicLimits findPeriodicLimitsForHeader(String xVrpLimitBreachResponseSimulation,
                                                                                            OBDomesticVRPConsentResponse consent) throws OBErrorException {
        final List<OBDomesticVRPControlParametersPeriodicLimits> periodicLimits = consent.getData().getControlParameters().getPeriodicLimits();
        if (periodicLimits != null) {
            final int separatorIndex = xVrpLimitBreachResponseSimulation.indexOf('-');
            final String periodType = xVrpLimitBreachResponseSimulation.substring(0, separatorIndex);
            final String periodAlignment = xVrpLimitBreachResponseSimulation.substring(separatorIndex + 1);
            for (OBDomesticVRPControlParametersPeriodicLimits periodicLimit : periodicLimits) {
                if (periodicLimit.getPeriodAlignment().getValue().equals(periodAlignment)
                        && periodicLimit.getPeriodType().getValue().equals(periodType)) {
                    return periodicLimit;
                }
            }
        }
        throw new OBErrorException(OBRIErrorType.REQUEST_VRP_LIMIT_BREACH_SIMULATION_NO_MATCHING_LIMIT_IN_CONSENT,
                xVrpLimitBreachResponseSimulation);
    }

    private void simulateLimitBreachResponse(OBDomesticVRPControlParametersPeriodicLimits periodicLimits) throws OBErrorException {
        throw new OBErrorException(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_PAYMENT_PERIODIC_LIMIT_BREACH,
                periodicLimits.getAmount(), periodicLimits.getCurrency(),
                periodicLimits.getPeriodType(), periodicLimits.getPeriodAlignment());
    }
}
