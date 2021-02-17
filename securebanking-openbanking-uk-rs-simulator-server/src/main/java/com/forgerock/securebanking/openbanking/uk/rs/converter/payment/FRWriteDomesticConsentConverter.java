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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticConsent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticConsentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticDataInitiation;
import uk.org.openbanking.datamodel.payment.*;
import uk.org.openbanking.datamodel.payment.paymentsetup.OBPaymentSetup1;

public class FRWriteDomesticConsentConverter {

    // OB to FR
    public static FRWriteDomesticConsent toFRWriteDomesticConsent(OBWriteDomesticConsent1 obWriteDomesticConsent1) {
        return obWriteDomesticConsent1 == null ? null : FRWriteDomesticConsent.builder()
                .data(toFRWriteDomesticConsentData(obWriteDomesticConsent1.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticConsent1.getRisk()))
                .build();
    }

    public static FRWriteDomesticConsent toFRWriteDomesticConsent(OBWriteDomesticConsent2 obWriteDomesticConsent2) {
        return obWriteDomesticConsent2 == null ? null : FRWriteDomesticConsent.builder()
                .data(toFRWriteDomesticConsentData(obWriteDomesticConsent2.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticConsent2.getRisk()))
                .build();
    }

    public static FRWriteDomesticConsent toFRWriteDomesticConsent(OBWriteDomesticConsent3 obWriteDomesticConsent3) {
        return obWriteDomesticConsent3 == null ? null : FRWriteDomesticConsent.builder()
                .data(toFRWriteDomesticConsentData(obWriteDomesticConsent3.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticConsent3.getRisk()))
                .build();
    }

    public static FRWriteDomesticConsent toFRWriteDomesticConsent(OBWriteDomesticConsent4 obWriteDomesticConsent4) {
        return obWriteDomesticConsent4 == null ? null : FRWriteDomesticConsent.builder()
                .data(toFRWriteDomesticConsentData(obWriteDomesticConsent4.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obWriteDomesticConsent4.getRisk()))
                .build();
    }

    public static FRWriteDomesticConsentData toFRWriteDomesticConsentData(OBWriteDataDomesticConsent1 data) {
        return data == null ? null : FRWriteDomesticConsentData.builder()
                .initiation(toFRWriteDomesticDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteDomesticConsentData toFRWriteDomesticConsentData(OBWriteDataDomesticConsent2 data) {
        return data == null ? null : FRWriteDomesticConsentData.builder()
                .initiation(toFRWriteDomesticDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .build();
    }

    public static FRWriteDomesticConsentData toFRWriteDomesticConsentData(OBWriteDomesticConsent3Data data) {
        return data == null ? null : FRWriteDomesticConsentData.builder()
                .initiation(toFRWriteDomesticDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteDomesticConsentData toFRWriteDomesticConsentData(OBWriteDomesticConsent4Data data) {
        return data == null ? null : FRWriteDomesticConsentData.builder()
                .readRefundAccount(FRReadRefundAccountConverter.toFRReadRefundAccount(data.getReadRefundAccount()))
                .initiation(toFRWriteDomesticDataInitiation(data.getInitiation()))
                .authorisation(FRDataAuthorisationConverter.toFRDataAuthorisation(data.getAuthorisation()))
                .scASupportData(FRDataSCASupportDataConverter.toFRDataSCASupportData(data.getScASupportData()))
                .build();
    }

    public static FRWriteDomesticDataInitiation toFRWriteDomesticDataInitiation(OBDomestic2 initiation) {
        return initiation == null ? null : FRWriteDomesticDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    public static FRWriteDomesticDataInitiation toFRWriteDomesticDataInitiation(OBWriteDomestic2DataInitiation initiation) {
        return initiation == null ? null : FRWriteDomesticDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toFRSupplementaryData(initiation.getSupplementaryData()))
                .build();
    }

    public static FRWriteDomesticDataInitiation toFRWriteDomesticDataInitiation(OBDomestic1 initiation) {
        return initiation == null ? null : FRWriteDomesticDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .build();
    }

    public static FRWriteDomesticConsent toFRWriteDomesticConsent(OBPaymentSetup1 obPaymentSetup1) {
        return obPaymentSetup1 == null ? null : FRWriteDomesticConsent.builder()
                .data(toFRWriteDomesticConsentData(obPaymentSetup1.getData()))
                .risk(FRPaymentRiskConverter.toFRRisk(obPaymentSetup1.getRisk()))
                .build();
    }

    public static FRWriteDomesticConsentData toFRWriteDomesticConsentData(OBPaymentDataSetup1 data) {
        return data == null ? null : FRWriteDomesticConsentData.builder()
                .initiation(toFRWriteDomesticDataInitiation(data.getInitiation()))
                .build();
    }

    public static FRWriteDomesticDataInitiation toFRWriteDomesticDataInitiation(OBInitiation1 initiation) {
        return initiation == null ? null : FRWriteDomesticDataInitiation.builder()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(null)
                .instructedAmount(FRAmountConverter.toFRAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(initiation.getCreditorAccount()))
                .creditorPostalAddress(null)
                .remittanceInformation(FRRemittanceInformationConverter.toFRRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(null)
                .build();
    }


    // FR to OB
    public static OBWriteDomestic2DataInitiation toOBWriteDomestic2DataInitiation(FRWriteDomesticDataInitiation initiation) {
        return initiation == null ? null : new OBWriteDomestic2DataInitiation()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructedAmount(FRAmountConverter.toOBWriteDomestic2DataInitiationInstructedAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBWriteDomestic2DataInitiationDebtorAccount(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBWriteDomestic2DataInitiationCreditorAccount(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBWriteDomestic2DataInitiationRemittanceInformation(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }

    public static OBInitiation1 toOBInitiation1(FRWriteDomesticDataInitiation initiation) {
        return initiation == null ? null : new OBInitiation1()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                //.debtorAgent(initiation.getDebtorAgent()) // this field isn't available in v3.x, so isn't stored in the repository
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccountDebtor1(initiation.getDebtorAccount()))
                //.creditorAgent(initiation.getCreditorAgent()) // this field isn't available in v3.x, so isn't stored in the repository
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccountCreditor1(initiation.getCreditorAccount()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBRemittanceInformation1(initiation.getRemittanceInformation()));
    }

    public static OBDomestic1 toOBDomestic1(FRWriteDomesticDataInitiation initiation) {
        return initiation == null ? null : new OBDomestic1()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBRemittanceInformation1(initiation.getRemittanceInformation()));
    }

    public static OBDomestic2 toOBDomestic2(FRWriteDomesticDataInitiation initiation) {
        return initiation == null ? null : new OBDomestic2()
                .instructionIdentification(initiation.getInstructionIdentification())
                .endToEndIdentification(initiation.getEndToEndIdentification())
                .localInstrument(initiation.getLocalInstrument())
                .instructedAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(initiation.getInstructedAmount()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getDebtorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(initiation.getCreditorAccount()))
                .creditorPostalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(initiation.getCreditorPostalAddress()))
                .remittanceInformation(FRRemittanceInformationConverter.toOBRemittanceInformation1(initiation.getRemittanceInformation()))
                .supplementaryData(FRPaymentSupplementaryDataConverter.toOBSupplementaryData1(initiation.getSupplementaryData()));
    }
}
