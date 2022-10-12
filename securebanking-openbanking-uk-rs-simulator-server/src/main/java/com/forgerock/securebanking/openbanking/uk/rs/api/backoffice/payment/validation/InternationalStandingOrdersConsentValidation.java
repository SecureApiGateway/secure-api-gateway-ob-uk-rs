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
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent6;

/**
 * Validation class for Domestic Payment consent request
 * <li>
 * International Standing Order
 *     <ul>
 *         <li>For 3.1.2 {@link OBWriteInternationalStandingOrderConsent4}</li>
 *         <li>For 3.1.3 {@link OBWriteInternationalStandingOrderConsent5}</li>
 *         <li>From 3.1.4 to 3.1.10 {@link OBWriteInternationalStandingOrderConsent6}</li>
 *     </ul>
 * </li>
 */
public class InternationalStandingOrdersConsentValidation extends PaymentConsentValidation {
    @Override
    public Class getRequestClass(OBVersion version) {
        if (version.equals(OBVersion.v3_1_2) || version.isBeforeVersion(OBVersion.v3_1_2)) {
            return OBWriteInternationalStandingOrderConsent4.class;
        } else if (version.equals(OBVersion.v3_1_3)) {
            return OBWriteInternationalStandingOrderConsent5.class;
        }
        return OBWriteInternationalStandingOrderConsent6.class;
    }

    @Override
    public <T> void validate(T consent) {
        if (consent instanceof OBWriteInternationalStandingOrderConsent4) {
            validateInstructedAmount(((OBWriteInternationalStandingOrderConsent4) consent).getData().getInitiation().getInstructedAmount());
            return;
        } else if (consent instanceof OBWriteInternationalStandingOrderConsent5) {
            validateInstructedAmount(((OBWriteInternationalStandingOrderConsent5) consent).getData().getInitiation().getInstructedAmount());
            return;
        }
        validateInstructedAmount(((OBWriteInternationalStandingOrderConsent6) consent).getData().getInitiation().getInstructedAmount());
    }
}
