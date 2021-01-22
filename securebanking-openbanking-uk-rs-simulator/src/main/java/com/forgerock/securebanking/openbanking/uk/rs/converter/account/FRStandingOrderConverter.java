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
package com.forgerock.securebanking.openbanking.uk.rs.converter.account;

import com.forgerock.securebanking.openbanking.uk.rs.converter.FRAccountIdentifierConverter;
import com.forgerock.securebanking.openbanking.uk.rs.converter.FRAmountConverter;
import com.forgerock.securebanking.openbanking.uk.rs.converter.FRFinancialInstrumentConverter;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData;
import uk.org.openbanking.datamodel.account.*;

public class FRStandingOrderConverter {

    // FR to OB
    public static OBStandingOrder1 toOBStandingOrder1(FRStandingOrderData standingOrder) {
        return standingOrder == null ? null : new OBStandingOrder1()
                .accountId(standingOrder.getAccountId())
                .standingOrderId(standingOrder.getStandingOrderId())
                .frequency(standingOrder.getFrequency())
                .reference(standingOrder.getReference())
                .firstPaymentDateTime(standingOrder.getFirstPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFirstPaymentAmount()))
                .nextPaymentDateTime(standingOrder.getNextPaymentDateTime())
                .nextPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getNextPaymentAmount()))
                .finalPaymentDateTime(standingOrder.getFinalPaymentDateTime())
                .finalPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFinalPaymentAmount()))
                .servicer(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification2(standingOrder.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount1(standingOrder.getCreditorAccount()));
    }

    public static OBStandingOrder2 toOBStandingOrder2(FRStandingOrderData standingOrder) {
        return standingOrder == null ? null : new OBStandingOrder2()
                .accountId(standingOrder.getAccountId())
                .standingOrderId(standingOrder.getStandingOrderId())
                .frequency(standingOrder.getFrequency())
                .reference(standingOrder.getReference())
                .firstPaymentDateTime(standingOrder.getFirstPaymentDateTime())
                .firstPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFirstPaymentAmount()))
                .nextPaymentDateTime(standingOrder.getNextPaymentDateTime())
                .nextPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getNextPaymentAmount()))
                .finalPaymentDateTime(standingOrder.getFinalPaymentDateTime())
                .finalPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFinalPaymentAmount()))
                .standingOrderStatusCode(toOBExternalStandingOrderStatus1Code(standingOrder.getStandingOrderStatusCode()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification2(standingOrder.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount1(standingOrder.getCreditorAccount()));
    }

    public static OBStandingOrder3 toOBStandingOrder3(FRStandingOrderData standingOrder) {
        return standingOrder == null ? null : new OBStandingOrder3()
                .accountId(standingOrder.getAccountId())
                .standingOrderId(standingOrder.getStandingOrderId())
                .frequency(standingOrder.getFrequency())
                .reference(standingOrder.getReference())
                .firstPaymentDateTime(standingOrder.getFirstPaymentDateTime())
                .nextPaymentDateTime(standingOrder.getNextPaymentDateTime())
                .finalPaymentDateTime(standingOrder.getFinalPaymentDateTime())
                .standingOrderStatusCode(toOBExternalStandingOrderStatus1Code(standingOrder.getStandingOrderStatusCode()))
                .firstPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFirstPaymentAmount()))
                .nextPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getNextPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFinalPaymentAmount()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification4(standingOrder.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(standingOrder.getCreditorAccount()));
    }

    public static OBStandingOrder4 toOBStandingOrder4(FRStandingOrderData standingOrder) {
        return standingOrder == null ? null : new OBStandingOrder4()
                .accountId(standingOrder.getAccountId())
                .standingOrderId(standingOrder.getStandingOrderId())
                .frequency(standingOrder.getFrequency())
                .reference(standingOrder.getReference())
                .firstPaymentDateTime(standingOrder.getFirstPaymentDateTime())
                .nextPaymentDateTime(standingOrder.getNextPaymentDateTime())
                .finalPaymentDateTime(standingOrder.getFinalPaymentDateTime())
                .standingOrderStatusCode(toOBExternalStandingOrderStatus1Code(standingOrder.getStandingOrderStatusCode()))
                .firstPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFirstPaymentAmount()))
                .nextPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getNextPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFinalPaymentAmount()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toOBSupplementaryData1(standingOrder.getSupplementaryData()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification4(standingOrder.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(standingOrder.getCreditorAccount()));
    }

    public static OBStandingOrder5 toOBStandingOrder5(FRStandingOrderData standingOrder) {
        return standingOrder == null ? null : new OBStandingOrder5()
                .accountId(standingOrder.getAccountId())
                .standingOrderId(standingOrder.getStandingOrderId())
                .frequency(standingOrder.getFrequency())
                .reference(standingOrder.getReference())
                .firstPaymentDateTime(standingOrder.getFirstPaymentDateTime())
                .nextPaymentDateTime(standingOrder.getNextPaymentDateTime())
                .finalPaymentDateTime(standingOrder.getFinalPaymentDateTime())
                .standingOrderStatusCode(toOBExternalStandingOrderStatus1Code(standingOrder.getStandingOrderStatusCode()))
                .firstPaymentAmount(FRAmountConverter.toAccountOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFirstPaymentAmount()))
                .nextPaymentAmount(FRAmountConverter.toAccountOBActiveOrHistoricCurrencyAndAmount(standingOrder.getNextPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toAccountOBActiveOrHistoricCurrencyAndAmount(standingOrder.getFinalPaymentAmount()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toOBSupplementaryData1(standingOrder.getSupplementaryData()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification5(standingOrder.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount5(standingOrder.getCreditorAccount()));
    }

    public static OBStandingOrder6 toOBStandingOrder6(FRStandingOrderData standingOrder) {
        return standingOrder == null ? null : new OBStandingOrder6()
                .accountId(standingOrder.getAccountId())
                .standingOrderId(standingOrder.getStandingOrderId())
                .frequency(standingOrder.getFrequency())
                .reference(standingOrder.getReference())
                .firstPaymentDateTime(standingOrder.getFirstPaymentDateTime())
                .nextPaymentDateTime(standingOrder.getNextPaymentDateTime())
                .lastPaymentDateTime(standingOrder.getLastPaymentDateTime())
                .finalPaymentDateTime(standingOrder.getFinalPaymentDateTime())
                .numberOfPayments(standingOrder.getNumberOfPayments())
                .standingOrderStatusCode(toOBExternalStandingOrderStatus1Code(standingOrder.getStandingOrderStatusCode()))
                .firstPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount2(standingOrder.getFirstPaymentAmount()))
                .nextPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount3(standingOrder.getNextPaymentAmount()))
                .lastPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount11(standingOrder.getLastPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount4(standingOrder.getFinalPaymentAmount()))
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification51(standingOrder.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount51(standingOrder.getCreditorAccount()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toOBSupplementaryData1(standingOrder.getSupplementaryData()));
    }

    public static OBExternalStandingOrderStatus1Code toOBExternalStandingOrderStatus1Code(FRStandingOrderData.FRStandingOrderStatus standingOrderStatusCode) {
        return standingOrderStatusCode == null ? null : OBExternalStandingOrderStatus1Code.valueOf(standingOrderStatusCode.name());
    }

    // OB to FR
    public static FRStandingOrderData toFRStandingOrderData(OBStandingOrder5 obStandingOrder) {
        return obStandingOrder == null ? null : FRStandingOrderData.builder()
                .accountId(obStandingOrder.getAccountId())
                .standingOrderId(obStandingOrder.getStandingOrderId())
                .frequency(obStandingOrder.getFrequency())
                .reference(obStandingOrder.getReference())
                .firstPaymentDateTime(obStandingOrder.getFirstPaymentDateTime())
                .nextPaymentDateTime(obStandingOrder.getNextPaymentDateTime())
                .finalPaymentDateTime(obStandingOrder.getFinalPaymentDateTime())
                .standingOrderStatusCode(toFRStandingOrderStatus(obStandingOrder.getStandingOrderStatusCode()))
                .firstPaymentAmount(FRAmountConverter.toFRAmount(obStandingOrder.getFirstPaymentAmount()))
                .nextPaymentAmount(FRAmountConverter.toFRAmount(obStandingOrder.getNextPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toFRAmount(obStandingOrder.getFinalPaymentAmount()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(obStandingOrder.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(obStandingOrder.getCreditorAccount()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toFRSupplementaryData(obStandingOrder.getSupplementaryData()))
                .build();
    }

    public static FRStandingOrderData toFRStandingOrderData(OBStandingOrder6 obStandingOrder) {
        return obStandingOrder == null ? null : FRStandingOrderData.builder()
                .accountId(obStandingOrder.getAccountId())
                .standingOrderId(obStandingOrder.getStandingOrderId())
                .frequency(obStandingOrder.getFrequency())
                .reference(obStandingOrder.getReference())
                .firstPaymentDateTime(obStandingOrder.getFirstPaymentDateTime())
                .nextPaymentDateTime(obStandingOrder.getNextPaymentDateTime())
                .lastPaymentDateTime(obStandingOrder.getLastPaymentDateTime())
                .finalPaymentDateTime(obStandingOrder.getFinalPaymentDateTime())
                .numberOfPayments(obStandingOrder.getNumberOfPayments())
                .standingOrderStatusCode(toFRStandingOrderStatus(obStandingOrder.getStandingOrderStatusCode()))
                .firstPaymentAmount(FRAmountConverter.toFRAmount(obStandingOrder.getFirstPaymentAmount()))
                .nextPaymentAmount(FRAmountConverter.toFRAmount(obStandingOrder.getNextPaymentAmount()))
                .lastPaymentAmount(FRAmountConverter.toFRAmount(obStandingOrder.getLastPaymentAmount()))
                .finalPaymentAmount(FRAmountConverter.toFRAmount(obStandingOrder.getFinalPaymentAmount()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(obStandingOrder.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(obStandingOrder.getCreditorAccount()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toFRSupplementaryData(obStandingOrder.getSupplementaryData()))
                .build();
    }

    public static FRStandingOrderData.FRStandingOrderStatus toFRStandingOrderStatus(OBExternalStandingOrderStatus1Code standingOrderStatusCode) {
        return standingOrderStatusCode == null ? null : FRStandingOrderData.FRStandingOrderStatus.valueOf(standingOrderStatusCode.name());
    }
}
