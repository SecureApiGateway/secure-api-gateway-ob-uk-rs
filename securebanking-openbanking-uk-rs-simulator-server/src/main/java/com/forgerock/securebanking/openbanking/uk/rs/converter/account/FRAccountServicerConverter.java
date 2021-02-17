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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRAccountIdentifier;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRFinancialAgent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRAccountServicer;
import uk.org.openbanking.datamodel.account.*;

public class FRAccountServicerConverter {

    // FR to OB
    public static OBBranchAndFinancialInstitutionIdentification2 toOBBranchAndFinancialInstitutionIdentification2(FRAccountServicer creditorAgent) {
        return creditorAgent == null ? null : new OBBranchAndFinancialInstitutionIdentification2()
                .schemeName(toOBExternalFinancialInstitutionIdentification2Code(creditorAgent.getSchemeName()))
                .identification(creditorAgent.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification2 toOBBranchAndFinancialInstitutionIdentification2(FRAccountIdentifier creditorAccount) {
        return creditorAccount == null ? null : new OBBranchAndFinancialInstitutionIdentification2()
                .schemeName(toOBExternalFinancialInstitutionIdentification2Code(creditorAccount.getSchemeName()))
                .identification(creditorAccount.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification3 toOBBranchAndFinancialInstitutionIdentification3(FRFinancialAgent creditorAgent) {
        return creditorAgent == null ? null : new OBBranchAndFinancialInstitutionIdentification3()
                .schemeName(creditorAgent.getSchemeName())
                .identification(creditorAgent.getIdentification())
                .name(creditorAgent.getName())
                .postalAddress(FRAccountPostalAddressConverter.toOBPostalAddress6(creditorAgent.getPostalAddress()));
    }

    public static OBBranchAndFinancialInstitutionIdentification4 toOBBranchAndFinancialInstitutionIdentification4(FRAccountServicer servicer) {
        return servicer == null ? null : new OBBranchAndFinancialInstitutionIdentification4()
                .schemeName(servicer.getSchemeName())
                .identification(servicer.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification5 toOBBranchAndFinancialInstitutionIdentification5(FRAccountServicer servicer) {
        return servicer == null ? null : new OBBranchAndFinancialInstitutionIdentification5()
                .schemeName(servicer.getSchemeName())
                .identification(servicer.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification50 toOBBranchAndFinancialInstitutionIdentification50(FRAccountServicer servicer) {
        return servicer == null ? null : new OBBranchAndFinancialInstitutionIdentification50()
                .schemeName(servicer.getSchemeName())
                .identification(servicer.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification6 toOBBranchAndFinancialInstitutionIdentification6(FRFinancialAgent creditorAgent) {
        return creditorAgent == null ? null : new OBBranchAndFinancialInstitutionIdentification6()
                .schemeName(creditorAgent.getSchemeName())
                .identification(creditorAgent.getIdentification())
                .name(creditorAgent.getName())
                .postalAddress(FRAccountPostalAddressConverter.toOBPostalAddress6(creditorAgent.getPostalAddress()));
    }

    public static OBBranchAndFinancialInstitutionIdentification60 toOBBranchAndFinancialInstitutionIdentification60(FRFinancialAgent creditorAgent) {
        return creditorAgent == null ? null : new OBBranchAndFinancialInstitutionIdentification60()
                .schemeName(creditorAgent.getSchemeName())
                .identification(creditorAgent.getIdentification())
                .name(creditorAgent.getName())
                .postalAddress(FRAccountPostalAddressConverter.toOBPostalAddress6(creditorAgent.getPostalAddress()));
    }

    public static OBBranchAndFinancialInstitutionIdentification61 toOBBranchAndFinancialInstitutionIdentification61(FRFinancialAgent creditorAgent) {
        return creditorAgent == null ? null : new OBBranchAndFinancialInstitutionIdentification61()
                .schemeName(creditorAgent.getSchemeName())
                .identification(creditorAgent.getIdentification())
                .name(creditorAgent.getName())
                .postalAddress(FRAccountPostalAddressConverter.toOBPostalAddress6(creditorAgent.getPostalAddress()));
    }

    public static OBBranchAndFinancialInstitutionIdentification62 toOBBranchAndFinancialInstitutionIdentification62(FRFinancialAgent creditorAgent) {
        return creditorAgent == null ? null : new OBBranchAndFinancialInstitutionIdentification62()
                .schemeName(creditorAgent.getSchemeName())
                .identification(creditorAgent.getIdentification())
                .name(creditorAgent.getName())
                .postalAddress(FRAccountPostalAddressConverter.toOBPostalAddress6(creditorAgent.getPostalAddress()));
    }

    public static OBExternalFinancialInstitutionIdentification2Code toOBExternalFinancialInstitutionIdentification2Code(String schemeName) {
        return schemeName == null ? null : OBExternalFinancialInstitutionIdentification2Code.valueOf(schemeName);
    }

    // OB to FR
    public static FRAccountServicer toFRAccountServicer(OBBranchAndFinancialInstitutionIdentification5 servicer) {
        return servicer == null ? null : FRAccountServicer.builder()
                .schemeName(servicer.getSchemeName())
                .identification(servicer.getIdentification())
                .build();
    }

    public static FRAccountServicer toFRAccountServicer(OBBranchAndFinancialInstitutionIdentification50 servicer) {
        return servicer == null ? null : FRAccountServicer.builder()
                .schemeName(servicer.getSchemeName())
                .identification(servicer.getIdentification())
                .build();
    }
}
