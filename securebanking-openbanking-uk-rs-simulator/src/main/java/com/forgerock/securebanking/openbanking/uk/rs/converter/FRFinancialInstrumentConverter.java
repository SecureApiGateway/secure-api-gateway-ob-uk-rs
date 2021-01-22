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
package com.forgerock.securebanking.openbanking.uk.rs.converter;

import com.forgerock.securebanking.openbanking.uk.rs.converter.account.FRAccountServicerConverter;
import com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRPaymentPostalAddressConverter;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRFinancialAgent;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRFinancialCreditor;
import uk.org.openbanking.datamodel.account.*;
import uk.org.openbanking.datamodel.payment.OBBranchAndFinancialInstitutionIdentification3;
import uk.org.openbanking.datamodel.payment.OBBranchAndFinancialInstitutionIdentification6;
import uk.org.openbanking.datamodel.payment.*;

public class FRFinancialInstrumentConverter {

    // OB to FR
    public static FRFinancialCreditor toFRFinancialCreditor(OBPartyIdentification43 creditor) {
        return creditor == null ? null : FRFinancialCreditor.builder()
                .name(creditor.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(creditor.getPostalAddress()))
                .build();
    }

    public static FRFinancialCreditor toFRFinancialCreditor(OBWriteInternational3DataInitiationCreditor creditor) {
        return creditor == null ? null : FRFinancialCreditor.builder()
                .name(creditor.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(creditor.getPostalAddress()))
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBBranchAndFinancialInstitutionIdentification3 agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(agent.getPostalAddress()))
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBBranchAndFinancialInstitutionIdentification5 agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBBranchAndFinancialInstitutionIdentification6 agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(agent.getPostalAddress()))
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(uk.org.openbanking.datamodel.account.OBBranchAndFinancialInstitutionIdentification6 agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(agent.getPostalAddress()))
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBBranchAndFinancialInstitutionIdentification51 agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBBranchAndFinancialInstitutionIdentification60 agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(agent.getPostalAddress()))
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBBranchAndFinancialInstitutionIdentification61 agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(agent.getPostalAddress()))
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBBranchAndFinancialInstitutionIdentification62 agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(agent.getPostalAddress()))
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBWriteInternational3DataInitiationCreditorAgent agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(agent.getPostalAddress()))
                .build();
    }

    public static FRFinancialAgent toFRFinancialAgent(OBWriteInternationalStandingOrder4DataInitiationCreditorAgent agent) {
        return agent == null ? null : FRFinancialAgent.builder()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toFRPostalAddress(agent.getPostalAddress()))
                .build();
    }

    // FR to OB
    public static OBWriteInternational3DataInitiationCreditor toOBWriteInternational3DataInitiationCreditor(FRFinancialCreditor creditor) {
        return creditor == null ? null : new OBWriteInternational3DataInitiationCreditor()
                .name(creditor.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(creditor.getPostalAddress()));
    }

    public static OBWriteInternational3DataInitiationCreditorAgent toOBWriteInternational3DataInitiationCreditorAgent(FRFinancialAgent agent) {
        return agent == null ? null : new OBWriteInternational3DataInitiationCreditorAgent()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(agent.getPostalAddress()));
    }

    public static OBWriteInternationalStandingOrder4DataInitiationCreditorAgent toOBWriteInternationalStandingOrder4DataInitiationCreditorAgent(FRFinancialAgent agent) {
        return agent == null ? null : new OBWriteInternationalStandingOrder4DataInitiationCreditorAgent()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(agent.getPostalAddress()));
    }

    public static OBBranchAndFinancialInstitutionIdentification2 toOBBranchAndFinancialInstitutionIdentification2(FRFinancialAgent agent) {
        return agent == null ? null : new OBBranchAndFinancialInstitutionIdentification2()
                .schemeName(FRAccountServicerConverter.toOBExternalFinancialInstitutionIdentification2Code(agent.getSchemeName()))
                .identification(agent.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification3 toOBBranchAndFinancialInstitutionIdentification3(FRFinancialAgent agent) {
        return agent == null ? null : new OBBranchAndFinancialInstitutionIdentification3()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(agent.getPostalAddress()));
    }

    public static OBBranchAndFinancialInstitutionIdentification4 toOBBranchAndFinancialInstitutionIdentification4(FRFinancialAgent agent) {
        return agent == null ? null : new OBBranchAndFinancialInstitutionIdentification4()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification5 toOBBranchAndFinancialInstitutionIdentification5(FRFinancialAgent agent) {
        return agent == null ? null : new OBBranchAndFinancialInstitutionIdentification5()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification51 toOBBranchAndFinancialInstitutionIdentification51(FRFinancialAgent agent) {
        return agent == null ? null : new OBBranchAndFinancialInstitutionIdentification51()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification());
    }

    public static OBBranchAndFinancialInstitutionIdentification6 toOBBranchAndFinancialInstitutionIdentification6(FRFinancialAgent agent) {
        return agent == null ? null : new OBBranchAndFinancialInstitutionIdentification6()
                .schemeName(agent.getSchemeName())
                .identification(agent.getIdentification())
                .name(agent.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(agent.getPostalAddress()));
    }

    public static OBPartyIdentification43 toOBPartyIdentification43(FRFinancialCreditor creditor) {
        return creditor == null ? null : new OBPartyIdentification43()
                .name(creditor.getName())
                .postalAddress(FRPaymentPostalAddressConverter.toOBPostalAddress6(creditor.getPostalAddress()));
    }

}
