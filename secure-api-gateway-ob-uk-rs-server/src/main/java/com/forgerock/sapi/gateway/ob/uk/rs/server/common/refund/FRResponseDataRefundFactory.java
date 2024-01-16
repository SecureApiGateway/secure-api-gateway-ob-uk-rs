/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.refund;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.*;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRInternationalDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRWriteDomesticVrpDataInitiation;

import java.util.Objects;
import java.util.Optional;

/**
 * Factory for creating instances of {@link FRResponseDataRefund} or {@link FRInternationalResponseDataRefund}.
 */
public class FRResponseDataRefundFactory {

    /**
     * Creates a {@link FRResponseDataRefund}, so long as the provided {@link FRReadRefundAccount} is set
     * to 'Yes' and the payment account is not null. Otherwise, returns an empty {@link Optional}.
     *
     * @param frAccountIdentifier The {@link FRAccountIdentifier} payment account.
     * @return The {@link Optional} {@link FRResponseDataRefund} instance.
     */
    public static Optional<FRResponseDataRefund> frResponseDataRefund(FRAccountIdentifier frAccountIdentifier) {
        if (Objects.nonNull(frAccountIdentifier)) {
            return Optional.of(FRResponseDataRefund.builder()
                    .account(frAccountIdentifier)
                    .build());
        }
        return Optional.empty();
    }

    /**
     * Creates a {@link FRInternationalResponseDataRefund}, so long as the provided {@link FRReadRefundAccount} is
     * set to 'Yes' and the initiation's debit account is null. Otherwise, returns an empty {@link Optional}.
     *
     * @param frAccountIdentifier The {@link FRAccountIdentifier} payment account.
     * @param initiation          {@link FRInternationalDataInitiation} The payment's initiation data.
     * @return The {@link Optional} {@link FRResponseDataRefund} instance.
     */
    public static Optional<FRInternationalResponseDataRefund> frInternationalResponseDataRefund(
            FRAccountIdentifier frAccountIdentifier,
            FRInternationalDataInitiation initiation
    ) {
        if (Objects.nonNull(frAccountIdentifier)) {
            FRFinancialCreditor creditor = initiation.getCreditor();
            FRFinancialAgent creditorAgent = initiation.getCreditorAgent();
            return Optional.of(FRInternationalResponseDataRefund.builder()
                    .account(FRAccountIdentifier.builder()
                            .schemeName(frAccountIdentifier.getSchemeName())
                            .identification(frAccountIdentifier.getIdentification())
                            .name(frAccountIdentifier.getName())
                            .secondaryIdentification(frAccountIdentifier.getSecondaryIdentification())
                            .build())
                    .creditor(creditor == null ? null : FRFinancialCreditor.builder()
                            .name(creditor.getName())
                            .postalAddress(creditor.getPostalAddress())
                            .build())
                    .agent(creditorAgent == null ? null : FRFinancialAgent.builder()
                            .schemeName(creditorAgent.getSchemeName())
                            .identification(creditorAgent.getIdentification())
                            .name(creditorAgent.getName())
                            .postalAddress(creditorAgent.getPostalAddress())
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    private static boolean hasRefund(FRReadRefundAccount frReadRefundAccount) {
        return frReadRefundAccount != null && frReadRefundAccount.equals(FRReadRefundAccount.YES);
    }
}
