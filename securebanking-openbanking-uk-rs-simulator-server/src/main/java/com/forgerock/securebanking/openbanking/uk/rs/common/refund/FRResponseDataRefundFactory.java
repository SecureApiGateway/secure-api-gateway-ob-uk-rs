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
package com.forgerock.securebanking.openbanking.uk.rs.common.refund;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.*;

import java.util.Optional;

/**
 * Factory for creating instances of {@link FRDomesticResponseDataRefund} or {@link FRInternationalResponseDataRefund}.
 */
public class FRResponseDataRefundFactory {

    // flag set on the consent by the PISP if PSU requests a refund for the transaction.
    // ZD: 55834 - https://github.com/OpenBankingToolkit/openbanking-toolkit/issues/14

    /**
     * Creates a {@link FRDomesticResponseDataRefund}, so long as the provided {@link FRReadRefundAccount} is set
     * to 'Yes' and the initiation's debit account is null. Otherwise returns an empty {@link Optional}.
     *
     * @param frReadRefundAccount The {@link FRReadRefundAccount} indicating if a refund should be issued.
     * @param initiation          {@link FRDomesticDataInitiation} The payment's initiation data.
     * @return The {@link Optional} {@link FRDomesticResponseDataRefund} instance.
     */
    public static Optional<FRDomesticResponseDataRefund> frDomesticResponseDataRefund(
            FRReadRefundAccount frReadRefundAccount,
            FRDomesticDataInitiation initiation) {
        if (hasRefund(frReadRefundAccount) && initiation.getDebtorAccount() != null) {
            return Optional.of(FRDomesticResponseDataRefund.builder()
                    .account(FRAccountIdentifier.builder()
                            .schemeName(initiation.getDebtorAccount().getSchemeName())
                            .identification(initiation.getDebtorAccount().getIdentification())
                            .name(initiation.getDebtorAccount().getName())
                            .secondaryIdentification(initiation.getDebtorAccount().getSecondaryIdentification())
                            .build())
                    .build());
        }
        return Optional.empty();
    }

    /**
     * Creates a {@link FRInternationalResponseDataRefund}, so long as the provided {@link FRReadRefundAccount} is
     * set to 'Yes' and the initiation's debit account is null. Otherwise returns an empty {@link Optional}.
     *
     * @param frReadRefundAccount The {@link FRReadRefundAccount} indicating if a refund should be issued.
     * @param initiation          {@link FRDomesticDataInitiation} The payment's initiation data.
     * @return The {@link Optional} {@link FRDomesticResponseDataRefund} instance.
     */
    public static Optional<FRInternationalResponseDataRefund> frInternationalResponseDataRefund(
            FRReadRefundAccount frReadRefundAccount,
            FRInternationalDataInitiation initiation) {
        if (hasRefund(frReadRefundAccount) && initiation.getDebtorAccount() != null) {
            FRAccountIdentifier debtorAccount = initiation.getDebtorAccount();
            FRFinancialCreditor creditor = initiation.getCreditor();
            FRFinancialAgent creditorAgent = initiation.getCreditorAgent();
            return Optional.of(FRInternationalResponseDataRefund.builder()
                            .account(FRAccountIdentifier.builder()
                                    .schemeName(debtorAccount.getSchemeName())
                                    .identification(debtorAccount.getIdentification())
                                    .name(debtorAccount.getName())
                                    .secondaryIdentification(debtorAccount.getSecondaryIdentification())
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