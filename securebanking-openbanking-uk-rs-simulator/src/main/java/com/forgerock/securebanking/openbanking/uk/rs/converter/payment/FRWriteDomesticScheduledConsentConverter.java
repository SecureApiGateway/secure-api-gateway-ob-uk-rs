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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticScheduledConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticScheduledConsentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticScheduledDataInitiation;
import uk.org.openbanking.datamodel.payment.*;

public class FRWriteDomesticScheduledConsentConverter {

    // OB to FR
    public static FRWriteDomesticScheduledConsent toFRWriteDomesticScheduledConsent(OBWriteDomesticScheduledConsent1 obWriteDomesticScheduledConsent1) {
        return obWriteDomesticScheduledConsent1 == null ? null : FRWriteDomesticScheduledConsent.builder()
                .data(toFRWriteDomesticScheduledConsentData(obWriteDomesticScheduledConsent1.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticScheduledConsent1.getRisk()))
                .build();
    }

    public static FRWriteDomesticScheduledConsent toFRWriteDomesticScheduledConsent(OBWriteDomesticScheduledConsent2 obWriteDomesticScheduledConsent2) {
        return obWriteDomesticScheduledConsent2 == null ? null : FRWriteDomesticScheduledConsent.builder()
                .data(toFRWriteDomesticScheduledConsentData(obWriteDomesticScheduledConsent2.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticScheduledConsent2.getRisk()))
                .build();
    }

    public static FRWriteDomesticScheduledConsent toFRWriteDomesticScheduledConsent(OBWriteDomesticScheduledConsent3 obWriteDomesticScheduledConsent3) {
        return obWriteDomesticScheduledConsent3 == null ? null : FRWriteDomesticScheduledConsent.builder()
                .data(toFRWriteDomesticScheduledConsentData(obWriteDomesticScheduledConsent3.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticScheduledConsent3.getRisk()))
                .build();
    }

    public static FRWriteDomesticScheduledConsent toFRWriteDomesticScheduledConsent(OBWriteDomesticScheduledConsent4 obWriteDomesticScheduledConsent4) {
        return obWriteDomesticScheduledConsent4 == null ? null : FRWriteDomesticScheduledConsent.builder()
                .data(toFRWriteDomesticScheduledConsentData(obWriteDomesticScheduledConsent4.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticScheduledConsent4.getRisk()))
                .build();
    }

    public static FRWriteDomesticScheduledConsentData toFRWriteDomesticScheduledConsentData(OBWriteDataDomesticScheduledConsent1 data) {
        return data == null ? null : FRWriteDomesticScheduledConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteDomesticScheduledDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteDomesticScheduledConsentData toFRWriteDomesticScheduledConsentData(OBWriteDataDomesticScheduledConsent2 data) {
        return data == null ? null : FRWriteDomesticScheduledConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteDomesticScheduledDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteDomesticScheduledConsentData toFRWriteDomesticScheduledConsentData(OBWriteDomesticScheduledConsent3Data data) {
        return data == null ? null : FRWriteDomesticScheduledConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .initiation(toFRWriteDomesticScheduledDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteDomesticScheduledConsentData toFRWriteDomesticScheduledConsentData(OBWriteDomesticScheduledConsent4Data data) {
        return data == null ? null : FRWriteDomesticScheduledConsentData.builder()
                .permission(FRPermissionConverter.toFRPermission(data.getPermission()))
                .readRefundAccount(FRReadRefundAccountConverter.toFRReadRefundAccount(data.getReadRefundAccount()))
                .initiation(toFRWriteDomesticScheduledDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteDomesticScheduledDataInitiation toFRWriteDomesticScheduledDataInitiation(OBDomesticScheduled1 initiation) {
        return initiation == null ? null : FRWriteDomesticScheduledDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .build();
    }

    public static FRWriteDomesticScheduledDataInitiation toFRWriteDomesticScheduledDataInitiation(OBDomesticScheduled2 initiation) {
        return initiation == null ? null : FRWriteDomesticScheduledDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    public static FRWriteDomesticScheduledDataInitiation toFRWriteDomesticScheduledDataInitiation(OBWriteDomesticScheduled2DataInitiation initiation) {
        return initiation == null ? null : FRWriteDomesticScheduledDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    // FR to OB
    public static OBWriteDomesticScheduled2DataInitiation toOBWriteDomesticScheduled2DataInitiation(FRWriteDomesticScheduledDataInitiation initiation) {
        return initiation == null ? null : new OBWriteDomesticScheduled2DataInitiation()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .instructedAmount(FRAmountConverter.toOBWriteDomestic2DataInitiationInstructedAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBWriteDomestic2DataInitiationDebtorAccount(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBWriteDomestic2DataInitiationCreditorAccount(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBWriteDomestic2DataInitiationRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }

    public static OBDomesticScheduled1 toOBDomesticScheduled1(FRWriteDomesticScheduledDataInitiation initiation) {
        return initiation == null ? null : new OBDomesticScheduled1()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBRemittanceInformation1(initiation.getRemittanceInformation()));
    }

    public static OBDomesticScheduled2 toOBDomesticScheduled2(FRWriteDomesticScheduledDataInitiation initiation) {
        return initiation == null ? null : new OBDomesticScheduled2()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .requestedExecutionDateTime(initiation.getRequestedExecutionDateTime())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBRemittanceInformation1(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }
}
