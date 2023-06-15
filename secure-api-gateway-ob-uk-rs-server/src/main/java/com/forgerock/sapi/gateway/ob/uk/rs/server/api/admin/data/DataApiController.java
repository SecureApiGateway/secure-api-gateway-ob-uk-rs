/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.admin.data;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRPartyData;
import com.forgerock.sapi.gateway.ob.uk.rs.admin.api.data.DataApi;
import com.forgerock.sapi.gateway.ob.uk.rs.admin.api.data.dto.FRAccountData;
import com.forgerock.sapi.gateway.ob.uk.rs.admin.api.data.dto.FRUserData;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.model.User;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.services.UserClientService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.exceptions.DataApiException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.*;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.balances.FRBalanceRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.beneficiaries.FRBeneficiaryRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.directdebits.FRDirectDebitRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.offers.FROfferRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.party.FRPartyRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.products.FRProductRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.scheduledpayments.FRScheduledPaymentRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.standingorders.FRStandingOrderRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.statements.FRStatementRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.transactions.FRTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRAccountBeneficiaryConverter.toOBBeneficiary5;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRCashBalanceConverter.toOBCashBalance1;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRDirectDebitConverter.toOBReadDirectDebit2DataDirectDebit;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRFinancialAccountConverter.toOBAccount6;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FROfferConverter.toOBOffer1;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRPartyConverter.toFRPartyData;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRPartyConverter.toOBParty2;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRStatementConverter.toOBStatement2;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRTransactionConverter.toOBTransaction6;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRScheduledPaymentConverter.toOBScheduledPayment3;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRStandingOrderConverter.toOBStandingOrder6;

@Controller("DataApi")
@Slf4j
public class DataApiController implements DataApi {

    private final FRAccountRepository accountsRepository;
    private final FRBalanceRepository balanceRepository;
    private final FRBeneficiaryRepository beneficiaryRepository;
    private final FRDirectDebitRepository directDebitRepository;
    private final FRProductRepository productRepository;
    private final FRStandingOrderRepository standingOrderRepository;
    private final FRTransactionRepository transactionRepository;
    private final FRStatementRepository statementRepository;
    private final FRScheduledPaymentRepository scheduledPayment1Repository;
    private final FRPartyRepository partyRepository;
    private final FROfferRepository offerRepository;
    private final DataUpdater dataUpdater;
    private final DataCreator dataCreator;
    private final UserClientService userClientService;

    public DataApiController(FRDirectDebitRepository directDebitRepository, FRAccountRepository accountsRepository,
                             FRBalanceRepository balanceRepository, FRBeneficiaryRepository beneficiaryRepository,
                             FRProductRepository productRepository, FRStandingOrderRepository standingOrderRepository,
                             FRTransactionRepository transactionRepository, FRStatementRepository statementRepository,
                             DataCreator dataCreator, FRScheduledPaymentRepository scheduledPayment1Repository,
                             FRPartyRepository partyRepository, DataUpdater dataUpdater, FROfferRepository offerRepository,
                             UserClientService userClientService
    ) {
        this.directDebitRepository = directDebitRepository;
        this.accountsRepository = accountsRepository;
        this.balanceRepository = balanceRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.productRepository = productRepository;
        this.standingOrderRepository = standingOrderRepository;
        this.transactionRepository = transactionRepository;
        this.statementRepository = statementRepository;
        this.dataCreator = dataCreator;
        this.scheduledPayment1Repository = scheduledPayment1Repository;
        this.partyRepository = partyRepository;
        this.dataUpdater = dataUpdater;
        this.offerRepository = offerRepository;
        this.userClientService = userClientService;
    }

    @Override
    public ResponseEntity<Page<FRAccountData>> exportAccountData(
            @PageableDefault Pageable pageable
    ) {
        List<FRAccountData> accountDatas = new ArrayList<>();
        Page<FRAccount> page = accountsRepository.findAll(pageable);
        // process last page
        for (FRAccount account : page.getContent()) {
            accountDatas.add(getAccount(account));
        }
        return ResponseEntity.ok(new PageImpl<>(accountDatas, page.getPageable(), page.getTotalElements()));
    }

    @Override
    public ResponseEntity<Boolean> hasData(
            @RequestParam("userId") String userId
    ) {
        return ResponseEntity.ok(accountsRepository.findByUserID(userId).size() > 0);
    }

    @Override
    public ResponseEntity<FRUserData> exportUserData(
            @RequestParam("userId") String userId
    ) {
        FRUserData userData = new FRUserData(userId);
        for (FRAccount account : accountsRepository.findByUserID(userId)) {
            userData.addAccountData(getAccount(account));
        }

        FRParty byUserId = partyRepository.findByUserId(userId);
        if (byUserId != null) {
            userData.setParty(toOBParty2(byUserId.getParty()));
        }
        return ResponseEntity.ok(userData);
    }

