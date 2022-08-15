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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.calculation;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation.PaymentConsentValidation;

public class PaymentConsentResponseCalculationFactory {
    /**
     * Get the calculation instance by consent type
     * @param consentId
     * @return a {@link PaymentConsentValidation} implementation instance
     */
    public static PaymentConsentResponseCalculation getCalculationInstance(String consentId) throws UnsupportedOperationException {
        IntentType intentType = IntentType.identify(consentId);
        switch (intentType) {
            case PAYMENT_DOMESTIC_CONSENT -> {
                return new DomesticPaymentConsentResponseCalculation();
            }
            case PAYMENT_DOMESTIC_SCHEDULED_CONSENT -> {
                return new DomesticScheduledPaymentConsentResponseCalculation();
            }
            case PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT -> {
                return new DomesticStandingOrderConsentResponseCalculation();
            }
            case PAYMENT_INTERNATIONAL_CONSENT -> {
                return new InternationalPaymentConsentResponseCalculation();
            }
            case PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT -> {
                return new InternationalScheduledPaymentConsentResponseCalculation();
            }
            case PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT -> {
                return new InternationalStandingOrderConsentResponseCalculation();
            }
            case PAYMENT_FILE_CONSENT -> {
                return new FilePaymentConsentResponseCalculation();
            }
            default -> {
                String message = String.format("Invalid type for intent ID: '%s'", consentId);
                throw new UnsupportedOperationException(message);
            }
        }
    }
}
