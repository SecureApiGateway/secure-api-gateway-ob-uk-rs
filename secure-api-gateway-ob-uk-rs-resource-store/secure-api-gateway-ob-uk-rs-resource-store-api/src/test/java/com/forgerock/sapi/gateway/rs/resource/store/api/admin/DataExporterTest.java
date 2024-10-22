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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRTransactionData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRAccountBeneficiaryConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRCashBalanceConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRDirectDebitConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FROfferConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRPartyConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRStatementConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRTransactionConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRScheduledPaymentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRStandingOrderConverter;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.account.FRAccountData;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBeneficiary;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRDirectDebit;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FROffer;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRParty;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRProduct;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRScheduledPayment;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStandingOrder;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStatement;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRTransaction;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances.FRBalanceRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.beneficiaries.FRBeneficiaryRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.directdebits.FRDirectDebitRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.offers.FROfferRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.party.FRPartyRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.products.FRProductRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.scheduledpayments.FRScheduledPaymentRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.standingorders.FRStandingOrderRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.statements.FRStatementRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.transactions.FRTransactionRepository;

import uk.org.openbanking.datamodel.v3.account.OBBalanceType1Code;
import uk.org.openbanking.datamodel.v3.account.OBBeneficiary5;
import uk.org.openbanking.datamodel.v3.account.OBBeneficiaryType1Code;
import uk.org.openbanking.datamodel.v3.account.OBCashAccount51;
import uk.org.openbanking.datamodel.v3.account.OBCreditDebitCode2;
import uk.org.openbanking.datamodel.v3.account.OBExternalPartyType1Code;
import uk.org.openbanking.datamodel.v3.account.OBExternalStatementType1Code;
import uk.org.openbanking.datamodel.v3.account.OBParty2;
import uk.org.openbanking.datamodel.v3.account.OBReadBalance1;
import uk.org.openbanking.datamodel.v3.account.OBReadBalance1Data;
import uk.org.openbanking.datamodel.v3.account.OBReadBalance1DataBalanceInner;
import uk.org.openbanking.datamodel.v3.account.OBReadBalance1DataBalanceInnerAmount;
import uk.org.openbanking.datamodel.v3.account.OBReadDirectDebit2DataDirectDebitInner;
import uk.org.openbanking.datamodel.v3.account.OBReadOffer1;
import uk.org.openbanking.datamodel.v3.account.OBReadOffer1Data;
import uk.org.openbanking.datamodel.v3.account.OBReadOffer1DataOfferInner;
import uk.org.openbanking.datamodel.v3.account.OBReadOffer1DataOfferInnerOfferType;
import uk.org.openbanking.datamodel.v3.account.OBReadProduct2DataProductInner;
import uk.org.openbanking.datamodel.v3.account.OBReadProduct2DataProductInnerProductType;
import uk.org.openbanking.datamodel.v3.account.OBScheduledPayment3;
import uk.org.openbanking.datamodel.v3.account.OBStandingOrder6;
import uk.org.openbanking.datamodel.v3.account.OBStatement2;
import uk.org.openbanking.datamodel.v3.account.OBTransaction6;
import uk.org.openbanking.datamodel.v3.account.OBTransactionCashBalance;
import uk.org.openbanking.datamodel.v3.account.OBTransactionCashBalanceAmount;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebClient(registerRestTemplate = true)
class DataExporterTest {

    @Autowired
    private DataExporter dataExporter;
    @Autowired
    private FRTransactionRepository transactionRepository;
    @Autowired
    private FRProductRepository productRepository;
    @Autowired
    private FROfferRepository offerRepository;
    @Autowired
    private FRPartyRepository partyRepository;
    @Autowired
    private FRScheduledPaymentRepository scheduledPaymentRepository;
    @Autowired
    private FRBeneficiaryRepository beneficiaryRepository;
    @Autowired
    private FRDirectDebitRepository directDebitRepository;
    @Autowired
    private FRStatementRepository statementRepository;
    @Autowired
    private FRStandingOrderRepository standingOrderRepository;
    @Autowired
    private FRBalanceRepository balanceRepository;

