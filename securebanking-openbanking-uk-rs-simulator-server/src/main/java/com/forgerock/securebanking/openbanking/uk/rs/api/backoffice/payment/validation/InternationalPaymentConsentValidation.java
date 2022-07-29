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
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;

/**
 * Validation class for Domestic Payment consent request
 * <li>
 *     International Payment
 *     <ul>
 *         <li>For 3.1.2 {@link OBWriteInternationalConsent3}</li>
 *         <li>For 3.1.3 {@link OBWriteInternationalConsent4}</li>
 *         <li>From 3.1.4 to 3.1.10 {@link OBWriteInternationalConsent5}</li>
 *     </ul>
 * </li>
 */
public class InternationalPaymentConsentValidation extends PaymentConsentValidation {
    @Override
    public Class getRequestClass(OBVersion version) {
        if (version.equals(OBVersion.v3_1_2) || version.isBeforeVersion(OBVersion.v3_1_2)) {
            return OBWriteInternationalConsent3.class;
        } else if (version.equals(OBVersion.v3_1_3)) {
            return OBWriteInternationalConsent4.class;
        }
        return OBWriteInternationalConsent5.class;
    }

    @Override
    public <T> void validate(T consent) {
        errors.clear();
        if (consent instanceof OBWriteInternationalConsent3) {
            validateInstructedAmount(((OBWriteInternationalConsent3) consent).getData().getInitiation().getInstructedAmount());
            validateExchangeRateInformation(((OBWriteInternationalConsent3) consent).getData().getInitiation().getExchangeRateInformation());
            return;
        } else if (consent instanceof OBWriteInternationalConsent4) {
            validateInstructedAmount(((OBWriteInternationalConsent4) consent).getData().getInitiation().getInstructedAmount());
            validateExchangeRateInformation(((OBWriteInternationalConsent4) consent).getData().getInitiation().getExchangeRateInformation());
            return;
        }
        validateInstructedAmount(((OBWriteInternationalConsent5) consent).getData().getInitiation().getInstructedAmount());
        validateExchangeRateInformation(((OBWriteInternationalConsent5) consent).getData().getInitiation().getExchangeRateInformation());
    }

}
