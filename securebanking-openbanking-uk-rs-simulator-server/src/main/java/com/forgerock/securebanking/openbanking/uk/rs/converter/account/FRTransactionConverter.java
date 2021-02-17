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
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRTransactionData;
import uk.org.openbanking.datamodel.account.*;

import static com.forgerock.securebanking.openbanking.uk.rs.converter.FRFinancialInstrumentConverter.toFRFinancialAgent;

public class FRTransactionConverter {

    // FR to OB
    public static OBTransaction1 toOBTransaction1(FRTransactionData transaction) {
        return transaction == null ? null : new OBTransaction1()
                .accountId(transaction.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionReference(transaction.getTransactionReference())
                .amount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(transaction.getAmount()))
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toOBCreditDebitCode(transaction.getCreditDebitIndicator()))
                .status(toOBEntryStatus1Code(transaction.getStatus()))
                .bookingDateTime(transaction.getBookingDateTime())
                .valueDateTime(transaction.getValueDateTime())
                .transactionInformation(transaction.getTransactionInformation())
                .addressLine(transaction.getAddressLine())
                .bankTransactionCode(toOBBankTransactionCodeStructure1(transaction.getBankTransactionCode()))
                .proprietaryBankTransactionCode(toProprietaryBankTransactionCodeStructure1(transaction.getProprietaryBankTransactionCode()))
                .balance(toOBTransactionCashBalance(transaction.getBalance()))
                .merchantDetails(toOBMerchantDetails1(transaction.getMerchantDetails()));
    }

    public static OBTransaction2 toOBTransaction2(FRTransactionData transaction) {
        return transaction == null ? null : new OBTransaction2()
                .accountId(transaction.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionReference(transaction.getTransactionReference())
                .statementReference(transaction.getStatementReferences())
                .amount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(transaction.getAmount()))
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toOBCreditDebitCode(transaction.getCreditDebitIndicator()))
                .status(toOBEntryStatus1Code(transaction.getStatus()))
                .bookingDateTime(transaction.getBookingDateTime())
                .valueDateTime(transaction.getValueDateTime())
                .addressLine(transaction.getAddressLine())
                .bankTransactionCode(toOBBankTransactionCodeStructure1(transaction.getBankTransactionCode()))
                .proprietaryBankTransactionCode(toProprietaryBankTransactionCodeStructure1(transaction.getProprietaryBankTransactionCode()))
                .equivalentAmount(null)
                .creditorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification2(transaction.getCreditorAgent()))
                .debtorAgent(FRFinancialInstrumentConverter.toOBBranchAndFinancialInstitutionIdentification2(transaction.getDebtorAgent()))
                .cardInstrument(toOBTransactionCardInstrument1(transaction.getCardInstrument()))
                .transactionInformation(transaction.getTransactionInformation())
                .balance(toOBTransactionCashBalance(transaction.getBalance()))
                .merchantDetails(toOBMerchantDetails1(transaction.getMerchantDetails()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount2(transaction.getCreditorAccount()))
                .debtorAccount(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification2(transaction.getDebtorAccount()));
    }

    public static OBTransaction3 toOBTransaction3(FRTransactionData transaction) {
        return transaction == null ? null : new OBTransaction3()
                .accountId(transaction.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionReference(transaction.getTransactionReference())
                .statementReference(transaction.getStatementReferences())
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toOBCreditDebitCode(transaction.getCreditDebitIndicator()))
                .status(toOBEntryStatus1Code(transaction.getStatus()))
                .bookingDateTime(transaction.getBookingDateTime())
                .valueDateTime(transaction.getValueDateTime())
                .addressLine(transaction.getAddressLine())
                .amount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(transaction.getAmount()))
                .chargeAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(transaction.getChargeAmount()))
                .currencyExchange(FRCurrencyExchangeConverter.toOBCurrencyExchange5(transaction.getCurrencyExchange()))
                .bankTransactionCode(toOBBankTransactionCodeStructure1(transaction.getBankTransactionCode()))
                .proprietaryBankTransactionCode(toOBTransaction3ProprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode()))
                .creditorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification3(transaction.getCreditorAgent()))
                .debtorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification3(transaction.getDebtorAgent()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(transaction.getDebtorAccount()))
                .cardInstrument(toOBTransactionCardInstrument1(transaction.getCardInstrument()))
                .transactionInformation(transaction.getTransactionInformation())
                .balance(toOBTransactionCashBalance(transaction.getBalance()))
                .merchantDetails(toOBMerchantDetails1(transaction.getMerchantDetails()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(transaction.getCreditorAccount()));
    }

    public static OBTransaction4 toOBTransaction4(FRTransactionData transaction) {
        return transaction == null ? null : new OBTransaction4()
                .accountId(transaction.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionReference(transaction.getTransactionReference())
                .statementReference(transaction.getStatementReferences())
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toOBCreditDebitCode(transaction.getCreditDebitIndicator()))
                .status(toOBEntryStatus1Code(transaction.getStatus()))
                .bookingDateTime(transaction.getBookingDateTime())
                .valueDateTime(transaction.getValueDateTime())
                .addressLine(transaction.getAddressLine())
                .amount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(transaction.getAmount()))
                .chargeAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(transaction.getChargeAmount()))
                .currencyExchange(FRCurrencyExchangeConverter.toOBCurrencyExchange5(transaction.getCurrencyExchange()))
                .bankTransactionCode(toOBBankTransactionCodeStructure1(transaction.getBankTransactionCode()))
                .proprietaryBankTransactionCode(toOBTransaction3ProprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode()))
                .cardInstrument(toOBTransactionCardInstrument1(transaction.getCardInstrument()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toOBSupplementaryData1(transaction.getSupplementaryData()))
                .transactionInformation(transaction.getTransactionInformation())
                .balance(toOBTransactionCashBalance(transaction.getBalance()))
                .merchantDetails(toOBMerchantDetails1(transaction.getMerchantDetails()))
                .creditorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification3(transaction.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(transaction.getCreditorAccount()))
                .debtorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification3(transaction.getDebtorAgent()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount3(transaction.getDebtorAccount()));
    }

    public static OBTransaction5 toOBTransaction5(FRTransactionData transaction) {
        return transaction == null ? null : new OBTransaction5()
                .accountId(transaction.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionReference(transaction.getTransactionReference())
                .statementReference(transaction.getStatementReferences())
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toOBTransaction5CreditDebitIndicatorEnum(transaction.getCreditDebitIndicator()))
                .status(toOBEntryStatus1Code(transaction.getStatus()))
                .bookingDateTime(transaction.getBookingDateTime())
                .valueDateTime(transaction.getValueDateTime())
                .addressLine(transaction.getAddressLine())
                .amount(FRAmountConverter.toAccountOBActiveOrHistoricCurrencyAndAmount(transaction.getAmount()))
                .chargeAmount(FRAmountConverter.toAccountOBActiveOrHistoricCurrencyAndAmount(transaction.getChargeAmount()))
                .currencyExchange(FRCurrencyExchangeConverter.toOBCurrencyExchange5(transaction.getCurrencyExchange()))
                .bankTransactionCode(toOBBankTransactionCodeStructure1(transaction.getBankTransactionCode()))
                .proprietaryBankTransactionCode(toOBTransaction5ProprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode()))
                .cardInstrument(toOBTransactionCardInstrument1(transaction.getCardInstrument()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toOBSupplementaryData1(transaction.getSupplementaryData()))
                .transactionInformation(transaction.getTransactionInformation())
                .balance(toOBTransactionCashBalance(transaction.getBalance()))
                .merchantDetails(toOBMerchantDetails1(transaction.getMerchantDetails()))
                .creditorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification6(transaction.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount6(transaction.getCreditorAccount()))
                .debtorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification6(transaction.getDebtorAgent()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount6(transaction.getDebtorAccount()));
    }

    public static OBTransaction6 toOBTransaction6(FRTransactionData transaction) {
        return transaction == null ? null : new OBTransaction6()
                .accountId(transaction.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionReference(transaction.getTransactionReference())
                .statementReference(transaction.getStatementReferences())
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toOBCreditDebitCode1(transaction.getCreditDebitIndicator()))
                .status(toOBEntryStatus1Code(transaction.getStatus()))
                .transactionMutability(toOBTransactionMutability1Code(transaction.getTransactionMutability()))
                .bookingDateTime(transaction.getBookingDateTime())
                .valueDateTime(transaction.getValueDateTime())
                .transactionInformation(transaction.getTransactionInformation())
                .addressLine(transaction.getAddressLine())
                .amount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount9(transaction.getAmount()))
                .chargeAmount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount10(transaction.getChargeAmount()))
                .currencyExchange(FRCurrencyExchangeConverter.toOBCurrencyExchange5(transaction.getCurrencyExchange()))
                .bankTransactionCode(toOBBankTransactionCodeStructure1(transaction.getBankTransactionCode()))
                .proprietaryBankTransactionCode(toProprietaryBankTransactionCodeStructure1(transaction.getProprietaryBankTransactionCode()))
                .balance(toOBTransactionCashBalance(transaction.getBalance()))
                .merchantDetails(toOBMerchantDetails1(transaction.getMerchantDetails()))
                .creditorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification61(transaction.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount60(transaction.getCreditorAccount()))
                .debtorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification62(transaction.getDebtorAgent()))
                .debtorAccount(FRAccountIdentifierConverter.toOBCashAccount61(transaction.getDebtorAccount()))
                .cardInstrument(toOBTransactionCardInstrument1(transaction.getCardInstrument()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toOBSupplementaryData1(transaction.getSupplementaryData()));
    }

    public static OBEntryStatus1Code toOBEntryStatus1Code(FRTransactionData.FREntryStatus status) {
        return status == null ? null : OBEntryStatus1Code.valueOf(status.name());
    }

    public static OBTransactionMutability1Code toOBTransactionMutability1Code(FRTransactionData.FRTransactionMutability transactionMutability) {
        return transactionMutability == null ? null : OBTransactionMutability1Code.valueOf(transactionMutability.name());
    }

    public static OBBankTransactionCodeStructure1 toOBBankTransactionCodeStructure1(FRTransactionData.FRBankTransactionCodeStructure transactionCode) {
        return transactionCode == null ? null : new OBBankTransactionCodeStructure1()
                .code(transactionCode.getCode())
                .subCode(transactionCode.getSubCode());
    }

    public static ProprietaryBankTransactionCodeStructure1 toProprietaryBankTransactionCodeStructure1(FRTransactionData.FRProprietaryBankTransactionCodeStructure proprietaryTransactionCode) {
        return proprietaryTransactionCode == null ? null : new ProprietaryBankTransactionCodeStructure1()
                .code(proprietaryTransactionCode.getCode())
                .issuer(proprietaryTransactionCode.getIssuer());
    }

    public static OBTransaction3ProprietaryBankTransactionCode toOBTransaction3ProprietaryBankTransactionCode(FRTransactionData.FRProprietaryBankTransactionCodeStructure proprietaryTransactionCode) {
        return proprietaryTransactionCode == null ? null : new OBTransaction3ProprietaryBankTransactionCode()
                .code(proprietaryTransactionCode.getCode())
                .issuer(proprietaryTransactionCode.getIssuer());
    }

    public static OBTransaction5ProprietaryBankTransactionCode toOBTransaction5ProprietaryBankTransactionCode(FRTransactionData.FRProprietaryBankTransactionCodeStructure proprietaryTransactionCode) {
        return proprietaryTransactionCode == null ? null : new OBTransaction5ProprietaryBankTransactionCode()
                .code(proprietaryTransactionCode.getCode())
                .issuer(proprietaryTransactionCode.getIssuer());
    }

    public static OBTransactionCardInstrument1 toOBTransactionCardInstrument1(FRTransactionData.FRTransactionCardInstrument cardInstrument) {
        return cardInstrument == null ? null : new OBTransactionCardInstrument1()
                .cardSchemeName(toOBExternalCardSchemeType1Code(cardInstrument.getCardSchemeName()))
                .authorisationType(toOBExternalCardAuthorisationType1Code(cardInstrument.getAuthorisationType()))
                .name(cardInstrument.getName())
                .identification(cardInstrument.getIdentification());
    }

    public static OBTransactionCashBalance toOBTransactionCashBalance(FRTransactionData.FRTransactionCashBalance balance) {
        return balance == null ? null : new OBTransactionCashBalance()
                .amount(FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount(balance.getAmount()))
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toOBCreditDebitCode(balance.getCreditDebitIndicator()))
                .type(FRCashBalanceConverter.toOBBalanceType1Code(balance.getType()));
    }

    public static OBMerchantDetails1 toOBMerchantDetails1(FRTransactionData.FRMerchantDetails merchantDetails) {
        return merchantDetails == null ? null : new OBMerchantDetails1()
                .merchantName(merchantDetails.getMerchantName())
                .merchantCategoryCode(merchantDetails.getMerchantCategoryCode());
    }

    public static OBExternalCardSchemeType1Code toOBExternalCardSchemeType1Code(FRTransactionData.FRCardScheme cardSchemeName) {
        return cardSchemeName == null ? null : OBExternalCardSchemeType1Code.valueOf(cardSchemeName.name());
    }

    public static OBExternalCardAuthorisationType1Code toOBExternalCardAuthorisationType1Code(FRTransactionData.FRCardAuthorisationType authorisationType) {
        return authorisationType == null ? null : OBExternalCardAuthorisationType1Code.valueOf(authorisationType.name());
    }

    // OB to FR
    public static FRTransactionData toFRTransactionData(OBTransaction5 transaction) {
        return transaction == null ? null : FRTransactionData.builder()
                .accountId(transaction.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionReference(transaction.getTransactionReference())
                .statementReferences(transaction.getStatementReference())
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toFRCreditDebitIndicator(transaction.getCreditDebitIndicator()))
                .status(toFREntryStatus(transaction.getStatus()))
                .bookingDateTime(transaction.getBookingDateTime())
                .valueDateTime(transaction.getValueDateTime())
                .addressLine(transaction.getAddressLine())
                .amount(FRAmountConverter.toFRAmount(transaction.getAmount()))
                .chargeAmount(FRAmountConverter.toFRAmount(transaction.getChargeAmount()))
                .currencyExchange(FRCurrencyExchangeConverter.toFRCurrencyExchange(transaction.getCurrencyExchange()))
                .bankTransactionCode(toFRBankTransactionCodeStructure(transaction.getBankTransactionCode()))
                .proprietaryBankTransactionCode(toFRProprietaryBankTransactionCodeStructure(transaction.getProprietaryBankTransactionCode()))
                .cardInstrument(toFRTransactionCardInstrument(transaction.getCardInstrument()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toFRSupplementaryData(transaction.getSupplementaryData()))
                .transactionInformation(transaction.getTransactionInformation())
                .balance(toFRTransactionCashBalance(transaction.getBalance()))
                .merchantDetails(toFRMerchantDetails(transaction.getMerchantDetails()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(transaction.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(transaction.getCreditorAccount()))
                .debtorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(transaction.getDebtorAgent()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(transaction.getDebtorAccount()))
                .build();
    }

    public static FRTransactionData toFRTransactionData(OBTransaction6 transaction) {
        return transaction == null ? null : FRTransactionData.builder()
                .accountId(transaction.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionReference(transaction.getTransactionReference())
                .statementReferences(transaction.getStatementReference())
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toFRCreditDebitIndicator(transaction.getCreditDebitIndicator()))
                .status(toFREntryStatus(transaction.getStatus()))
                .transactionMutability(toFRTransactionMutability(transaction.getTransactionMutability()))
                .bookingDateTime(transaction.getBookingDateTime())
                .valueDateTime(transaction.getValueDateTime())
                .transactionInformation(transaction.getTransactionInformation())
                .addressLine(transaction.getAddressLine())
                .amount(FRAmountConverter.toFRAmount(transaction.getAmount()))
                .chargeAmount(FRAmountConverter.toFRAmount(transaction.getChargeAmount()))
                .currencyExchange(FRCurrencyExchangeConverter.toFRCurrencyExchange(transaction.getCurrencyExchange()))
                .bankTransactionCode(toFRBankTransactionCodeStructure(transaction.getBankTransactionCode()))
                .proprietaryBankTransactionCode(toFRProprietaryBankTransactionCodeStructure(transaction.getProprietaryBankTransactionCode()))
                .balance(toFRTransactionCashBalance(transaction.getBalance()))
                .merchantDetails(toFRMerchantDetails(transaction.getMerchantDetails()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(transaction.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(transaction.getCreditorAccount()))
                .debtorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(transaction.getDebtorAgent()))
                .debtorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(transaction.getDebtorAccount()))
                .cardInstrument(toFRTransactionCardInstrument(transaction.getCardInstrument()))
                .supplementaryData(FRAccountSupplementaryDataConverter.toFRSupplementaryData(transaction.getSupplementaryData()))
                .build();
    }

    public static FRTransactionData.FREntryStatus toFREntryStatus(OBEntryStatus1Code status) {
        return status == null ? null : FRTransactionData.FREntryStatus.valueOf(status.name());
    }

    public static FRTransactionData.FRTransactionMutability toFRTransactionMutability(OBTransactionMutability1Code transactionMutability) {
        return transactionMutability == null ? null : FRTransactionData.FRTransactionMutability.valueOf(transactionMutability.name());
    }

    public static FRTransactionData.FRBankTransactionCodeStructure toFRBankTransactionCodeStructure(OBBankTransactionCodeStructure1 transactionCode) {
        return transactionCode == null ? null : FRTransactionData.FRBankTransactionCodeStructure.builder()
                .code(transactionCode.getCode())
                .subCode(transactionCode.getSubCode())
                .build();
    }

    private static FRTransactionData.FRProprietaryBankTransactionCodeStructure toFRProprietaryBankTransactionCodeStructure(OBTransaction5ProprietaryBankTransactionCode proprietaryTransactionCode) {
        return proprietaryTransactionCode == null ? null : FRTransactionData.FRProprietaryBankTransactionCodeStructure.builder()
                .code(proprietaryTransactionCode.getCode())
                .issuer(proprietaryTransactionCode.getIssuer())
                .build();
    }

    public static FRTransactionData.FRProprietaryBankTransactionCodeStructure toFRProprietaryBankTransactionCodeStructure(ProprietaryBankTransactionCodeStructure1 proprietaryTransactionCode) {
        return proprietaryTransactionCode == null ? null : FRTransactionData.FRProprietaryBankTransactionCodeStructure.builder()
                .code(proprietaryTransactionCode.getCode())
                .issuer(proprietaryTransactionCode.getIssuer())
                .build();
    }

    public static FRTransactionData.FRTransactionCashBalance toFRTransactionCashBalance(OBTransactionCashBalance balance) {
        return balance == null ? null : FRTransactionData.FRTransactionCashBalance.builder()
                .amount(FRAmountConverter.toFRAmount(balance.getAmount()))
                .creditDebitIndicator(FRCreditDebitIndicatorConverter.toFRCreditDebitIndicator(balance.getCreditDebitIndicator()))
                .type(FRCashBalanceConverter.toFRBalanceType(balance.getType()))
                .build();
    }

    public static FRTransactionData.FRMerchantDetails toFRMerchantDetails(OBMerchantDetails1 merchantDetails) {
        return merchantDetails == null ? null : FRTransactionData.FRMerchantDetails.builder()
                .merchantName(merchantDetails.getMerchantName())
                .merchantCategoryCode(merchantDetails.getMerchantCategoryCode())
                .build();
    }


    public static FRTransactionData.FRTransactionCardInstrument toFRTransactionCardInstrument(OBTransactionCardInstrument1 cardInstrument) {
        return cardInstrument == null ? null : FRTransactionData.FRTransactionCardInstrument.builder()
                .cardSchemeName(toFRCardScheme(cardInstrument.getCardSchemeName()))
                .authorisationType(toFRCardAuthorisationType(cardInstrument.getAuthorisationType()))
                .name(cardInstrument.getName())
                .identification(cardInstrument.getIdentification())
                .build();
    }

    public static FRTransactionData.FRCardScheme toFRCardScheme(OBExternalCardSchemeType1Code cardSchemeName) {
        return cardSchemeName == null ? null : FRTransactionData.FRCardScheme.valueOf(cardSchemeName.name());
    }

    public static FRTransactionData.FRCardAuthorisationType toFRCardAuthorisationType(OBExternalCardAuthorisationType1Code authorisationType) {
        return authorisationType == null ? null : FRTransactionData.FRCardAuthorisationType.valueOf(authorisationType.name());
    }
}
