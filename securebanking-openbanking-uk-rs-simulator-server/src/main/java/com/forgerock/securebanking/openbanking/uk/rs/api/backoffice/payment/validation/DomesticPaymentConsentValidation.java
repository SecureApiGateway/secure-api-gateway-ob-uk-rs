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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent3;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;

/**
 * Validation class for Domestic Payment consent request
 * <ul>
 *     <li>
 *         Consent request {@link OBWriteDomesticConsent3} from v3.1.2 to 3.1.3
 *     </li>
 *     <li>
 *         Consent request {@link OBWriteDomesticConsent4} from v3.1.4 to 3.1.10
 *     </li>
 * </ul>
 */
public class DomesticPaymentConsentValidation extends PaymentConsentValidation {
    @Override
    public Class getRequestClass(OBVersion version) {
        if (version.isBeforeVersion(OBVersion.v3_1_4)) {
            return OBWriteDomesticConsent3.class;
        }
        return OBWriteDomesticConsent4.class;
    }

    @Override
    public <T> void validate(T consent) {
        errors.clear();
        if (consent instanceof OBWriteDomesticConsent3) {
            validateInstructedAmount(((OBWriteDomesticConsent3) consent).getData().getInitiation().getInstructedAmount());
            return;
        }
        validateInstructedAmount(((OBWriteDomesticConsent4) consent).getData().getInitiation().getInstructedAmount());
    }

}
