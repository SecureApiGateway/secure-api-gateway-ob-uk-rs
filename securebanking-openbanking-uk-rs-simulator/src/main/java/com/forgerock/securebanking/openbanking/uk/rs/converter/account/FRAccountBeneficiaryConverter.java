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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountBeneficiary;
import com.forgerock.securebanking.openbanking.uk.rs.converter.FRAccountIdentifierConverter;
import com.forgerock.securebanking.openbanking.uk.rs.converter.FRFinancialInstrumentConverter;
import uk.org.openbanking.datamodel.account.*;

public class FRAccountBeneficiaryConverter {

    // FR to OB
    public static OBBeneficiary1 toOBBeneficiary1(FRAccountBeneficiary beneficiary) {
        return beneficiary == null ? null : new OBBeneficiary1()
                .accountId(beneficiary.getAccountId())
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .reference(beneficiary.getReference())
                .servicer(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification2(beneficiary.getCreditorAccount()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount1(beneficiary.getCreditorAccount()));
    }

    public static OBBeneficiary2 toOBBeneficiary2(FRAccountBeneficiary beneficiary) {
        return beneficiary == null ? null : new OBBeneficiary2()
                .accountId(beneficiary.getAccountId())
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .reference(beneficiary.getReference())
                .creditorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification3(beneficiary.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount3(beneficiary.getCreditorAccount()));
    }

    public static OBBeneficiary3 toOBBeneficiary3(FRAccountBeneficiary beneficiary) {
        return beneficiary == null ? null : new OBBeneficiary3()
                .accountId(beneficiary.getAccountId())
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .reference(beneficiary.getReference())
                .creditorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification6(beneficiary.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount5(beneficiary.getCreditorAccount()));
    }

    public static OBBeneficiary4 toOBBeneficiary4(FRAccountBeneficiary beneficiary) {
        return beneficiary == null ? null : new OBBeneficiary4()
                .accountId(beneficiary.getAccountId())
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .reference(beneficiary.getReference())
                .supplementaryData(FRAccountSupplementaryDataConverter.toOBSupplementaryData1(beneficiary.getSupplementaryData()))
                .creditorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification60(beneficiary.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount50(beneficiary.getCreditorAccount()));
    }

    public static OBBeneficiary5 toOBBeneficiary5(FRAccountBeneficiary beneficiary) {
        return beneficiary == null ? null : new OBBeneficiary5()
                .accountId(beneficiary.getAccountId())
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .beneficiaryType(toOBBeneficiaryType1Code(beneficiary.getBeneficiaryType()))
                .reference(beneficiary.getReference())
                .supplementaryData(FRAccountSupplementaryDataConverter.toOBSupplementaryData1(beneficiary.getSupplementaryData()))
                .creditorAgent(FRAccountServicerConverter.toOBBranchAndFinancialInstitutionIdentification60(beneficiary.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toOBCashAccount50(beneficiary.getCreditorAccount()));
    }

    public static OBBeneficiaryType1Code toOBBeneficiaryType1Code(FRAccountBeneficiary.FRBeneficiaryType beneficiaryType) {
        return beneficiaryType == null ? null : OBBeneficiaryType1Code.valueOf(beneficiaryType.name());
    }

    // OB to FR
    public static FRAccountBeneficiary toFRAccountBeneficiary(OBBeneficiary3 beneficiary) {
        return beneficiary == null ? null : FRAccountBeneficiary.builder()
                .accountId(beneficiary.getAccountId())
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .reference(beneficiary.getReference())
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(beneficiary.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(beneficiary.getCreditorAccount()))
                .build();
    }

    public static FRAccountBeneficiary toFRAccountBeneficiary(OBBeneficiary5 beneficiary) {
        return beneficiary == null ? null : FRAccountBeneficiary.builder()
                .accountId(beneficiary.getAccountId())
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .beneficiaryType(toFRBeneficiaryType(beneficiary.getBeneficiaryType()))
                .reference(beneficiary.getReference())
                .supplementaryData(FRAccountSupplementaryDataConverter.toFRSupplementaryData(beneficiary.getSupplementaryData()))
                .creditorAgent(FRFinancialInstrumentConverter.toFRFinancialAgent(beneficiary.getCreditorAgent()))
                .creditorAccount(FRAccountIdentifierConverter.toFRAccountIdentifier(beneficiary.getCreditorAccount()))
                .build();
    }

    public static FRAccountBeneficiary.FRBeneficiaryType toFRBeneficiaryType(OBBeneficiaryType1Code beneficiaryType) {
        return beneficiaryType == null ? null : FRAccountBeneficiary.FRBeneficiaryType.valueOf(beneficiaryType.name());
    }

}
