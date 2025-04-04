/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin.v3;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRBalanceType;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRPartyData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRPartyConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.rs.resource.store.api.testsupport.FRCustomerInfoTestHelper;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.account.v3.FRAccountData;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.user.v3.FRUserData;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.*;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.customerinfo.FRCustomerInfoConverter;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
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
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.customerinfo.FRCustomerInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Iterables;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.org.openbanking.datamodel.v3.account.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRAccountBeneficiaryConverter.toFRAccountBeneficiary;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRCashBalanceConverter.toFRCashBalance;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRDirectDebitConverter.toFRDirectDebitData;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FROfferConverter.toFROfferData;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRStatementConverter.toFRStatementData;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRTransactionConverter.toFRTransactionData;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRScheduledPaymentConverter.toFRScheduledPaymentData;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRStandingOrderConverter.toFRStandingOrderData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link DataUpdater}.
 */
@ExtendWith(MockitoExtension.class)
public class DataUpdaterTest {

    private DataUpdater dataUpdater;
    @Mock
    private FRAccountRepository accountsRepository;
    @Mock
    private FRBalanceRepository balanceRepository;
    @Mock
    private FRBeneficiaryRepository beneficiaryRepository;
    @Mock
    private FRDirectDebitRepository directDebitRepository;
    @Mock
    private FRProductRepository productRepository;
    @Mock
    private FRStandingOrderRepository standingOrderRepository;
    @Mock
    private FRTransactionRepository transactionRepository;
    @Mock
    private FRStatementRepository statementRepository;
    @Mock
    private FRScheduledPaymentRepository scheduledPaymentRepository;
    @Mock
    private FRPartyRepository partyRepository;
    @Mock
    private FROfferRepository offerRepository;
    @Mock
    private FRCustomerInfoRepository customerInfoRepository;

    @BeforeEach
    public void setUp() {
        dataUpdater = new DataUpdater(accountsRepository, balanceRepository, beneficiaryRepository,
                directDebitRepository, productRepository, standingOrderRepository, transactionRepository,
                statementRepository, scheduledPaymentRepository, partyRepository, offerRepository,
                customerInfoRepository, 1000);
    }

