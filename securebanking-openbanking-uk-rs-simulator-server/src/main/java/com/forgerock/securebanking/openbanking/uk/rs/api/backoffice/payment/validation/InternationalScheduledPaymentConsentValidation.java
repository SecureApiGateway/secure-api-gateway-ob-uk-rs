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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsent3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsent5;

/**
 * Validation class for Domestic Payment consent request
 * <li>
 *     International Scheduled Payment
 *     <ul>
 *         <li>For 3.1.2 {@link OBWriteInternationalScheduledConsent3}</li>
 *         <li>For 3.1.3 {@link OBWriteInternationalScheduledConsent4}</li>
 *         <li>From 3.1.4 to 3.1.10 {@link OBWriteInternationalScheduledConsent5}</li>
 *     </ul>
 * </li>
 *
 */
public class InternationalScheduledPaymentConsentValidation extends PaymentConsentValidation {
    @Override
    public Class getRequestClass(OBVersion version) {
        if (version.equals(OBVersion.v3_1_2) || version.isBeforeVersion(OBVersion.v3_1_2)) {
            return OBWriteInternationalScheduledConsent3.class;
        } else if (version.equals(OBVersion.v3_1_3)) {
            return OBWriteInternationalScheduledConsent4.class;
        }
        return OBWriteInternationalScheduledConsent5.class;
    }

    @Override
    public <T> void validate(T consent) {
        errors.clear();
        if (consent instanceof OBWriteInternationalScheduledConsent3) {
            validateInstructedAmount(((OBWriteInternationalScheduledConsent3) consent).getData().getInitiation().getInstructedAmount());
            validateExchangeRateInformation(((OBWriteInternationalScheduledConsent3) consent).getData().getInitiation().getExchangeRateInformation());
            return;
        } else if (consent instanceof OBWriteInternationalScheduledConsent4) {
            validateInstructedAmount(((OBWriteInternationalScheduledConsent4) consent).getData().getInitiation().getInstructedAmount());
            validateExchangeRateInformation(((OBWriteInternationalScheduledConsent4) consent).getData().getInitiation().getExchangeRateInformation());
            return;
        }
        validateInstructedAmount(((OBWriteInternationalScheduledConsent5) consent).getData().getInitiation().getInstructedAmount());
        validateExchangeRateInformation(((OBWriteInternationalScheduledConsent5) consent).getData().getInitiation().getExchangeRateInformation());
    }
}
