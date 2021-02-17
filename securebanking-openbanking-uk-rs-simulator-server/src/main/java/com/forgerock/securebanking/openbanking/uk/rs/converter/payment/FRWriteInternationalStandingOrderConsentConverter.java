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

import com.forgerock.securebanking.openbanking.uk.rs.converter.FRAccountIdentifierConverter;
import com.forgerock.securebanking.openbanking.uk.rs.converter.FRAmountConverter;
import com.forgerock.securebanking.openbanking.uk.rs.converter.FRFinancialInstrumentConverter;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderConsentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderDataInitiation;
import uk.org.openbanking.datamodel.account.OBCashAccount3;
import uk.org.openbanking.datamodel.payment.*;

import static uk.org.openbanking.datamodel.service.converter.payment.CountryCodeHelper.determineCountryCode;

public class FRWriteInternationalStandingOrderConsentConverter {

    // OB to FR
    public static FRWriteInternationalStandingOrderConsent toFRWriteInternationalStandingOrderConsent(OBWriteInternationalStandingOrderConsent1 obWriteInternationalStandingOrderConsent1) {
        return obWriteInternationalStandingOrderConsent1 == null ? null : FRWriteInternationalStandingOrderConsent.builder()
                .data(toFRWriteInternationalStandingOrderConsentData(obWriteInternationalStandingOrderConsent1.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalStandingOrderConsent1.getRisk()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsent toFRWriteInternationalStandingOrderConsent(OBWriteInternationalStandingOrderConsent2 obWriteInternationalStandingOrderConsent2) {
        return obWriteInternationalStandingOrderConsent2 == null ? null : FRWriteInternationalStandingOrderConsent.builder()
                .data(toFRWriteInternationalStandingOrderConsentData(obWriteInternationalStandingOrderConsent2.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalStandingOrderConsent2.getRisk()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsent toFRWriteInternationalStandingOrderConsent(OBWriteInternationalStandingOrderConsent3 obWriteInternationalStandingOrderConsent3) {
        return obWriteInternationalStandingOrderConsent3 == null ? null : FRWriteInternationalStandingOrderConsent.builder()
                .data(toFRWriteInternationalStandingOrderConsentData(obWriteInternationalStandingOrderConsent3.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalStandingOrderConsent3.getRisk()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsent toFRWriteInternationalStandingOrderConsent(OBWriteInternationalStandingOrderConsent5 obWriteInternationalStandingOrderConsent5) {
        return obWriteInternationalStandingOrderConsent5 == null ? null : FRWriteInternationalStandingOrderConsent.builder()
                .data(toFRWriteInternationalStandingOrderConsentData(obWriteInternationalStandingOrderConsent5.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalStandingOrderConsent5.getRisk()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsent toFRWriteInternationalStandingOrderConsent(OBWriteInternationalStandingOrderConsent6 obWriteInternationalStandingOrderConsent6) {
        return obWriteInternationalStandingOrderConsent6 == null ? null : FRWriteInternationalStandingOrderConsent.builder()
                .data(toFRWriteInternationalStandingOrderConsentData(obWriteInternationalStandingOrderConsent6.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalStandingOrderConsent6.getRisk()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsentData toFRWriteInternationalStandingOrderConsentData(OBWriteDataInternationalStandingOrderConsent1 data) {
        return data == null ? null : FRWriteInternationalStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteInternationalStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsentData toFRWriteInternationalStandingOrderConsentData(OBWriteDataInternationalStandingOrderConsent2 data) {
        return data == null ? null : FRWriteInternationalStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteInternationalStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsentData toFRWriteInternationalStandingOrderConsentData(OBWriteDataInternationalStandingOrderConsent3 data) {
        return data == null ? null : FRWriteInternationalStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteInternationalStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsentData toFRWriteInternationalStandingOrderConsentData(OBWriteInternationalStandingOrderConsent5Data data) {
        return data == null ? null : FRWriteInternationalStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteInternationalStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteInternationalStandingOrderConsentData toFRWriteInternationalStandingOrderConsentData(OBWriteInternationalStandingOrderConsent6Data data) {
        return data == null ? null : FRWriteInternationalStandingOrderConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .readRefundAccount(FRReadRefundAccountConverter.toFRReadRefundAccount(data.getReadRefundAccount()))
                .initiation(toFRWriteInternationalStandingOrderDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteInternationalStandingOrderDataInitiation toFRWriteInternationalStandingOrderDataInitiation(OBInternationalStandingOrder1 initiation) {
        OBCashAccount3 creditorAccount = initiation.getCreditorAccount();
        return initiation == null ? null : FRWriteInternationalStandingOrderDataInitiation.builder()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toFRChargeBearerType(initiation.getChargeBearer()))
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(determineCountryCode(creditorAccount.getSchemeName(), creditorAccount.getIdentification())) // default value to prevent validation error
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toFRFinancialCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(creditorAccount))
                .build();
    }

    public static FRWriteInternationalStandingOrderDataInitiation toFRWriteInternationalStandingOrderDataInitiation(OBInternationalStandingOrder2 initiation) {
        OBCashAccount3 creditorAccount = initiation.getCreditorAccount();
        return initiation == null ? null : FRWriteInternationalStandingOrderDataInitiation.builder()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toFRChargeBearerType(initiation.getChargeBearer()))
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(determineCountryCode(creditorAccount.getSchemeName(), creditorAccount.getIdentification())) // default value to prevent validation error
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toFRFinancialCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(creditorAccount))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    public static FRWriteInternationalStandingOrderDataInitiation toFRWriteInternationalStandingOrderDataInitiation(OBInternationalStandingOrder3 initiation) {
        OBCashAccountCreditor3 creditorAccount = initiation.getCreditorAccount();
        return initiation == null ? null : FRWriteInternationalStandingOrderDataInitiation.builder()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toFRChargeBearerType(initiation.getChargeBearer()))
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(determineCountryCode(creditorAccount.getSchemeName(), creditorAccount.getIdentification())) // default value to prevent validation error
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toFRFinancialCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(creditorAccount))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    public static FRWriteInternationalStandingOrderDataInitiation toFRWriteInternationalStandingOrderDataInitiation(OBWriteInternationalStandingOrder4DataInitiation initiation) {
        return initiation == null ? null : FRWriteInternationalStandingOrderDataInitiation.builder()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .purpose(initiation.getPurpose())
                .extendedPurpose(initiation.getExtendedPurpose())
                .chargeBearer(FRChargeBearerConverter.toFRChargeBearerType(initiation.getChargeBearer()))
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(initiation.getDestinationCountryCode())
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toFRFinancialCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    // FR to OB
    public static OBWriteInternationalStandingOrder4DataInitiation toOBWriteInternationalStandingOrder4DataInitiation(FRWriteInternationalStandingOrderDataInitiation initiation) {
        return initiation == null ? null : new OBWriteInternationalStandingOrder4DataInitiation()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .purpose(initiation.getPurpose())
                .extendedPurpose(initiation.getExtendedPurpose())
                .chargeBearer(FRChargeBearerConverter.toOBChargeBearerType1Code(initiation.getChargeBearer()))
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(initiation.getDestinationCountryCode())
                .instructedAmount(FRAmountConverter.toOBWriteDomestic2DataInitiationInstructedAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBWriteDomesticStandingOrder3DataInitiationDebtorAccount(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toOBWriteInternational3DataInitiationCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBWriteInternationalStandingOrder4DataInitiationCreditorAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBWriteInternationalStandingOrder4DataInitiationCreditorAccount(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }

    public static OBInternationalStandingOrder1 toOBInternationalStandingOrder1(FRWriteInternationalStandingOrderDataInitiation initiation) {
        return initiation == null ? null : new OBInternationalStandingOrder1()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toOBChargeBearerType1Code(initiation.getChargeBearer()))
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toOBPartyIdentification43(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification3(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()));
    }

    public static OBInternationalStandingOrder2 toOBInternationalStandingOrder2(FRWriteInternationalStandingOrderDataInitiation initiation) {
        return initiation == null ? null : new OBInternationalStandingOrder2()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toOBChargeBearerType1Code(initiation.getChargeBearer()))
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toOBPartyIdentification43(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification3(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }

    public static OBInternationalStandingOrder3 toOBInternationalStandingOrder3(FRWriteInternationalStandingOrderDataInitiation initiation) {
        return initiation == null ? null : new OBInternationalStandingOrder3()
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toOBChargeBearerType1Code(initiation.getChargeBearer()))
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .instructedAmount(FRAmountConverter.toOBDomestic2InstructedAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccountDebtor4(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toOBPartyIdentification43(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification6(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccountCreditor3(initiation.getCreditorAccount()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }
}
