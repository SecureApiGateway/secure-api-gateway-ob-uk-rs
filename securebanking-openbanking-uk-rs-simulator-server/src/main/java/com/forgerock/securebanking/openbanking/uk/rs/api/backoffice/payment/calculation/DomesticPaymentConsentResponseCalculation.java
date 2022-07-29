/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.calculation;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.*;

import java.util.List;

/**
 * Validation class for Domestic Payment consent response
 * <ul>
 *     <li>
 *         Consent response {@link OBWriteDomesticConsentResponse3} from v3.1.2 to 3.1.3
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteDomesticConsentResponse4} for v3.1.4
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteDomesticConsentResponse5} from 3.1.5  to 3.1.10
 *     </li>
 * </ul>
 */
@SuppressWarnings("unchecked")
@Slf4j
public class DomesticPaymentConsentResponseCalculation extends PaymentConsentResponseCalculation {
    @Override
    public Class getResponseClass(OBVersion version) {
        log.debug("{} is the version to calculate response elements", version.getCanonicalName());
        if (version.isBeforeVersion(OBVersion.v3_1_4)) {
            return OBWriteDomesticConsentResponse3.class;
        } else if (version.equals(OBVersion.v3_1_4)) {
            return OBWriteDomesticConsentResponse4.class;
        }
        return OBWriteDomesticConsentResponse5.class;
    }

    @Override
    public <T, R> R calculate(T consentRequest, R consentResponse) {
        errors.clear();
        // TODO logic to add errors when validate the request
//      errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Error message reason"));
//      return false;
        if (consentResponse instanceof OBWriteDomesticConsentResponse3) {
            log.debug("OBWriteDomesticConsentResponse3 instance");
            ((OBWriteDomesticConsentResponse3) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse3DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .amount(getDefaultAmount())
                    );

        } else if (consentResponse instanceof OBWriteDomesticConsentResponse4) {
            log.debug("OBWriteDomesticConsentResponse4 instance");
            ((OBWriteDomesticConsentResponse4) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse4DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .amount(getDefaultAmount())
                    );
        } else {
            log.debug("OBWriteDomesticConsentResponse5 instance");
            ((OBWriteDomesticConsentResponse5) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse5DataCharges().
                                    chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .amount(getDefaultAmount())
                    );
        }
        return consentResponse;
    }

    @Override
    public List<OBError1> getErrors() {
        return errors;
    }
}