    @Test
    public void updateBalancesShouldThrowExceptionForExceedingLimit() {
        // Given
        String accountId = "1";
        FRAccountData accountData = new FRAccountData().addBalance(new OBReadBalance1DataBalanceInner().accountId(accountId).type(OBBalanceType1Code.INTERIMAVAILABLE));
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(balanceRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);

        assertThatThrownBy(
                // When
                () -> dataUpdater.updateBalances(accountData, Collections.singleton("1"))
        )
                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @Test
    public void updateBeneficiariesShouldThrowExceptionForExceedingLimit() {
        // Given
        String accountId = "1";
        FRAccountData accountData = new FRAccountData().addBeneficiary(new OBBeneficiary5().accountId(accountId));
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(beneficiaryRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);

        assertThatThrownBy(
                // When
                () -> dataUpdater.updateBeneficiaries(accountData, Collections.singleton("1"))
        )
                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @Test
    public void updateDirectDebitsShouldThrowExceptionForExceedingLimit() {
        // Given
        String accountId = "1";
        FRAccountData accountData = new FRAccountData().addDirectDebit(new OBReadDirectDebit2DataDirectDebitInner().accountId(accountId));
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(directDebitRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);

        assertThatThrownBy(
                // When
                () -> dataUpdater.updateDirectDebits(accountData, Collections.singleton("1"))
        )
                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @Test
    public void updateStandingOrdersShouldThrowExceptionForExceedingLimit() {
        // Given
        String accountId = "1";
        FRAccountData accountData = new FRAccountData().addStandingOrder(new OBStandingOrder6().accountId(accountId));
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(standingOrderRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);

        assertThatThrownBy(
                // When
                () -> dataUpdater.updateStandingOrders(accountData, Collections.singleton("1"))
        )
                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @Test
    public void updateTransactionsShouldThrowExceptionForExceedingLimit() {
        // Given
        String accountId = "1";
        FRAccountData accountData = new FRAccountData().addTransaction(new OBTransaction6().accountId(accountId));
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(transactionRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);

        assertThatThrownBy(
                // When
                () -> dataUpdater.updateTransactions(accountData, Collections.singleton("1"))
        )
                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @Test
    public void updateStatementsShouldThrowExceptionForExceedingLimit() {
        // Given
        String accountId = "1";
        FRAccountData accountData = new FRAccountData().addStatement(new OBStatement2().accountId(accountId));
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(statementRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);

        assertThatThrownBy(
                // When
                () -> dataUpdater.updateStatements(accountData, Collections.singleton("1"))
        )
                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @Test
    public void updateScheduledPaymentsShouldThrowExceptionForExceedingLimit() {
        // Given
        String accountId = "1";
        FRAccountData accountData = new FRAccountData().addScheduledPayment(new OBScheduledPayment3().accountId(accountId));
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(scheduledPaymentRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);

        assertThatThrownBy(
                // When
                () -> dataUpdater.updateScheduledPayments(accountData, Collections.singleton("1"))
        )
                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @Test
    public void updateOffersShouldThrowExceptionForExceedingLimit() {
        // Given
        String accountId = "1";
        FRAccountData accountData = new FRAccountData().addOffer(new OBReadOffer1DataOfferInner().accountId(accountId));
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(offerRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);

        assertThatThrownBy(
                // When
                () -> dataUpdater.updateOffers(accountData, Collections.singleton("1"))
        )
                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @Test
    public void updateBalancesShouldAllowUpdatesWhenOnLimit() {
        // Given
        String accountId = "1";
        OBReadBalance1DataBalanceInner cashBalance = new OBReadBalance1DataBalanceInner().accountId(accountId).type(OBBalanceType1Code.INTERIMAVAILABLE);
        FRAccountData accountData = new FRAccountData().addBalance(cashBalance);
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(balanceRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);
        FRBalance existingBalance = FRBalance.builder().balance(toFRCashBalance(cashBalance)).build();
        given(balanceRepository.findByAccountIdAndBalanceType(accountId, FRBalanceType.INTERIMAVAILABLE)).willReturn(Optional.of(existingBalance));

        // When
        dataUpdater.updateBalances(accountData, Collections.singleton("1"));

        // Then
        verify(balanceRepository).saveAll(Collections.singletonList(existingBalance));
    }

    @Test
    public void updateBeneficiariesShouldAllowUpdatesWhenOnLimit() {
        // Given
        String accountId = "1";
        OBBeneficiary5 beneficiary = new OBBeneficiary5().beneficiaryId("2").accountId(accountId);
        FRAccountData accountData = new FRAccountData().addBeneficiary(beneficiary);
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(beneficiaryRepository.countByAccountIdIn(Collections.singleton("1"))).willReturn(1000L);
        FRBeneficiary existingBeneficiary = FRBeneficiary.builder()
                .beneficiary(toFRAccountBeneficiary(beneficiary))
                .accountId(accountId)
                .build();
        given(beneficiaryRepository.findById(beneficiary.getBeneficiaryId())).willReturn(Optional.of(existingBeneficiary));

        // When
        dataUpdater.updateBeneficiaries(accountData, Collections.singleton("1"));

        // Then
        verify(beneficiaryRepository).saveAll(Collections.singletonList(existingBeneficiary));
    }

    @Test
    public void updateDirectDebitsShouldAllowUpdatesWhenOnLimit() {
        // Given
        String accountId = "1";
        OBReadDirectDebit2DataDirectDebitInner directDebit = new OBReadDirectDebit2DataDirectDebitInner().accountId(accountId).directDebitId("2");
        FRAccountData accountData = new FRAccountData().addDirectDebit(directDebit);
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(directDebitRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);
        FRDirectDebit existingDirectDebit = FRDirectDebit.builder()
                .directDebit(toFRDirectDebitData(directDebit))
                .accountId(accountId)
                .build();
        given(directDebitRepository.findById(directDebit.getDirectDebitId())).willReturn(Optional.of(existingDirectDebit));

        // When
        dataUpdater.updateDirectDebits(accountData, Collections.singleton("1"));

        // Then
        verify(directDebitRepository).saveAll(Collections.singletonList(existingDirectDebit));
    }

    @Test
    public void updateStandingOrdersShouldAllowUpdatesWhenOnLimit() {
        // Given
        String accountId = "1";
        OBStandingOrder6 standingOrder = new OBStandingOrder6().accountId(accountId).standingOrderId("2");
        FRAccountData accountData = new FRAccountData().addStandingOrder(standingOrder);
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(standingOrderRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);
        FRStandingOrder existingStandingOrder = FRStandingOrder.builder()
                .standingOrder(toFRStandingOrderData(standingOrder))
                .accountId(accountId)
                .build();
        given(standingOrderRepository.findById(standingOrder.getStandingOrderId())).willReturn(Optional.of(existingStandingOrder));

        // When
        dataUpdater.updateStandingOrders(accountData, Collections.singleton("1"));

        // Then
        verify(standingOrderRepository).saveAll(Collections.singletonList(existingStandingOrder));
    }

    @Test
    public void updateTransactionsShouldAllowUpdatesWhenOnLimit() {
        // Given
        String accountId = "1";
        OBTransaction6 transaction = new OBTransaction6().transactionId("2").accountId(accountId);
        FRAccountData accountData = new FRAccountData().addTransaction(transaction);
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(transactionRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);
        FRTransaction existingTransaction = FRTransaction.builder()
                .transaction(toFRTransactionData(transaction))
                .accountId(accountId)
                .build();
        given(transactionRepository.findById(transaction.getTransactionId())).willReturn(Optional.of(existingTransaction));

        // When
        dataUpdater.updateTransactions(accountData, Collections.singleton("1"));

        // Then
        verify(transactionRepository).saveAll(Collections.singletonList(existingTransaction));
    }

    @Test
    public void updateStatementsShouldAllowUpdatesWhenOnLimit() {
        // Given
        String accountId = "1";
        OBStatement2 statement = new OBStatement2().accountId(accountId).statementId("2");
        FRAccountData accountData = new FRAccountData().addStatement(statement);
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(statementRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);
        FRStatement existingStatement = FRStatement.builder()
                .statement(toFRStatementData(statement))
                .accountId(accountId)
                .build();
        given(statementRepository.findById(statement.getStatementId())).willReturn(Optional.of(existingStatement));

        // When
        dataUpdater.updateStatements(accountData, Collections.singleton("1"));

        // Then
        verify(statementRepository).saveAll(Collections.singletonList(existingStatement));
    }

    @Test
    public void updateScheduledPaymentsShouldAllowUpdatesWhenOnLimit() {
        // Given
        String accountId = "1";
        OBScheduledPayment3 scheduledPayment = new OBScheduledPayment3().accountId(accountId).scheduledPaymentId("2");
        FRAccountData accountData = new FRAccountData().addScheduledPayment(scheduledPayment);
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(scheduledPaymentRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);
        FRScheduledPayment existingScheduledPayment = FRScheduledPayment.builder()
                .scheduledPayment(toFRScheduledPaymentData(scheduledPayment))
                .accountId(accountId)
                .build();
        given(scheduledPaymentRepository.findById(scheduledPayment.getScheduledPaymentId())).willReturn(Optional.of(existingScheduledPayment));

        // When
        dataUpdater.updateScheduledPayments(accountData, Collections.singleton("1"));

        // Then
        verify(scheduledPaymentRepository).saveAll(Collections.singletonList(existingScheduledPayment));
    }

    @Test
    public void updateOffersShouldAllowUpdatesWhenOnLimit() {
        // Given
        String accountId = "1";
        OBReadOffer1DataOfferInner offer = new OBReadOffer1DataOfferInner().accountId(accountId).offerId("2");
        FRAccountData accountData = new FRAccountData().addOffer(offer);
        accountData.setAccount(new OBAccount6().accountId(accountId));
        given(offerRepository.countByAccountIdIn(Collections.singleton(accountId))).willReturn(1000L);
        FROffer existingOffer = FROffer.builder()
                .offer(toFROfferData(offer))
                .accountId(accountId)
                .build();
        given(offerRepository.findById(offer.getOfferId())).willReturn(Optional.of(existingOffer));

        // When
        dataUpdater.updateOffers(accountData, Collections.singleton("1"));

        // Then
        verify(offerRepository).saveAll(Collections.singletonList(existingOffer));
    }

    @Test
    public void updateBalance_noExistingBalances_acceptAndCreate() {
        // Given
        given(balanceRepository.findByAccountIdAndBalanceType(any(), any())).willReturn(Optional.empty());
        OBReadBalance1DataBalanceInner interimAvailBalance = new OBReadBalance1DataBalanceInner()
                .accountId("1")
                .type(OBBalanceType1Code.INTERIMAVAILABLE);

        // When
        dataUpdater.updateBalances(accountDataWithBalance(interimAvailBalance), Collections.emptySet());

        // Then
        verify(balanceRepository).saveAll(argThat((b) -> Iterables.firstOf(b).getAccountId().equals("1")));
    }

    @Test
    public void updateBalance_balanceOfSameType_reject() {
        // Given
        OBReadBalance1DataBalanceInner interimAvailBalance = new OBReadBalance1DataBalanceInner()
                .accountId("1")
                .type(OBBalanceType1Code.INTERIMAVAILABLE);
        FRBalance frBalance = FRBalance.builder()
                .balance(toFRCashBalance(interimAvailBalance))
                .accountId(interimAvailBalance.getAccountId())
                .build();
        given(balanceRepository.findByAccountIdAndBalanceType(any(), any())).willReturn(Optional.of(frBalance));
        FRAccountData accountDataDiff = accountDataWithBalance(interimAvailBalance);
        accountDataDiff.setBalances(Arrays.asList(interimAvailBalance, interimAvailBalance));

        // When
        assertThatThrownBy(() -> {
            dataUpdater.updateBalances(accountDataDiff, Collections.emptySet());
        })

                // Then
                .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updateBalance_existingBalanceOfDiffType_acceptAndCreate() {
        // Given
        given(balanceRepository.findByAccountIdAndBalanceType(eq("1"), eq(FRBalanceType.OPENINGBOOKED))).willReturn(Optional.empty());
        OBReadBalance1DataBalanceInner openingBookedBalance = new OBReadBalance1DataBalanceInner()
                .accountId("1")
                .type(OBBalanceType1Code.OPENINGBOOKED);
        // When
        dataUpdater.updateBalances(accountDataWithBalance(openingBookedBalance), Collections.emptySet());

        // Then
        verify(balanceRepository).saveAll(argThat((b) -> Iterables.firstOf(b).getAccountId().equals("1")));
    }

    @Test
    public void updateCustomerInfo() {
        // Given
        FRCustomerInfo customerInfo = FRCustomerInfoTestHelper.aValidFRCustomerInfo();
        FRUserData userData = new FRUserData();
        userData.setUserId(customerInfo.getUserID());
        userData.setCustomerInfo(customerInfo);
        given(customerInfoRepository.findByUserID(
                eq(userData.getUserId()))).willReturn(FRCustomerInfoConverter.dtoToEntity(customerInfo)
        );
        // When
        dataUpdater.updateCustomerInfo(userData.getCustomerInfo(), userData.getCustomerInfo().getUserID());

        // Then
        verify(customerInfoRepository).findByUserID(userData.getUserId());
    }

    @Test
    public void updatePartyInfo() {
        final String partyId = UUID.randomUUID().toString();
        final OBParty2 obParty = new OBParty2().partyId(partyId).partyType(OBExternalPartyType1Code.SOLE).name("John Smith");

        final FRUserData userData = new FRUserData();
        final String userId = UUID.randomUUID().toString();
        userData.setUserId(userId);
        userData.setUserName("test-user");

        final FRParty existingParty = new FRParty();
        existingParty.setUserId(userId);
        existingParty.setId(partyId);
        final FRPartyData existingPartyData = new FRPartyData();
        existingPartyData.setPartyId(partyId);
        existingParty.setParty(existingPartyData);
        doReturn(existingParty).when(partyRepository).findByUserId(eq(userId));

        userData.setParty(obParty);
        dataUpdater.updateParty(userData);

        final ArgumentCaptor<FRParty> captor = ArgumentCaptor.captor();
        verify(partyRepository).save(captor.capture());
        final FRPartyData updatedParty = captor.getValue().getParty();
        assertThat(updatedParty.getPartyId()).isEqualTo(partyId);
        assertThat(updatedParty.getName()).isEqualTo(obParty.getName());
        assertThat(updatedParty.getPartyType()).isEqualTo(FRPartyConverter.toFRPartyType(obParty.getPartyType()));

    }

    private FRAccountData accountDataWithBalance(OBReadBalance1DataBalanceInner balance) {
        FRAccountData accountData = new FRAccountData();
        accountData.setAccount(new OBAccount6().accountId(balance.getAccountId()));
        accountData.setBalances(Collections.singletonList(balance));
        return accountData;
    }
}
