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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.calculation;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.payment.*;

/**
 * Validation class for Domestic Payment Consent response
 * <ul>
 *     <li>
 *         Consent response {@link OBWriteInternationalStandingOrderConsentResponse4} for v3.1.2
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalStandingOrderConsentResponse5} for v3.1.3
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalStandingOrderConsentResponse6} for v3.1.4
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalStandingOrderConsentResponse7} from v3.1.5 to v3.1.10
 *     </li>
 * </ul>
 */
@SuppressWarnings("unchecked")
@Slf4j
public class InternationalStandingOrderConsentResponseCalculation extends PaymentConsentResponseCalculation {

    public static final String TYPE = "UK.OBIE.CHAPSOut";

    @Override
    public Class getResponseClass(OBVersion version) {
        log.debug("{} is the version to calculate response elements", version.getCanonicalName());
        if (version.equals(OBVersion.v3_1_2)) {
            return OBWriteInternationalStandingOrderConsentResponse4.class;
        } else if (version.equals(OBVersion.v3_1_3)) {
            return OBWriteInternationalStandingOrderConsentResponse5.class;
        } else if (version.equals(OBVersion.v3_1_4)) {
            return OBWriteInternationalStandingOrderConsentResponse6.class;
        }
        return OBWriteInternationalStandingOrderConsentResponse7.class;
    }

    @Override
    public <T, R> R calculate(T consentRequest, R consentResponse) {
        if (consentResponse instanceof OBWriteInternationalStandingOrderConsentResponse4) {
            log.debug("OBWriteInternationalStandingOrderConsentResponse4 instance");
            ((OBWriteInternationalStandingOrderConsentResponse4) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse3DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );
        } else if (consentResponse instanceof OBWriteInternationalStandingOrderConsentResponse5) {
            log.debug("OBWriteInternationalStandingOrderConsentResponse5 instance");
            ((OBWriteInternationalStandingOrderConsentResponse5) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse3DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );
        } else if (consentResponse instanceof OBWriteInternationalStandingOrderConsentResponse6) {
            log.debug("OBWriteInternationalStandingOrderConsentResponse6 instance");
            ((OBWriteInternationalStandingOrderConsentResponse6) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse4DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );
        } else {
            log.debug("OBWriteInternationalStandingOrderConsentResponse7 instance");
            ((OBWriteInternationalStandingOrderConsentResponse7) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse5DataCharges().
                                    chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );
        }
        return consentResponse;
    }
}
