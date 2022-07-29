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
import uk.org.openbanking.datamodel.payment.*;

/**
 * Validation class for Domestic Payment consent request
 * <li>
 *     Domestic Standing order
 *     <ul>
 *         <li>For 3.1.2 {@link OBWriteDomesticStandingOrderConsent4}</li>
 *         <li>From 3.1.3 to 3.1.10 {@link OBWriteDomesticStandingOrderConsent5}</li>
 *     </ul>
 * </li>
 */
public class DomesticStandingOrdersConsentValidation extends PaymentConsentValidation {
    @Override
    public Class getRequestClass(OBVersion version) {
        if (version.isBeforeVersion(OBVersion.v3_1_3)) {
            return OBWriteDomesticStandingOrderConsent4.class;
        }
        return OBWriteDomesticStandingOrderConsent5.class;
    }

    @Override
    public <T> void validate(T consent) {
        errors.clear();
        if (consent instanceof OBWriteDomesticStandingOrderConsent4) {
            validate(((OBWriteDomesticStandingOrderConsent4) consent).getData().getInitiation().getFirstPaymentAmount());
            validate(((OBWriteDomesticStandingOrderConsent4) consent).getData().getInitiation().getRecurringPaymentAmount());
            validate(((OBWriteDomesticStandingOrderConsent4) consent).getData().getInitiation().getFinalPaymentAmount());
            return;
        }
        validate(((OBWriteDomesticStandingOrderConsent5) consent).getData().getInitiation().getFirstPaymentAmount());
        validate(((OBWriteDomesticStandingOrderConsent5) consent).getData().getInitiation().getRecurringPaymentAmount());
        validate(((OBWriteDomesticStandingOrderConsent5) consent).getData().getInitiation().getFinalPaymentAmount());
    }

    private void validate(OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount firstPaymentAmount) {
        validateAmount(firstPaymentAmount.getAmount());
        validateCurrency(firstPaymentAmount.getCurrency());
    }

    private void validate(OBWriteDomesticStandingOrder3DataInitiationRecurringPaymentAmount recurringPaymentAmount) {
        validateAmount(recurringPaymentAmount.getAmount());
        validateCurrency(recurringPaymentAmount.getCurrency());
    }

    private void validate(OBWriteDomesticStandingOrder3DataInitiationFinalPaymentAmount finalPaymentAmount) {
        validateAmount(finalPaymentAmount.getAmount());
        validateCurrency(finalPaymentAmount.getCurrency());
    }
}
