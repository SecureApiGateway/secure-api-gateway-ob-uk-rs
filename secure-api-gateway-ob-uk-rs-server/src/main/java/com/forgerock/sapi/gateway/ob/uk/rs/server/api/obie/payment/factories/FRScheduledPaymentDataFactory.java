/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRScheduledPaymentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduledDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledDataInitiation;
import org.joda.time.DateTime;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRScheduledPaymentData.FRScheduleType.EXECUTION;

/**
 * Factory for creating {@link FRScheduledPaymentData} instances.
 */
public class FRScheduledPaymentDataFactory {

    public static FRScheduledPaymentData createFRScheduledPaymentData(FRWriteDomesticScheduled frWriteDomesticScheduled,
                                                                      String xAccountId) {
        FRWriteDomesticScheduledDataInitiation initiation = frWriteDomesticScheduled.getData().getInitiation();
        return frScheduledPaymentData(
                frWriteDomesticScheduled.getData().getConsentId(),
                xAccountId,
                initiation.getRequestedExecutionDateTime(),
                initiation.getInstructedAmount(),
                initiation.getCreditorAccount());
    }


    public static FRScheduledPaymentData createFRScheduledPaymentData(FRWriteInternationalScheduled frWriteInternationalScheduled,
                                                                      String xAccountId) {
        FRWriteInternationalScheduledDataInitiation initiation = frWriteInternationalScheduled.getData().getInitiation();
        return frScheduledPaymentData(
                frWriteInternationalScheduled.getData().getConsentId(),
                xAccountId,
                initiation.getRequestedExecutionDateTime(),
                initiation.getInstructedAmount(),
                initiation.getCreditorAccount());
    }

    private static FRScheduledPaymentData frScheduledPaymentData(String scheduledPaymentId,
                                                                 String xAccountId,
                                                                 DateTime requestedExecutionDateTime,
                                                                 FRAmount instructedAmount,
                                                                 FRAccountIdentifier creditorAccount) {
        return FRScheduledPaymentData.builder()
                .accountId(xAccountId)
                .scheduledPaymentId(scheduledPaymentId)
                .scheduledPaymentDateTime(requestedExecutionDateTime)
                .scheduledType(EXECUTION)
                .instructedAmount(instructedAmount)
                .creditorAccount(creditorAccount)
                .build();
    }

    //test2
}
