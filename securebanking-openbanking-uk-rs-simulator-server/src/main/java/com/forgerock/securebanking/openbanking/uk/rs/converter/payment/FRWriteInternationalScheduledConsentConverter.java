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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduledConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduledConsentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduledDataInitiation;
import uk.org.openbanking.datamodel.account.OBCashAccount3;
import uk.org.openbanking.datamodel.payment.*;

import static uk.org.openbanking.datamodel.service.converter.payment.CountryCodeHelper.determineCountryCode;

public class FRWriteInternationalScheduledConsentConverter {

    // OB to FR
    public static FRWriteInternationalScheduledConsent toFRWriteInternationalScheduledConsent(OBWriteInternationalScheduledConsent1 obWriteInternationalScheduledConsent1) {
        return obWriteInternationalScheduledConsent1 == null ? null : FRWriteInternationalScheduledConsent.builder()
                .data(toFRWriteInternationalScheduledConsentData(obWriteInternationalScheduledConsent1.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalScheduledConsent1.getRisk()))
                .build();
    }

    public static FRWriteInternationalScheduledConsent toFRWriteInternationalScheduledConsent(OBWriteInternationalScheduledConsent2 obWriteInternationalScheduledConsent2) {
        return obWriteInternationalScheduledConsent2 == null ? null : FRWriteInternationalScheduledConsent.builder()
                .data(toFRWriteInternationalScheduledConsentData(obWriteInternationalScheduledConsent2.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalScheduledConsent2.getRisk()))
                .build();
    }

    public static FRWriteInternationalScheduledConsent toFRWriteInternationalScheduledConsent(OBWriteInternationalScheduledConsent4 obWriteInternationalScheduledConsent4) {
        return obWriteInternationalScheduledConsent4 == null ? null : FRWriteInternationalScheduledConsent.builder()
                .data(toFRWriteInternationalScheduledConsentData(obWriteInternationalScheduledConsent4.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalScheduledConsent4.getRisk()))
                .build();
    }

    public static FRWriteInternationalScheduledConsent toFRWriteInternationalScheduledConsent(OBWriteInternationalScheduledConsent5 obWriteInternationalScheduledConsent5) {
        return obWriteInternationalScheduledConsent5 == null ? null : FRWriteInternationalScheduledConsent.builder()
                .data(toFRWriteInternationalScheduledConsentData(obWriteInternationalScheduledConsent5.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteInternationalScheduledConsent5.getRisk()))
                .build();
    }

