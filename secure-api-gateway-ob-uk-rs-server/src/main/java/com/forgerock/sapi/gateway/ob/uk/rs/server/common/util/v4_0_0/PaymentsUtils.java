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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.v4_0_0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRRemittanceInformationConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRSupplementaryDataConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRAccountIdentifierConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRAmountConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRMandateRelatedInformationConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRRegulatoryReportingConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRUltimateCreditorConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRUltimateDebtorConverter;

import uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiationDebtorAccount;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrder3DataInitiation;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsentResponse6DataInitiation;
import uk.org.openbanking.datamodel.v4.common.OBReadRefundAccount;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsentResponse5DataChargesInner;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrder3;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsentResponse6;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsentResponse6Data;

/**
 * Added for operations and constants regarding the payment status for the payments APIs
 */
public class PaymentsUtils {

    public static final Map<String, String> statusLinkingMap = Map.of(
            "InitiationPending", "Pending",
            "InitiationFailed", "Rejected",
            "InitiationCompleted", "Accepted",
            "Cancelled", "Cancelled"
    );

    public static OBWriteDomesticStandingOrderConsentResponse6 createTestDataStandingOrderConsentResponse6(OBWriteDomesticStandingOrder3 payment) {
        List<OBWriteDomesticConsentResponse5DataChargesInner> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataChargesInner());
        return new OBWriteDomesticStandingOrderConsentResponse6()
                .data(new OBWriteDomesticStandingOrderConsentResponse6Data()
                        .readRefundAccount(OBReadRefundAccount.YES)
                        .initiation(
                                toOBWriteDomesticStandingOrderConsentResponse6DataInitiation(
                                        payment.getData().getInitiation()))
                        .charges(charges)
                )
                .risk(payment.getRisk());
    }

    public static OBWriteDomesticStandingOrderConsentResponse6DataInitiation toOBWriteDomesticStandingOrderConsentResponse6DataInitiation(
            OBWriteDomesticStandingOrder3DataInitiation initiation
    ) {
        OBWriteDomesticStandingOrderConsentResponse6DataInitiation dataInitiation6 =
                new OBWriteDomesticStandingOrderConsentResponse6DataInitiation()
                        .creditorAccount(initiation.getCreditorAccount())
                        .finalPaymentAmount(initiation.getFinalPaymentAmount())
                        .firstPaymentAmount(initiation.getFirstPaymentAmount())
                        .recurringPaymentAmount(initiation.getRecurringPaymentAmount())
                        .mandateRelatedInformation(initiation.getMandateRelatedInformation())
                        .regulatoryReporting(initiation.getRegulatoryReporting())
                        .remittanceInformation(initiation.getRemittanceInformation())
                        .ultimateCreditor(initiation.getUltimateCreditor())
                        .ultimateDebtor(initiation.getUltimateDebtor())
                        .supplementaryData(initiation.getSupplementaryData());

        if (Objects.nonNull(initiation.getDebtorAccount())) {
            dataInitiation6.debtorAccount(
                    new OBWriteDomestic2DataInitiationDebtorAccount()
                            .identification(initiation.getDebtorAccount().getIdentification())
                            .secondaryIdentification(initiation.getDebtorAccount().getSecondaryIdentification())
                            .schemeName(initiation.getDebtorAccount().getSchemeName())
                            .name(initiation.getDebtorAccount().getName())
                            .proxy(initiation.getDebtorAccount().getProxy())
            );
        }
        return dataInitiation6;
    }
}