    @Override
    public ResponseEntity updateUserData(
            @RequestBody FRUserData userData
    ) throws DataApiException {
        try {
            // verify the user has been created in the Identity cloud platform
            // Will throw ExceptionClient Not Found when:
            // - The user response is null
            // - User not found
            // - The account is inactive
            // - HttpClientErrorException 404 returned by IG
            User user = userClientService.getUserByName(userData.getUserName());
            // user exist, carry on to update the user data
            String userId = user.getId();
            dataUpdater.updateParty(userData);

            Set<String> accountIds = accountsRepository.findByUserID(userId)
                    .stream()
                    .map(FRAccount::getId)
                    .collect(Collectors.toSet());
            for (FRAccountData accountDataDiff : userData.getAccountDatas()) {

                String accountId = accountDataDiff.getAccount().getAccountId();
                //Account
                Optional<FRAccount> isAccount = accountsRepository.findById(accountId);
                if (isAccount.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account ID '" + accountId + "' doesn't exist");
                }
                FRAccount account = isAccount.get();
                if (!account.getUserID().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account ID '"
                            + accountDataDiff.getAccount().getAccountId() + "' is not owned by user '" + userId + "'");

                }

                dataUpdater.updateAccount(accountDataDiff, account, accountIds);
                dataUpdater.updateBalances(accountDataDiff, accountIds);
                dataUpdater.updateProducts(accountDataDiff, accountIds);
                dataUpdater.updateParty(accountDataDiff, accountIds);
                dataUpdater.updateBeneficiaries(accountDataDiff, accountIds);
                dataUpdater.updateDirectDebits(accountDataDiff, accountIds);
                dataUpdater.updateStandingOrders(accountDataDiff, accountIds);
                dataUpdater.updateTransactions(accountDataDiff, accountIds);
                dataUpdater.updateStatements(accountDataDiff, accountIds);
                dataUpdater.updateScheduledPayments(accountDataDiff, accountIds);
                dataUpdater.updateOffers(accountDataDiff, accountIds);
            }
            return exportUserData(userId);
        } catch (ExceptionClient exceptionClient) {
            log.error(
                    "Status: {}, reason: {}",
                    exceptionClient.getErrorClient().getErrorType().getHttpStatus(),
                    exceptionClient.getReason()
            );
            throw new DataApiException(exceptionClient);
        }
    }

    @Override
    public ResponseEntity importUserData(
            @RequestBody FRUserData userData
    ) throws DataApiException {
        try {
            // verify the user has been created in the Identity cloud platform
            // Will throw ExceptionClient Not Found when:
            // - The user response is null
            // - User not found
            // - The account is inactive
            // - HttpClientErrorException 404 returned by IG
            User user = userClientService.getUserByName(userData.getUserName());
            // user exist, carry on to create the user data
            String userId = user.getId();
            FRUserData userDataResponse = new FRUserData();
            userDataResponse.setUserId(userId);
            userDataResponse.setUserName(user.getUserName());
            if (userData.getParty() != null) {
                FRParty existingParty = partyRepository.findByUserId(userId);

                //Party
                if (existingParty != null) {
                    userData.getParty().setPartyId(existingParty.getId());
                }

                FRParty newParty = new FRParty();
                newParty.setUserId(userId);
                newParty.setParty(toFRPartyData(userData.getParty()));
                newParty.setId(userData.getParty().getPartyId());
                FRPartyData newPartyData = partyRepository.save(newParty).getParty();
                userDataResponse.setParty(toOBParty2(newPartyData));
            }

            Set<String> existingAccountIds = accountsRepository.findByUserID(userId)
                    .stream()
                    .map(FRAccount::getId)
                    .collect(Collectors.toSet());

            for (FRAccountData accountData : userData.getAccountDatas()) {
                FRAccountData accountDataResponse = new FRAccountData();

                //Account
                if (accountData.getAccount() != null) {
                    FRFinancialAccount frAccount = dataCreator.createAccount(accountData, userId).getAccount();
                    accountDataResponse.setAccount(toOBAccount6(frAccount));
                    existingAccountIds.add(accountDataResponse.getAccount().getAccountId());
                }
                //Product
                dataCreator.createProducts(accountData, existingAccountIds).ifPresent(accountDataResponse::setProduct);
                //Party
                dataCreator.createParty(accountData).ifPresent(p -> accountDataResponse.setParty(toOBParty2(p)));
                //Balance
                dataCreator.createBalances(accountData, existingAccountIds).forEach(b -> accountDataResponse.addBalance(toOBCashBalance1(b.getBalance())));
                //Beneficiaries
                dataCreator.createBeneficiaries(accountData, existingAccountIds).forEach(b -> accountDataResponse.addBeneficiary(toOBBeneficiary5(b.getBeneficiary())));
                //Direct debits
                dataCreator.createDirectDebits(accountData, existingAccountIds).forEach(d -> accountDataResponse.addDirectDebit(toOBReadDirectDebit2DataDirectDebit(d.getDirectDebit())));
                //Standing orders
                dataCreator.createStandingOrders(accountData, existingAccountIds).forEach(d -> accountDataResponse.addStandingOrder(toOBStandingOrder6(d.getStandingOrder())));
                //Transactions
                dataCreator.createTransactions(accountData, existingAccountIds).forEach(d -> accountDataResponse.addTransaction(toOBTransaction6(d.getTransaction())));
                //Statements
                dataCreator.createStatements(accountData, existingAccountIds).forEach(d -> accountDataResponse.addStatement(toOBStatement2(d.getStatement())));
                //Scheduled payments
                dataCreator.createScheduledPayments(accountData, existingAccountIds).forEach(d -> accountDataResponse.addScheduledPayment(toOBScheduledPayment3(d.getScheduledPayment())));
                //offers
                dataCreator.createOffers(accountData, existingAccountIds).forEach(d -> accountDataResponse.addOffer(toOBOffer1(d.getOffer())));

                userDataResponse.addAccountData(accountDataResponse);
            }
            return ResponseEntity.ok(userDataResponse);

        } catch (ExceptionClient exceptionClient) {
            log.error(
                    "Status: {}, reason: {}",
                    exceptionClient.getErrorClient().getErrorType().getHttpStatus(),
                    exceptionClient.getReason()
            );
            throw new DataApiException(exceptionClient);
        }
    }

    @Override
    public ResponseEntity<Boolean> deleteUserData(
            @RequestParam("userId") String userId
    ) {
        Collection<FRAccount> accounts = accountsRepository.findByUserID(userId);
        for (FRAccount account : accounts) {
            deleteAccount(account, userId);
        }
        return ResponseEntity.ok(accounts.size() > 0);
    }

    private void deleteAccount(FRAccount account, String userId) {
        accountsRepository.deleteById(account.getId());
        balanceRepository.deleteBalanceByAccountId(account.getId());
        productRepository.deleteProductByAccountId(account.getId());
        beneficiaryRepository.deleteBeneficiaryByAccountId(account.getId());
        directDebitRepository.deleteDirectDebitByAccountId(account.getId());
        standingOrderRepository.deleteStandingOrderByAccountId(account.getId());
        transactionRepository.deleteTransactionByAccountId(account.getId());
        statementRepository.deleteFRStatementByAccountId(account.getId());
        scheduledPayment1Repository.deleteFRScheduledPaymentByAccountId(account.getId());
        partyRepository.deleteFRPartyByAccountId(account.getId());
        offerRepository.deleteFROfferByAccountId(account.getId());
        partyRepository.deleteFRPartyByUserId(userId);

    }

    private FRAccountData getAccount(FRAccount account) {
        FRAccountData accountData = new FRAccountData();
        accountData.setAccount(toOBAccount6(account.getAccount()));

        Page<FRProduct> products = productRepository.findByAccountId(account.getId(), (PageRequest.of(0, 1)));
        if (!products.getContent().isEmpty()) {
            accountData.setProduct(products.getContent().get(0).getProduct());
        }
        {
            final int pageLimit = 300;
            int pageNumber = 0;
            Page<FRBalance> page = balanceRepository.findByAccountId(account.getId(), PageRequest.of(pageNumber, pageLimit));
            while (page.hasNext()) {
                for (FRBalance balance : page.getContent()) {
                    accountData.addBalance(toOBCashBalance1(balance.getBalance()));
                }
                page = balanceRepository.findAll(PageRequest.of(pageNumber++, pageLimit));
            }
            // process last page
            for (FRBalance balance : page.getContent()) {
                accountData.addBalance(toOBCashBalance1(balance.getBalance()));
            }
        }
        {
            final int pageLimit = 300;
            int pageNumber = 0;
            Page<FRBeneficiary> page = beneficiaryRepository.findByAccountId(account.getId(), PageRequest.of(pageNumber, pageLimit));
            while (page.hasNext()) {
                for (FRBeneficiary beneficiary : page.getContent()) {
                    accountData.addBeneficiary(toOBBeneficiary5(beneficiary.getBeneficiary()));
                }
                page = beneficiaryRepository.findAll(PageRequest.of(pageNumber++, pageLimit));
            }
            // process last page
            for (FRBeneficiary beneficiary : page.getContent()) {
                accountData.addBeneficiary(toOBBeneficiary5(beneficiary.getBeneficiary()));
            }
        }
        {
            final int pageLimit = 300;
            int pageNumber = 0;
            Page<FRDirectDebit> page = directDebitRepository.findByAccountId(account.getId(), PageRequest.of(pageNumber, pageLimit));
            while (page.hasNext()) {
                for (FRDirectDebit directDebit : page.getContent()) {
                    accountData.addDirectDebit(toOBReadDirectDebit2DataDirectDebit(directDebit.getDirectDebit()));
                }
                page = directDebitRepository.findAll(PageRequest.of(pageNumber++, pageLimit));
            }
            // process last page
            for (FRDirectDebit directDebit : page.getContent()) {
                accountData.addDirectDebit(toOBReadDirectDebit2DataDirectDebit(directDebit.getDirectDebit()));
            }
        }
        {
            final int pageLimit = 300;
            int pageNumber = 0;
            Page<FRStandingOrder> page = standingOrderRepository.findByAccountId(account.getId(), PageRequest.of(pageNumber, pageLimit));
            while (page.hasNext()) {
                for (FRStandingOrder standingOrder : page.getContent()) {
                    accountData.addStandingOrder(toOBStandingOrder6(standingOrder.getStandingOrder()));
                }
                page = standingOrderRepository.findAll(PageRequest.of(pageNumber++, pageLimit));
            }
            // process last page
            for (FRStandingOrder standingOrder : page.getContent()) {
                accountData.addStandingOrder(toOBStandingOrder6(standingOrder.getStandingOrder()));
            }
        }
        {
            final int pageLimit = 300;
            int pageNumber = 0;
            Page<FRTransaction> page = transactionRepository.findByAccountId(account.getId(), PageRequest.of(pageNumber, pageLimit));
            while (page.hasNext()) {
                for (FRTransaction transaction : page.getContent()) {
                    accountData.addTransaction(toOBTransaction6(transaction.getTransaction()));
                }
                page = transactionRepository.findAll(PageRequest.of(pageNumber++, pageLimit));
            }
            // process last page
            for (FRTransaction transaction : page.getContent()) {
                accountData.addTransaction(toOBTransaction6(transaction.getTransaction()));
            }
        }
        {
            final int pageLimit = 300;
            int pageNumber = 0;
            Page<FRStatement> page = statementRepository.findByAccountId(account.getId(), PageRequest.of(pageNumber, pageLimit));
            while (page.hasNext()) {
                for (FRStatement statement1 : page.getContent()) {
                    accountData.addStatement(toOBStatement2(statement1.getStatement()));
                }
                page = statementRepository.findAll(PageRequest.of(pageNumber++, pageLimit));
            }
            // process last page
            for (FRStatement statement1 : page.getContent()) {
                accountData.addStatement(toOBStatement2(statement1.getStatement()));
            }
        }
        {
            final int pageLimit = 300;
            int pageNumber = 0;
            Page<FRScheduledPayment> page = scheduledPayment1Repository.findByAccountId(account.getId(), PageRequest.of(pageNumber, pageLimit));
            while (page.hasNext()) {
                for (FRScheduledPayment scheduledPayment1 : page.getContent()) {
                    accountData.addScheduledPayment(toOBScheduledPayment3(scheduledPayment1.getScheduledPayment()));
                }
                page = scheduledPayment1Repository.findAll(PageRequest.of(pageNumber++, pageLimit));
            }
            // process last page
            for (FRScheduledPayment scheduledPayment1 : page.getContent()) {
                accountData.addScheduledPayment(toOBScheduledPayment3(scheduledPayment1.getScheduledPayment()));
            }
        }
        {
            FRParty party = partyRepository.findByAccountId(account.getId());
            if (party != null) {
                accountData.setParty(toOBParty2(party.getParty()));
            }
        }
        {
            final int pageLimit = 300;
            int pageNumber = 0;
            Page<FROffer> page = offerRepository.findByAccountId(account.getId(), PageRequest.of(pageNumber, pageLimit));
            while (page.hasNext()) {
                for (FROffer offer1 : page.getContent()) {
                    accountData.addOffer(toOBOffer1(offer1.getOffer()));
                }
                page = offerRepository.findAll(PageRequest.of(pageNumber++, pageLimit));
            }
            // process last page
            for (FROffer offer1 : page.getContent()) {
                accountData.addOffer(toOBOffer1(offer1.getOffer()));
            }
        }
        return accountData;
    }
}
