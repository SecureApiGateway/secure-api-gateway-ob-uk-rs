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

import com.forgerock.securebanking.openbanking.uk.rs.converter.FRAmountConverter;
import com.forgerock.securebanking.openbanking.uk.rs.converter.FRAccountIdentifierConverter;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrderConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrderConsentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrderDataInitiation;
import uk.org.openbanking.datamodel.payment.*;

public class FRWriteDomesticStandingOrderConsentConverter {

    // OB to FR
    public static FRWriteDomesticStandingOrderConsent toFRWriteDomesticStandingOrderConsent(OBWriteDomesticStandingOrderConsent1 obWriteDomesticStandingOrderConsent1) {
        return obWriteDomesticStandingOrderConsent1 == null ? null : FRWriteDomesticStandingOrderConsent.builder()
                .data(toFRWriteDomesticStandingOrderConsent(obWriteDomesticStandingOrderConsent1.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticStandingOrderConsent1.getRisk()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsent toFRWriteDomesticStandingOrderConsent(OBWriteDomesticStandingOrderConsent2 obWriteDomesticStandingOrderConsent2) {
        return obWriteDomesticStandingOrderConsent2 == null ? null : FRWriteDomesticStandingOrderConsent.builder()
                .data(toFRWriteDomesticStandingOrderConsent(obWriteDomesticStandingOrderConsent2.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticStandingOrderConsent2.getRisk()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsent toFRWriteDomesticStandingOrderConsent(OBWriteDomesticStandingOrderConsent3 obWriteDomesticStandingOrderConsent3) {
        return obWriteDomesticStandingOrderConsent3 == null ? null : FRWriteDomesticStandingOrderConsent.builder()
                .data(toFRWriteDomesticStandingOrderConsent(obWriteDomesticStandingOrderConsent3.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticStandingOrderConsent3.getRisk()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsent toFRWriteDomesticStandingOrderConsent(OBWriteDomesticStandingOrderConsent4 obWriteDomesticStandingOrderConsent4) {
        return obWriteDomesticStandingOrderConsent4 == null ? null : FRWriteDomesticStandingOrderConsent.builder()
                .data(toFRWriteDomesticStandingOrderConsent(obWriteDomesticStandingOrderConsent4.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticStandingOrderConsent4.getRisk()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsent toFRWriteDomesticStandingOrderConsent(OBWriteDomesticStandingOrderConsent5 obWriteDomesticStandingOrderConsent5) {
        return obWriteDomesticStandingOrderConsent5 == null ? null : FRWriteDomesticStandingOrderConsent.builder()
                .data(toFRWriteDomesticStandingOrderConsent(obWriteDomesticStandingOrderConsent5.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticStandingOrderConsent5.getRisk()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsentData toFRWriteDomesticStandingOrderConsent(OBWriteDataDomesticStandingOrderConsent1 data) {
        return data == null ? null : FRWriteDomesticStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteDomesticStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsentData toFRWriteDomesticStandingOrderConsent(OBWriteDataDomesticStandingOrderConsent2 data) {
        return data == null ? null : FRWriteDomesticStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteDomesticStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsentData toFRWriteDomesticStandingOrderConsent(OBWriteDataDomesticStandingOrderConsent3 data) {
        return data == null ? null : FRWriteDomesticStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteDomesticStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsentData toFRWriteDomesticStandingOrderConsent(OBWriteDomesticStandingOrderConsent4Data data) {
        return data == null ? null : FRWriteDomesticStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteDomesticStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteDomesticStandingOrderConsentData toFRWriteDomesticStandingOrderConsent(OBWriteDomesticStandingOrderConsent5Data data) {
        return data == null ? null : FRWriteDomesticStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .readRefundAccount(FRReadRefundAccountConverter.toFRReadRefundAccount(data.getReadRefundAccount()))
                .initiation(toFRWriteDomesticStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteDomesticStandingOrderDataInitiation toFRWriteDomesticStandingOrderDataInitiation(OBDomesticStandingOrder1 initiation) {
        return initiation == null ? null : FRWriteDomesticStandingOrderDataInitiation.builder()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toFRAmount(initiation.getFirstPaymentAmount()))
                .recurringPaymentAmount(FRAmountConverter.toFRAmount(initiation.getRecurringPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toFRAmount(initiation.getFinalPaymentAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .build();
    }

    public static FRWriteDomesticStandingOrderDataInitiation toFRWriteDomesticStandingOrderDataInitiation(OBDomesticStandingOrder2 initiation) {
        return initiation == null ? null : FRWriteDomesticStandingOrderDataInitiation.builder()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toFRAmount(initiation.getFirstPaymentAmount()))
                .recurringPaymentAmount(FRAmountConverter.toFRAmount(initiation.getRecurringPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toFRAmount(initiation.getFinalPaymentAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    public static FRWriteDomesticStandingOrderDataInitiation toFRWriteDomesticStandingOrderDataInitiation(OBDomesticStandingOrder3 initiation) {
        return initiation == null ? null : FRWriteDomesticStandingOrderDataInitiation.builder()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toFRAmount(initiation.getFirstPaymentAmount()))
                .recurringPaymentAmount(FRAmountConverter.toFRAmount(initiation.getRecurringPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toFRAmount(initiation.getFinalPaymentAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    public static FRWriteDomesticStandingOrderDataInitiation toFRWriteDomesticStandingOrderDataInitiation(OBWriteDomesticStandingOrder3DataInitiation initiation) {
        return initiation == null ? null : FRWriteDomesticStandingOrderDataInitiation.builder()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toFRAmount(initiation.getFirstPaymentAmount()))
                .recurringPaymentAmount(FRAmountConverter.toFRAmount(initiation.getRecurringPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toFRAmount(initiation.getFinalPaymentAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    // FR to OB
    public static OBWriteDomesticStandingOrder3DataInitiation toOBWriteDomesticStandingOrder3DataInitiation(FRWriteDomesticStandingOrderDataInitiation initiation) {
        return initiation == null ? null : new OBWriteDomesticStandingOrder3DataInitiation()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toOBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount(initiation.getFirstPaymentAmount()))
                .recurringPaymentAmount(FRAmountConverter.toOBWriteDomesticStandingOrder3DataInitiationRecurringPaymentAmount(initiation.getRecurringPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toOBWriteDomesticStandingOrder3DataInitiationFinalPaymentAmount(initiation.getFinalPaymentAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBWriteDomesticStandingOrder3DataInitiationDebtorAccount(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBWriteDomesticStandingOrder3DataInitiationCreditorAccount(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }

    public static OBDomesticStandingOrder1 toOBDomesticStandingOrder1(FRWriteDomesticStandingOrderDataInitiation initiation) {
        return initiation == null ? null : new OBDomesticStandingOrder1()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getFirstPaymentAmount()))
                .recurringPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getRecurringPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getFinalPaymentAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()));
    }

    public static OBDomesticStandingOrder2 toOBDomesticStandingOrder2(FRWriteDomesticStandingOrderDataInitiation initiation) {
        return initiation == null ? null : new OBDomesticStandingOrder2()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getFirstPaymentAmount()))
                .recurringPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getRecurringPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getFinalPaymentAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }

    public static OBDomesticStandingOrder3 toOBDomesticStandingOrder3(FRWriteDomesticStandingOrderDataInitiation initiation) {
        return initiation == null ? null : new OBDomesticStandingOrder3()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .recurringPaymentDateTime(initiation.getRecurringPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toOBDomesticStandingOrder3FirstPaymentAmount(initiation.getFirstPaymentAmount()))
                .recurringPaymentAmount(FRAmountConverter.toOBDomesticStandingOrder3RecurringPaymentAmount(initiation.getRecurringPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toOBDomesticStandingOrder3FinalPaymentAmount(initiation.getFinalPaymentAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccountDebtor4(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccountCreditor3(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }
}
