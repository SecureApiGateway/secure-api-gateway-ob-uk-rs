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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.util;

import org.joda.time.DateTimeZone;
import uk.org.openbanking.datamodel.payment.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public static OBWriteDomesticConsentResponse4 createTestDataConsentResponse4(OBWriteDomestic2 payment) {
        List<OBWriteDomesticConsentResponse4DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse4DataCharges());
        return new OBWriteDomesticConsentResponse4()
                .data(new OBWriteDomesticConsentResponse4Data()
                        .readRefundAccount(OBReadRefundAccountEnum.YES)
                        .initiation(payment.getData().getInitiation())
                        .charges(charges)
                )
                .risk(payment.getRisk());
    }

    public static OBWriteDomesticConsentResponse5 createTestDataConsentResponse5(OBWriteDomestic2 payment) {
        List<OBWriteDomesticConsentResponse5DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataCharges());
        return new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .readRefundAccount(OBReadRefundAccountEnum.YES)
                        .initiation(payment.getData().getInitiation())
                        .charges(charges)
                )
                .risk(payment.getRisk());
    }

    public static OBWriteDomesticConsent4 createTestDataConsent4(OBWriteDomestic2 payment) {
        List<OBWriteDomesticConsentResponse5DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataCharges());
        return new OBWriteDomesticConsent4()
                .data(new OBWriteDomesticConsent4Data()
                        .readRefundAccount(OBReadRefundAccountEnum.YES)
                        .initiation(payment.getData().getInitiation())
                )
                .risk(payment.getRisk());
    }

    public static OBWriteDomesticScheduledConsentResponse5 createTestDataScheduledConsentResponse5(OBWriteDomesticScheduled2 payment) {
        List<OBWriteDomesticConsentResponse5DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataCharges());
        return new OBWriteDomesticScheduledConsentResponse5()
                .data(new OBWriteDomesticScheduledConsentResponse5Data()
                        .readRefundAccount(OBReadRefundAccountEnum.YES)
                        .initiation(formatDates(payment.getData().getInitiation()))
                        .charges(charges)
                )
                .risk(payment.getRisk());

    }

    private static OBWriteDomesticScheduled2DataInitiation formatDates(
            OBWriteDomesticScheduled2DataInitiation initiation
    ) {
        initiation.requestedExecutionDateTime(
                Objects.nonNull(initiation.getRequestedExecutionDateTime()) ?
                        initiation.getRequestedExecutionDateTime().withZone(DateTimeZone.UTC) :
                        null
        );
        return initiation;
    }

    public static OBWriteDomesticStandingOrderConsentResponse5 createTestDataStandingOrderConsentResponse5(OBWriteDomesticStandingOrder3 payment) {
        List<OBWriteDomesticConsentResponse4DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse4DataCharges());
        return new OBWriteDomesticStandingOrderConsentResponse5()
                .data(new OBWriteDomesticStandingOrderConsentResponse5Data()
                        .readRefundAccount(OBReadRefundAccountEnum.YES)
                        .initiation(formatDates(payment.getData().getInitiation()))
                        .charges(charges)
                )
                .risk(payment.getRisk());

    }

    public static OBWriteDomesticStandingOrderConsentResponse6 createTestDataStandingOrderConsentResponse6(OBWriteDomesticStandingOrder3 payment) {
        List<OBWriteDomesticConsentResponse5DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataCharges());
        return new OBWriteDomesticStandingOrderConsentResponse6()
                .data(new OBWriteDomesticStandingOrderConsentResponse6Data()
                        .readRefundAccount(OBReadRefundAccountEnum.YES)
                        .initiation(
                                toOBWriteDomesticStandingOrderConsentResponse6DataInitiation(
                                        payment.getData().getInitiation())
                        )
                        .charges(charges)
                )
                .risk(payment.getRisk());

    }

    public static OBWriteDomesticStandingOrderConsentResponse6DataInitiation toOBWriteDomesticStandingOrderConsentResponse6DataInitiation(
            OBWriteDomesticStandingOrder3DataInitiation initiation
    ) {
        OBWriteDomesticStandingOrderConsentResponse6DataInitiation dataInitiation6 =
                new OBWriteDomesticStandingOrderConsentResponse6DataInitiation()
                        .reference(initiation.getReference())
                        .creditorAccount(initiation.getCreditorAccount())
                        .finalPaymentAmount(initiation.getFinalPaymentAmount())
                        .firstPaymentAmount(initiation.getFirstPaymentAmount())
                        .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                        .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                        .frequency(initiation.getFrequency())
                        .numberOfPayments(initiation.getNumberOfPayments())
                        .recurringPaymentAmount(initiation.getRecurringPaymentAmount())
                        .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                        .supplementaryData(initiation.getSupplementaryData());
        if (Objects.nonNull(initiation.getDebtorAccount())) {
            dataInitiation6.debtorAccount(
                    new OBWriteDomestic2DataInitiationDebtorAccount()
                            .identification(initiation.getDebtorAccount().getIdentification())
                            .secondaryIdentification(initiation.getDebtorAccount().getSecondaryIdentification())
                            .schemeName(initiation.getDebtorAccount().getSchemeName())
                            .name(initiation.getDebtorAccount().getName())
            );
        }
        return dataInitiation6;
    }

    private static OBWriteDomesticStandingOrder3DataInitiation formatDates(
            OBWriteDomesticStandingOrder3DataInitiation initiation
    ) {
        initiation.finalPaymentDateTime(
                Objects.nonNull(initiation.getFinalPaymentDateTime()) ?
                        initiation.getFinalPaymentDateTime().withZone(DateTimeZone.UTC) :
                        null
        );
        initiation.firstPaymentDateTime(
                Objects.nonNull(initiation.getFirstPaymentDateTime()) ?
                        initiation.getFirstPaymentDateTime().withZone(DateTimeZone.UTC) :
                        null
        );
        initiation.recurringPaymentDateTime(
                Objects.nonNull(initiation.getRecurringPaymentDateTime()) ?
                        initiation.getRecurringPaymentDateTime().withZone(DateTimeZone.UTC) :
                        null
        );
        return initiation;
    }

    public static OBWriteInternationalConsentResponse6 createTestDataInternationalConsentResponse6(OBWriteInternational3 payment) {
        List<OBWriteDomesticConsentResponse5DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataCharges());
        return new OBWriteInternationalConsentResponse6()
                .data(
                        new OBWriteInternationalConsentResponse6Data()
                                .readRefundAccount(OBReadRefundAccountEnum.YES)
                                .initiation(payment.getData().getInitiation())
                                .charges(charges)
                )
                .risk(payment.getRisk());
    }

    public static OBWriteInternationalScheduledConsentResponse5 createTestDataInternationalScheduledConsentResponse5(OBWriteInternationalScheduled3 payment) {
        List<OBWriteDomesticConsentResponse4DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse4DataCharges());
        return new OBWriteInternationalScheduledConsentResponse5()
                .data(
                        new OBWriteInternationalScheduledConsentResponse5Data()
                                .readRefundAccount(OBReadRefundAccountEnum.YES)
                                .initiation(formatDates(payment.getData().getInitiation()))
                                .charges(charges)
                )
                .risk(payment.getRisk());
    }

    public static OBWriteInternationalScheduledConsentResponse6 createTestDataInternationalScheduledConsentResponse6(OBWriteInternationalScheduled3 payment) {
        List<OBWriteDomesticConsentResponse5DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataCharges());
        return new OBWriteInternationalScheduledConsentResponse6()
                .data(
                        new OBWriteInternationalScheduledConsentResponse6Data()
                                .readRefundAccount(OBReadRefundAccountEnum.YES)
                                .initiation(toOBWriteInternationalScheduledConsentResponse6DataInitiation(payment.getData().getInitiation()))
                                .charges(charges)
                )
                .risk(payment.getRisk());
    }

    private static OBWriteInternationalScheduled3DataInitiation formatDates(OBWriteInternationalScheduled3DataInitiation initiation) {
        initiation.requestedExecutionDateTime(
                Objects.nonNull(initiation.getRequestedExecutionDateTime()) ?
                        initiation.getRequestedExecutionDateTime().withZone(DateTimeZone.UTC) :
                        null
        );
        return initiation;
    }

    private static OBWriteInternationalScheduledConsentResponse6DataInitiation toOBWriteInternationalScheduledConsentResponse6DataInitiation(
            OBWriteInternationalScheduled3DataInitiation initiation
    ) {
        OBWriteInternationalScheduledConsentResponse6DataInitiation dataInitiation6 =
                new OBWriteInternationalScheduledConsentResponse6DataInitiation()
                        .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime().withZone(DateTimeZone.UTC))
                        .instructionIdentification(initiation.getInstructionIdentification())
                        .endToEndIdentification(initiation.getEndToEndIdentification())
                        .localInstrument(initiation.getLocalInstrument())
                        .chargeBearer(initiation.getChargeBearer())
                        .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                        .destinationCountryCode(initiation.getDestinationCountryCode())
                        .extendedPurpose(initiation.getExtendedPurpose())
                        .instructionPriority(initiation.getInstructionPriority())
                        .purpose(initiation.getPurpose())
                        .instructedAmount(initiation.getInstructedAmount())
                        .creditorAccount(initiation.getCreditorAccount())
                        .creditor(
                                Objects.nonNull(initiation.getCreditor()) ?
                                        new OBWriteInternationalScheduledConsentResponse6DataInitiationCreditor()
                                                .name(initiation.getCreditor().getName())
                                                .postalAddress(initiation.getCreditor().getPostalAddress()) :
                                        null
                        )
                        .creditorAgent(initiation.getCreditorAgent())
                        .remittanceInformation(initiation.getRemittanceInformation())
                        .exchangeRateInformation(initiation.getExchangeRateInformation())
                        .supplementaryData(initiation.getSupplementaryData());

        if (Objects.nonNull(initiation.getDebtorAccount())) {
            dataInitiation6.debtorAccount(
                    new OBWriteDomestic2DataInitiationDebtorAccount()
                            .identification(initiation.getDebtorAccount().getIdentification())
                            .secondaryIdentification(initiation.getDebtorAccount().getSecondaryIdentification())
                            .schemeName(initiation.getDebtorAccount().getSchemeName())
                            .name(initiation.getDebtorAccount().getName())
            );
        }
        return dataInitiation6;
    }

    public static OBWriteInternationalStandingOrderConsentResponse6 createTestDataInternationalStandingOrderConsentResponse6(
            OBWriteInternationalStandingOrder4 payment
    ) {
        List<OBWriteDomesticConsentResponse4DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse4DataCharges());
        return new OBWriteInternationalStandingOrderConsentResponse6()
                .data(
                        new OBWriteInternationalStandingOrderConsentResponse6Data()
                                .readRefundAccount(OBReadRefundAccountEnum.YES)
                                .initiation(formatDates(payment.getData().getInitiation()))
                                .charges(charges)
                )
                .risk(payment.getRisk());
    }

    public static OBWriteInternationalStandingOrderConsentResponse7 createTestDataInternationalStandingOrderConsentResponse7(
            OBWriteInternationalStandingOrder4 payment
    ) {
        List<OBWriteDomesticConsentResponse5DataCharges> charges = new ArrayList<>();
        charges.add(new OBWriteDomesticConsentResponse5DataCharges());
        return new OBWriteInternationalStandingOrderConsentResponse7()
                .data(
                        new OBWriteInternationalStandingOrderConsentResponse7Data()
                                .readRefundAccount(OBReadRefundAccountEnum.YES)
                                .initiation(toOBWriteInternationalStandingOrderConsentResponse7DataInitiation(payment.getData().getInitiation()))
                                .charges(charges)
                )
                .risk(payment.getRisk());
    }

    public static OBWriteInternationalStandingOrderConsentResponse7DataInitiation toOBWriteInternationalStandingOrderConsentResponse7DataInitiation(
            OBWriteInternationalStandingOrder4DataInitiation initiation
    ) {
        OBWriteInternationalStandingOrderConsentResponse7DataInitiation consentResponse7DataInitiation =
                new OBWriteInternationalStandingOrderConsentResponse7DataInitiation()
                        .chargeBearer(initiation.getChargeBearer())
                        .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                        .destinationCountryCode(initiation.getDestinationCountryCode())
                        .extendedPurpose(initiation.getExtendedPurpose())
                        .finalPaymentDateTime(initiation.getFinalPaymentDateTime().withZone(DateTimeZone.UTC))
                        .firstPaymentDateTime(initiation.getFirstPaymentDateTime().withZone(DateTimeZone.UTC))
                        .frequency(initiation.getFrequency())
                        .numberOfPayments(initiation.getNumberOfPayments())
                        .purpose(initiation.getPurpose())
                        .reference(initiation.getReference())
                        .instructedAmount(initiation.getInstructedAmount())
                        .creditorAccount(initiation.getCreditorAccount())
                        .creditor(initiation.getCreditor())
                        .creditorAgent(initiation.getCreditorAgent())
                        .supplementaryData(initiation.getSupplementaryData());

        if (Objects.nonNull(initiation.getDebtorAccount())) {
            consentResponse7DataInitiation.debtorAccount(
                    new OBWriteDomestic2DataInitiationDebtorAccount()
                            .identification(initiation.getDebtorAccount().getIdentification())
                            .secondaryIdentification(initiation.getDebtorAccount().getSecondaryIdentification())
                            .schemeName(initiation.getDebtorAccount().getSchemeName())
                            .name(initiation.getDebtorAccount().getName())
            );
        }
        return consentResponse7DataInitiation;
    }

    private static OBWriteInternationalStandingOrder4DataInitiation formatDates(OBWriteInternationalStandingOrder4DataInitiation initiation) {
        initiation.finalPaymentDateTime(
                Objects.nonNull(initiation.getFinalPaymentDateTime()) ?
                        initiation.getFinalPaymentDateTime().withZone(DateTimeZone.UTC) :
                        null
        );
        initiation.firstPaymentDateTime(
                Objects.nonNull(initiation.getFirstPaymentDateTime()) ?
                        initiation.getFirstPaymentDateTime().withZone(DateTimeZone.UTC) :
                        null
        );

        return initiation;
    }
}