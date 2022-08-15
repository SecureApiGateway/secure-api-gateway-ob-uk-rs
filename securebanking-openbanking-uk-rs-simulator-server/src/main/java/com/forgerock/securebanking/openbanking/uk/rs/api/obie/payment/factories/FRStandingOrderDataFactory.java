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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.factories;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrder;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrderDataInitiation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrder;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderDataInitiation;
import com.forgerock.securebanking.openbanking.uk.rs.service.frequency.FrequencyService;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData.FRStandingOrderStatus.ACTIVE;

/**
 * Factory for creating {@link FRStandingOrderData} instances.
 */
public class FRStandingOrderDataFactory {

    public static FRStandingOrderData createFRStandingOrderData(FRWriteDomesticStandingOrder frWriteDomesticStandingOrder, String accountId) {
        FRWriteDomesticStandingOrderDataInitiation initiation = frWriteDomesticStandingOrder.getData().getInitiation();
        return FRStandingOrderData.builder()
                .standingOrderId(frWriteDomesticStandingOrder.getData().getConsentId())
                .accountId(accountId)
                .standingOrderStatusCode(ACTIVE)
                .creditorAccount(initiation.getCreditorAccount())
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .firstPaymentAmount(initiation.getFirstPaymentAmount())
                .nextPaymentAmount(initiation.getRecurringPaymentAmount())
                .nextPaymentDateTime(FrequencyService.getNextDateTime(initiation.getFirstPaymentDateTime(), initiation.getFrequency()))
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .finalPaymentAmount(initiation.getFinalPaymentAmount())
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .build();
    }

    public static FRStandingOrderData createFRStandingOrderData(FRWriteInternationalStandingOrder frWriteInternationalStandingOrder, String accountId) {
        FRWriteInternationalStandingOrderDataInitiation initiation = frWriteInternationalStandingOrder.getData().getInitiation();
        return FRStandingOrderData.builder()
                .standingOrderId(frWriteInternationalStandingOrder.getData().getConsentId())
                .accountId(accountId)
                .standingOrderStatusCode(ACTIVE)
                .creditorAccount(initiation.getCreditorAccount())
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .firstPaymentAmount(initiation.getInstructedAmount())
                .nextPaymentAmount(initiation.getInstructedAmount())
                .nextPaymentDateTime(FrequencyService.getNextDateTime(initiation.getFirstPaymentDateTime(), initiation.getFrequency()))
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .finalPaymentAmount(initiation.getInstructedAmount())
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .build();
    }
}
