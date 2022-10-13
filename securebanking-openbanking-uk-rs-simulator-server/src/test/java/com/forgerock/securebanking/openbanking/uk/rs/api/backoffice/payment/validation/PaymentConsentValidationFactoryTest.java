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

import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Junit test for {@link PaymentConsentValidationFactory}
 */
public class PaymentConsentValidationFactoryTest {

    @ParameterizedTest
    @EnumSource(
            value = IntentType.class,
            names = {
                    "PAYMENT_DOMESTIC_CONSENT",
                    "PAYMENT_DOMESTIC_SCHEDULED_CONSENT",
                    "PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT",
                    "PAYMENT_INTERNATIONAL_CONSENT",
                    "PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT",
                    "PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT",
                    "PAYMENT_FILE_CONSENT"
            }
    )
    public void shouldReturnTheCorrectInstance(IntentType intentType) {
        String intentId = intentType.generateIntentId();
        PaymentConsentValidation validation = PaymentConsentValidationFactory.getValidationInstance(intentId);
        switch (intentType) {
            case PAYMENT_DOMESTIC_CONSENT ->
                    assertThat(validation).isExactlyInstanceOf(DomesticPaymentConsentValidation.class);
            case PAYMENT_DOMESTIC_SCHEDULED_CONSENT ->
                    assertThat(validation).isExactlyInstanceOf(DomesticScheduledPaymentConsentValidation.class);
            case PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT ->
                    assertThat(validation).isExactlyInstanceOf(DomesticStandingOrdersConsentValidation.class);
            case PAYMENT_INTERNATIONAL_CONSENT ->
                    assertThat(validation).isExactlyInstanceOf(InternationalPaymentConsentValidation.class);
            case PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT ->
                    assertThat(validation).isExactlyInstanceOf(InternationalScheduledPaymentConsentValidation.class);
            case PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT ->
                    assertThat(validation).isExactlyInstanceOf(InternationalStandingOrdersConsentValidation.class);
            case PAYMENT_FILE_CONSENT -> assertThat(validation).isExactlyInstanceOf(FilePaymentConsentValidation.class);
        }
    }
}
