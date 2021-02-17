/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrderDataInitiation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderDataInitiation;
import com.forgerock.securebanking.openbanking.uk.rs.service.frequency.FrequencyService;

import java.util.UUID;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData.FRStandingOrderStatus.ACTIVE;

/**
 * Factory for creating {@link FRStandingOrderData} instances.
 */
public class FRStandingOrderDataFactory {

    public static FRStandingOrderData createFRStandingOrderData(FRWriteDomesticStandingOrderDataInitiation initiation, String accountId) {
        return FRStandingOrderData.builder()
                .standingOrderId(UUID.randomUUID().toString())
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

    public static FRStandingOrderData createFRStandingOrderData(FRWriteInternationalStandingOrderDataInitiation initiation, String accountId) {
        return FRStandingOrderData.builder()
                .standingOrderId(UUID.randomUUID().toString())
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