    private String accountId;

    @BeforeEach
    public void beforeEach() {
        // Fresh accountId (and test data) for each test
        accountId = UUID.randomUUID().toString();
    }

    @Test
    public void testExportingAccountWithNoData() {
        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getBalances()).isEmpty();
        assertThat(accountData.getTransactions()).isEmpty();
        assertThat(accountData.getParty()).isNull();
        assertThat(accountData.getStandingOrders()).isEmpty();
        assertThat(accountData.getBeneficiaries()).isEmpty();
        assertThat(accountData.getProduct()).isNull();
        assertThat(accountData.getStandingOrders()).isEmpty();
        assertThat(accountData.getDirectDebits()).isEmpty();
        assertThat(accountData.getOffers()).isEmpty();
        assertThat(accountData.getScheduledPayments()).isEmpty();
        assertThat(accountData.getStatements()).isEmpty();
    }

    private FRAccount createAccount() {
        return FRAccount.builder().id(accountId).account(FRFinancialAccount.builder().accountId(accountId).build()).build();
    }

    private FRAccountData exportAccountData() {
        final FRAccount account = createAccount();
        final FRAccountData accountData = dataExporter.exportAccountData(account);

        assertThat(accountData.getAccount().getAccountId()).isEqualTo(accountId);
        return accountData;
    }

    @Test
    public void testExportingAccountWithTransactionData() {
        final int numTransactions = 1234;
        final List<OBTransaction6> transactions = generateTransactions(numTransactions);

        final FRAccountData accountData = exportAccountData();
        assertThat(accountData.getTransactions()).hasSize(numTransactions);
        assertThat(accountData.getTransactions()).isEqualTo(transactions);
    }

    private List<OBTransaction6> generateTransactions(int numTransactions) {
        final List<OBTransaction6> transactions = new ArrayList<>(numTransactions);
        for (int i = 0; i < numTransactions; i++) {
            transactions.add(new OBTransaction6().accountId(accountId)
                                                 .balance(new OBTransactionCashBalance(OBCreditDebitCode2.CREDIT,
                                                         OBBalanceType1Code.CLOSINGCLEARED,
                                                         new OBTransactionCashBalanceAmount(i + ".00", "GBP")))
                                                 .transactionReference("Test Payment: " + i));
        }
        transactionRepository.saveAll(transactions.stream().map(obTransaction -> {
            final FRTransactionData frTransactionData = FRTransactionConverter.toFRTransactionData(obTransaction);
            return FRTransaction.builder().transaction(frTransactionData).accountId(accountId).build();
        }).toList());

        return transactions;
    }

    @Test
    public void testExportingAccountWithProductData() {
        final OBReadProduct2DataProductInner product = generateProductData();

        final FRAccountData accountData = exportAccountData();
        assertThat(accountData.getProduct()).isEqualTo(product);
    }

    private OBReadProduct2DataProductInner generateProductData() {
        final OBReadProduct2DataProductInner product = new OBReadProduct2DataProductInner().accountId(accountId)
                                                                                           .productId(UUID.randomUUID().toString())
                                                                                           .productType(OBReadProduct2DataProductInnerProductType.PERSONALCURRENTACCOUNT)
                                                                                           .productName("321 Product");

        productRepository.save(FRProduct.builder().accountId(accountId).product(product).build());
        return product;
    }

    @Test
    public void testExportingAccountWithOffers() {
        final List<OBReadOffer1> offers = generateOffers(7);

        final FRAccountData accountData = exportAccountData();
        validateOffers(accountData, offers);
    }

    private static void validateOffers(FRAccountData accountData, List<OBReadOffer1> offers) {
        assertThat(accountData.getOffers()).isEqualTo(offers.stream().flatMap(offer -> offer.getData().getOffer().stream()).toList());
    }

    private List<OBReadOffer1> generateOffers(int numOffers) {
        final List<OBReadOffer1> offers = new ArrayList<>(numOffers);
        for (int i = 0; i < numOffers; i++) {
            offers.add(new OBReadOffer1(new OBReadOffer1Data().offer(List.of(new OBReadOffer1DataOfferInner().accountId(accountId).offerType(OBReadOffer1DataOfferInnerOfferType.BALANCETRANSFER).description("Offer #" + i)))));
        }
        offerRepository.saveAll(offers.stream().map(offer -> {
            final FROffer frOffer = new FROffer();
            frOffer.setAccountId(accountId);
            frOffer.setOffer(FROfferConverter.toFROfferData(offer.getData().getOffer().get(0)));
            return frOffer;
        }).toList());
        return offers;
    }

    @Test
    public void testExportingAccountWithPartyData() {
        final OBParty2 party = generatePartyData();

        final FRAccountData accountData = exportAccountData();
        assertThat(accountData.getParty()).isEqualTo(party);
    }

    private OBParty2 generatePartyData() {
        final OBParty2 party = new OBParty2().partyType(OBExternalPartyType1Code.SOLE).accountRole("acc-role");
        partyRepository.save(FRParty.builder().accountId(accountId).party(FRPartyConverter.toFRPartyData(party)).build());
        return party;
    }

    @Test
    public void testExportingAccountWithScheduledPaymentsData() {
        final int numPayments = 99;
        final List<OBScheduledPayment3> scheduledPayments = generateScheduledPayments(numPayments);

        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getScheduledPayments()).isEqualTo(scheduledPayments);
    }

    // Corrupt standingOrders are ones where ths FRStandingOrder.id does not match the OB obj standingOrderId
    @Test
    public void testExportingAccountWithScheduledPaymentsDataFiltersOutCorruptData() {
        // Save some valid scheduled payments
        final int numPayments = 99;
        final List<OBScheduledPayment3> scheduledPayments = generateScheduledPayments(numPayments);

        // Add a corrupt scheduled payments
        scheduledPaymentRepository.save(
                FRScheduledPayment.builder()
                        .accountId(accountId)
                        .id(UUID.randomUUID().toString())
                        .scheduledPayment(
                                FRScheduledPaymentConverter.toFRScheduledPaymentData(
                                        new OBScheduledPayment3().accountId(accountId)
                                                .reference("Corrupt Scheduled Payment")
                                                .scheduledPaymentId(UUID.randomUUID().toString())
                                                .creditorAccount(new OBCashAccount51().schemeName("ABC")
                                                        .name("Test Acc")
                                                        .identification("acc-123"))))
                        .build());

        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getScheduledPayments()).isEqualTo(scheduledPayments);
    }

    private List<OBScheduledPayment3> generateScheduledPayments(int numPayments) {
        final List<OBScheduledPayment3> payments = new ArrayList<>(numPayments);
        for (int i = 0; i < numPayments; i++) {
            payments.add(new OBScheduledPayment3().accountId(accountId)
                                                  .reference("Scheduled Payment #" + i)
                                                  .scheduledPaymentId(UUID.randomUUID().toString())
                                                  .creditorAccount(new OBCashAccount51().schemeName("ABC")
                                                                                        .name("Test Acc")
                                                                                        .identification("acc" + i)));
        }
        scheduledPaymentRepository.saveAll(
                payments.stream()
                        .map(obScheduledPayment ->
                                FRScheduledPayment.builder().accountId(accountId)
                                                            .id(obScheduledPayment.getScheduledPaymentId())
                                                            .scheduledPayment(
                                                                    FRScheduledPaymentConverter.toFRScheduledPaymentData(obScheduledPayment))
                                                            .build())
                        .toList());
        return payments;
    }

    @Test
    public void testExportingAccountWithBeneficiaryData() {
        int numBeneficiaries = 2;
        final List<OBBeneficiary5> beneficiaries = generateBeneficiaries(numBeneficiaries);

        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getBeneficiaries()).isEqualTo(beneficiaries);
    }

    private List<OBBeneficiary5> generateBeneficiaries(int numBeneficiaries) {
        final List<OBBeneficiary5> beneficiaries = new ArrayList<>(numBeneficiaries);
        for (int i = 0; i < numBeneficiaries; i++) {
            beneficiaries.add(new OBBeneficiary5().accountId(accountId)
                                                  .reference("Beneficiary #" + i)
                                                  .beneficiaryType(OBBeneficiaryType1Code.ORDINARY));
        }
        beneficiaryRepository.saveAll(beneficiaries.stream().map(obBeneficiary ->
                        FRBeneficiary.builder().accountId(accountId)
                                .beneficiary(FRAccountBeneficiaryConverter.toFRAccountBeneficiary(obBeneficiary))
                                .build())
                        .toList());
        return beneficiaries;
    }

    @Test
    public void testExportAccountWithDirectDebitData() {
        int numDirectDebits = 4;
        final List<OBReadDirectDebit2DataDirectDebitInner> directDebits = generateDirectDebitData(numDirectDebits);

        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getDirectDebits()).isEqualTo(directDebits);
    }

    private List<OBReadDirectDebit2DataDirectDebitInner> generateDirectDebitData(int numDirectDebits) {
        final List<OBReadDirectDebit2DataDirectDebitInner> directDebits = new ArrayList<>(numDirectDebits);
        for (int i = 0; i < numDirectDebits; i++) {
            directDebits.add(new OBReadDirectDebit2DataDirectDebitInner().accountId(accountId).name("DirectDebit #" + i));
        }
        directDebitRepository.saveAll(directDebits.stream().map(obDirectDebit ->
                        FRDirectDebit.builder().accountId(accountId)
                                .directDebit(FRDirectDebitConverter.toFRDirectDebitData(obDirectDebit))
                                .build())
                .toList());
        return directDebits;
    }

    @Test
    public void testExportAccountWithStatementData() {
        int numStatements = 36;
        final List<OBStatement2> statements = generateStatements(numStatements);

        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getStatements()).isEqualTo(statements);
    }

    private List<OBStatement2> generateStatements(int numStatements) {
        final List<OBStatement2> statements = new ArrayList<>(numStatements);
        for (int i = 0; i < numStatements; i++) {
            statements.add(new OBStatement2().accountId(accountId).statementReference("Statement #" + i).type(OBExternalStatementType1Code.REGULARPERIODIC));
        }
        statementRepository.saveAll(statements.stream().map(obStatement ->
                        FRStatement.builder().accountId(accountId)
                                .statement(FRStatementConverter.toFRStatementData(obStatement))
                                .build())
                .toList());
        return statements;
    }

    @Test
    public void testExportAccountWithStandingOrders() {
        int numStandingOrders = 1;
        final List<OBStandingOrder6> standingOrders = generateStandingOrders(numStandingOrders);

        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getStandingOrders()).isEqualTo(standingOrders);
    }

    // Corrupt standingOrders are ones where ths FRStandingOrder.id does not match the OB obj standingOrderId
    @Test
    public void testExportAccountWithStandingOrdersFilterOutCorruptData() {
        // Generate valid standing orders
        int numStandingOrders = 5;
        final List<OBStandingOrder6> standingOrders = generateStandingOrders(numStandingOrders);

        // Add a corrupt standingOrder
        standingOrderRepository.save(FRStandingOrder.builder().id(UUID.randomUUID().toString())
                                                              .accountId(accountId)
                                                              .standingOrder(FRStandingOrderConverter.toFRStandingOrderData(
                                                                      new OBStandingOrder6().standingOrderId(UUID.randomUUID().toString())
                                                                              .accountId(accountId)
                                                                              .reference("Corrupt Standing Order")))
                                                              .build());

        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getStandingOrders()).isEqualTo(standingOrders);
    }

    private List<OBStandingOrder6> generateStandingOrders(int numStandingOrders) {
        final List<OBStandingOrder6> standingOrders = new ArrayList<>(numStandingOrders);
        for (int i = 0; i < numStandingOrders; i++) {
            standingOrders.add(new OBStandingOrder6().standingOrderId(UUID.randomUUID().toString()).accountId(accountId).reference("Standing Order #" + i));
        }
        standingOrderRepository.saveAll(
                standingOrders.stream()
                              .map(obStandingOrder ->
                                      FRStandingOrder.builder().id(obStandingOrder.getStandingOrderId())
                                                                                  .accountId(accountId)
                                                                                  .standingOrder(FRStandingOrderConverter.toFRStandingOrderData(obStandingOrder))
                                                                                  .build())
                              .toList());
        return standingOrders;
    }

    @Test
    public void testExportAccountWithBalances() {
        final int numBalances = 3;
        final List<OBReadBalance1> balances = generateBalances(numBalances);

        final FRAccountData accountData = exportAccountData();

        validateBalances(accountData, balances);
    }

    private static void validateBalances(FRAccountData accountData, List<OBReadBalance1> balances) {
        assertThat(accountData.getBalances()).isEqualTo(balances.stream().flatMap(balance -> balance.getData().getBalance().stream()).toList());
    }

    private List<OBReadBalance1> generateBalances(int numBalances) {
        final List<OBReadBalance1> balances = new ArrayList<>(numBalances);
        for (int i = 0; i < numBalances; i++) {
            balances.add(new OBReadBalance1().data(
                    new OBReadBalance1Data().balance(
                            List.of(new OBReadBalance1DataBalanceInner().accountId(accountId)
                                    .amount(new OBReadBalance1DataBalanceInnerAmount(i + ".01", "GBP"))))));
        }
        balanceRepository.saveAll(balances.stream().map(obBalance ->
                        FRBalance.builder().accountId(accountId)
                                .balance(FRCashBalanceConverter.toFRCashBalance(obBalance.getData().getBalance().get(0)))
                                .build())
                .toList());
        return balances;
    }

    @Test
    public void testExportingAccountWithAllData() {
        final List<OBTransaction6> transactions = generateTransactions(9341);
        final List<OBReadBalance1> balances = generateBalances(15);
        final List<OBReadOffer1> offers = generateOffers(1);
        final List<OBReadDirectDebit2DataDirectDebitInner> directDebits = generateDirectDebitData(128);
        final List<OBBeneficiary5> beneficiaries = generateBeneficiaries(9);
        final OBParty2 party = generatePartyData();
        final OBReadProduct2DataProductInner product = generateProductData();
        final List<OBScheduledPayment3> scheduledPayments = generateScheduledPayments(501);
        final List<OBStandingOrder6> standingOrders = generateStandingOrders(12);
        final List<OBStatement2> statements = generateStatements(128);

        final FRAccountData accountData = exportAccountData();

        assertThat(accountData.getTransactions()).isEqualTo(transactions);
        assertThat(accountData.getProduct()).isEqualTo(product);
        validateOffers(accountData, offers);
        assertThat(accountData.getParty()).isEqualTo(party);
        assertThat(accountData.getScheduledPayments()).isEqualTo(scheduledPayments);
        assertThat(accountData.getBeneficiaries()).isEqualTo(beneficiaries);
        assertThat(accountData.getDirectDebits()).isEqualTo(directDebits);
        assertThat(accountData.getStatements()).isEqualTo(statements);
        assertThat(accountData.getStandingOrders()).isEqualTo(standingOrders);
        validateBalances(accountData, balances);
    }

}