    public static FRWriteInternationalScheduledConsentData toFRWriteInternationalScheduledConsentData(OBWriteDataInternationalScheduledConsent1 data) {
        return data == null ? null : FRWriteInternationalScheduledConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteInternationalScheduledDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteInternationalScheduledConsentData toFRWriteInternationalScheduledConsentData(OBWriteDataInternationalScheduledConsent2 data) {
        return data == null ? null : FRWriteInternationalScheduledConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteInternationalScheduledDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteInternationalScheduledConsentData toFRWriteInternationalScheduledConsentData(OBWriteInternationalScheduledConsent4Data data) {
        return data == null ? null : FRWriteInternationalScheduledConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteInternationalScheduledDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteInternationalScheduledConsentData toFRWriteInternationalScheduledConsentData(OBWriteInternationalScheduledConsent5Data data) {
        return data == null ? null : FRWriteInternationalScheduledConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .readRefundAccount(FRReadRefundAccountConverter.toFRReadRefundAccount(data.getReadRefundAccount()))
                .initiation(toFRWriteInternationalScheduledDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteInternationalScheduledDataInitiation toFRWriteInternationalScheduledDataInitiation(OBInternationalScheduled1 initiation) {
        OBCashAccount3 creditorAccount = initiation.getCreditorAccount();
        return initiation == null ? null : FRWriteInternationalScheduledDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructionPriority(FRInstructionPriorityConverter.toFRInstructionPriority(initiation.getInstructionPriority()))
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toFRChargeBearerType(initiation.getChargeBearer()))
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(determineCountryCode(creditorAccount.getSchemeName(), creditorAccount.getIdentification())) // default value to prevent validation error
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .exchangeRateInformation(FRExchangeRateConverter.toFRExchangeRateInformation(initiation.getExchangeRateInformation()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toFRFinancialCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(creditorAccount))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .build();
    }

    public static FRWriteInternationalScheduledDataInitiation toFRWriteInternationalScheduledDataInitiation(OBInternationalScheduled2 initiation) {
        OBCashAccount3 creditorAccount = initiation.getCreditorAccount();
        return initiation == null ? null : FRWriteInternationalScheduledDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructionPriority(FRInstructionPriorityConverter.toFRInstructionPriority(initiation.getInstructionPriority()))
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toFRChargeBearerType(initiation.getChargeBearer()))
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(determineCountryCode(creditorAccount.getSchemeName(), creditorAccount.getIdentification())) // default value to prevent validation error
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .exchangeRateInformation(FRExchangeRateConverter.toFRExchangeRateInformation(initiation.getExchangeRateInformation()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toFRFinancialCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(creditorAccount))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    public static FRWriteInternationalScheduledDataInitiation toFRWriteInternationalScheduledDataInitiation(OBWriteInternationalScheduled3DataInitiation initiation) {
        return initiation == null ? null : FRWriteInternationalScheduledDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructionPriority(FRInstructionPriorityConverter.toFRInstructionPriority(initiation.getInstructionPriority()))
                .purpose(initiation.getPurpose())
                .extendedPurpose(initiation.getExtendedPurpose())
                .chargeBearer(FRChargeBearerConverter.toFRChargeBearerType(initiation.getChargeBearer()))
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(initiation.getDestinationCountryCode())
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .exchangeRateInformation(FRExchangeRateConverter.toFRExchangeRateInformation(initiation.getExchangeRateInformation()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toFRFinancialCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }


    // FR to OB
    public static OBWriteInternationalScheduled3DataInitiation toOBWriteInternationalScheduled3DataInitiation(FRWriteInternationalScheduledDataInitiation initiation) {
        return initiation == null ? null : new OBWriteInternationalScheduled3DataInitiation()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructionPriority(FRInstructionPriorityConverter.toOBWriteInternationalScheduled3DataInitiationInstructionPriority(initiation.getInstructionPriority()))
                .purpose(initiation.getPurpose())
                .extendedPurpose(initiation.getExtendedPurpose())
                .chargeBearer(FRChargeBearerConverter.toOBChargeBearerType1Code(initiation.getChargeBearer()))
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .destinationCountryCode(initiation.getDestinationCountryCode())
                .instructedAmount(FRAmountConverter.toOBWriteDomestic2DataInitiationInstructedAmount(initiation.getInstructedAmount()))
                .exchangeRateInformation(FRExchangeRateConverter.toOBWriteInternational3DataInitiationExchangeRateInformation(initiation.getExchangeRateInformation()))
                .debtorAccount(FRAccountIdentifierConverter.toOBWriteDomestic2DataInitiationDebtorAccount(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toOBWriteInternational3DataInitiationCreditor(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBWriteInternational3DataInitiationCreditorAgent(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBWriteDomestic2DataInitiationCreditorAccount(initiation.getCreditorAccount()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBWriteDomestic2DataInitiationRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }

    public static OBInternationalScheduled1 toOBInternationalScheduled1(FRWriteInternationalScheduledDataInitiation initiation) {
        return initiation == null ? null : new OBInternationalScheduled1()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructionPriority(FRInstructionPriorityConverter.toOBPriority2Code(initiation.getInstructionPriority()))
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toOBChargeBearerType1Code(initiation.getChargeBearer()))
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                .exchangeRateInformation(FRExchangeRateConverter.toOBExchangeRate1(initiation.getExchangeRateInformation()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toOBPartyIdentification43(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification3(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBRemittanceInformation1(initiation.getRemittanceInformation()));
    }

    public static OBInternationalScheduled2 toOBInternationalScheduled2(FRWriteInternationalScheduledDataInitiation initiation) {
        return initiation == null ? null : new OBInternationalScheduled2()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructionPriority(FRInstructionPriorityConverter.toOBPriority2Code(initiation.getInstructionPriority()))
                .purpose(initiation.getPurpose())
                .chargeBearer(FRChargeBearerConverter.toOBChargeBearerType1Code(initiation.getChargeBearer()))
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .currencyOfTransfer(initiation.getCurrencyOfTransfer())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                .exchangeRateInformation(FRExchangeRateConverter.toOBExchangeRate1(initiation.getExchangeRateInformation()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditor(FRFinancialInstrumentConverter.toOBPartyIdentification43(initiation.getCreditor()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification3(initiation.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBRemittanceInformation1(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }
}
