/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rs.resource.store.datamodel.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.org.openbanking.datamodel.account.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FRAccountData {

    private OBAccount6 account;
    private List<OBReadBalance1DataBalanceInner> balances = new ArrayList<>();
    private OBReadProduct2DataProductInner product;
    private OBParty2 party;
    private List<OBBeneficiary5> beneficiaries = new ArrayList<>();
    private List<OBReadDirectDebit2DataDirectDebitInner> directDebits = new ArrayList<>();
    private List<OBStandingOrder6> standingOrders = new ArrayList<>();
    private List<OBTransaction6> transactions = new ArrayList<>();
    private List<OBStatement2> statements = new ArrayList<>();
    private List<OBScheduledPayment3> scheduledPayments = new ArrayList<>();
    private List<OBReadOffer1DataOfferInner> offers = new ArrayList<>();

    public FRAccountData addBalance(OBReadBalance1DataBalanceInner balance) {
        balances.add(balance);
        return this;
    }

    public FRAccountData addBeneficiary(OBBeneficiary5 beneficiary) {
        beneficiaries.add(beneficiary);
        return this;
    }

    public FRAccountData addDirectDebit(OBReadDirectDebit2DataDirectDebitInner directDebit1) {
        directDebits.add(directDebit1);
        return this;
    }

    public FRAccountData addStandingOrder(OBStandingOrder6 standingOrder) {
        standingOrders.add(standingOrder);
        return this;
    }

    public FRAccountData addTransaction(OBTransaction6 transaction) {
        transactions.add(transaction);
        return this;
    }

    public FRAccountData addStatement(OBStatement2 statement1) {
        statements.add(statement1);
        return this;
    }

    public FRAccountData addScheduledPayment(OBScheduledPayment3 scheduledPayment1) {
        scheduledPayments.add(scheduledPayment1);
        return this;
    }

    public FRAccountData addOffer(OBReadOffer1DataOfferInner offer1) {
        offers.add(offer1);
        return this;
    }
}
