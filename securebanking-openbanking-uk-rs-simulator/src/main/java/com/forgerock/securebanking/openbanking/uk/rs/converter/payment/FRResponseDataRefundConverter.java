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
package com.forgerock.securebanking.openbanking.uk.rs.converter.payment;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAccountIdentifier;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRFinancialAgent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRFinancialCreditor;
import com.forgerock.securebanking.openbanking.uk.rs.common.refund.FRDomesticResponseDataRefund;
import com.forgerock.securebanking.openbanking.uk.rs.common.refund.FRInternationalResponseDataRefund;
import uk.org.openbanking.datamodel.payment.*;

import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRPaymentPostalAddressConverter.toOBPostalAddress6;

/**
 * Converter for {@link FRDomesticResponseDataRefund} and {@link FRInternationalResponseDataRefund} into equivalent OB objects.
 */
public class FRResponseDataRefundConverter {

    public static OBWriteDomesticResponse4DataRefund toOBWriteDomesticResponse4DataRefund(FRDomesticResponseDataRefund refund) {
        return refund == null ? null : new OBWriteDomesticResponse4DataRefund()
                .account(toOBWriteDomesticResponse4DataRefundAccount(refund.getAccount()));
    }

    public static OBWriteInternationalResponse4DataRefund toOBWriteInternationalResponse4DataRefund(FRInternationalResponseDataRefund refund) {
        return refund == null ? null : new OBWriteInternationalResponse4DataRefund()
                .account(toOBWriteDomesticResponse4DataRefundAccount(refund.getAccount()))
                .creditor(toOBWriteInternationalResponse4DataRefundCreditor(refund.getCreditor()))
                .agent(toOBWriteInternationalResponse4DataRefundAgent(refund.getAgent()));
    }

    public static OBWriteInternationalRefundResponse1DataRefund toOBWriteInternationalRefundResponse1DataRefund(FRInternationalResponseDataRefund refund) {
        return refund == null ? null : new OBWriteInternationalRefundResponse1DataRefund()
                .account(toOBWriteDomesticResponse4DataRefundAccount(refund.getAccount()))
                .creditor(toOBWriteInternationalRefundResponse1DataRefundCreditor(refund.getCreditor()))
                .agent(toOBWriteInternationalRefundResponse1DataRefundAgent(refund.getAgent()));
    }

    private static OBWriteDomesticResponse4DataRefundAccount toOBWriteDomesticResponse4DataRefundAccount(FRAccountIdentifier account) {
        return account == null ? null : new OBWriteDomesticResponse4DataRefundAccount()
                .schemeName(account.getSchemeName())
                .identification(account.getIdentification())
                .name(account.getName())
                .secondaryIdentification(account.getSecondaryIdentification());
    }

    private static OBWriteInternationalResponse4DataRefundCreditor toOBWriteInternationalResponse4DataRefundCreditor(FRFinancialCreditor creditor) {
        return creditor == null ? null : new OBWriteInternationalResponse4DataRefundCreditor()
                .name(creditor.getName())
                .postalAddress(toOBPostalAddress6(creditor.getPostalAddress()));
    }

    private static OBWriteInternationalRefundResponse1DataRefundCreditor toOBWriteInternationalRefundResponse1DataRefundCreditor(FRFinancialCreditor creditor) {
        return creditor == null ? null : new OBWriteInternationalRefundResponse1DataRefundCreditor()
                .name(creditor.getName())
                .postalAddress(toOBPostalAddress6(creditor.getPostalAddress()));
    }

    private static OBWriteInternationalResponse4DataRefundAgent toOBWriteInternationalResponse4DataRefundAgent(FRFinancialAgent agent) {
        return agent == null ? null : new OBWriteInternationalResponse4DataRefundAgent()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(toOBPostalAddress6(agent.getPostalAddress()));
    }

    private static OBWriteInternationalRefundResponse1DataRefundAgent toOBWriteInternationalRefundResponse1DataRefundAgent(FRFinancialAgent agent) {
        return agent == null ? null : new OBWriteInternationalRefundResponse1DataRefundAgent()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(toOBPostalAddress6(agent.getPostalAddress()));
    }
}
