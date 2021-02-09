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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRScheduledPaymentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAccountIdentifier;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAmount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticScheduledDataInitiation;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduledDataInitiation;
import org.joda.time.DateTime;

import java.util.UUID;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRScheduledPaymentData.FRScheduleType.EXECUTION;

/**
 * Factory for creating {@link FRScheduledPaymentData} instances.
 */
public class FRScheduledPaymentDataFactory {

    public static FRScheduledPaymentData createFRScheduledPaymentData(FRWriteDomesticScheduledDataInitiation initiation,
                                                                      String xAccountId) {
        return frScheduledPaymentData(xAccountId,
                initiation.getRequestedExecutionDateTime(),
                initiation.getInstructedAmount(),
                initiation.getCreditorAccount());
    }


    public static FRScheduledPaymentData createFRScheduledPaymentData(FRWriteInternationalScheduledDataInitiation initiation,
                                                                      String xAccountId) {
        return frScheduledPaymentData(xAccountId,
                initiation.getRequestedExecutionDateTime(),
                initiation.getInstructedAmount(),
                initiation.getCreditorAccount());
    }

    private static FRScheduledPaymentData frScheduledPaymentData(String xAccountId,
                                                                 DateTime requestedExecutionDateTime,
                                                                 FRAmount instructedAmount,
                                                                 FRAccountIdentifier creditorAccount) {
        return FRScheduledPaymentData.builder()
                .accountId(xAccountId)
                .scheduledPaymentId(UUID.randomUUID().toString())
                .scheduledPaymentDateTime(requestedExecutionDateTime)
                .scheduledType(EXECUTION)
                .instructedAmount(instructedAmount)
                .creditorAccount(creditorAccount)
                .build();
    }
}
