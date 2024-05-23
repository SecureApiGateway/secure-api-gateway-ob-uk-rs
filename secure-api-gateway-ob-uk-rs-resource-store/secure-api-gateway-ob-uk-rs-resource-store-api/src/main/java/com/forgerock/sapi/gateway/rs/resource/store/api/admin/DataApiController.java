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

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRAccountBeneficiaryConverter.toOBBeneficiary5;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRCashBalanceConverter.toOBReadBalance1DataBalance;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRDirectDebitConverter.toOBReadDirectDebit2DataDirectDebit;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRFinancialAccountConverter.toOBAccount6;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FROfferConverter.toOBReadOffer1DataOffer;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRPartyConverter.toFRPartyData;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRPartyConverter.toOBParty2;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRStatementConverter.toOBStatement2;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRTransactionConverter.toOBTransaction6;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRScheduledPaymentConverter.toOBScheduledPayment3;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRStandingOrderConverter.toOBStandingOrder6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRPartyData;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.model.User;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.services.UserClientService;
import com.forgerock.sapi.gateway.rs.resource.store.api.admin.exceptions.DataApiException;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.account.FRAccountData;
import com.forgerock.sapi.gateway.rs.resource.store.datamodel.user.FRUserData;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRParty;
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

import lombok.extern.slf4j.Slf4j;

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

    private final FRCustomerInfoRepository customerInfoRepository;
    private final boolean isCustomerInfoEnabled;
    private final DataUpdater dataUpdater;
    private final DataCreator dataCreator;
    private final DataExporter dataExporter;
    private final UserClientService userClientService;

    public DataApiController(FRDirectDebitRepository directDebitRepository, FRAccountRepository accountsRepository,
                             FRBalanceRepository balanceRepository, FRBeneficiaryRepository beneficiaryRepository,
                             FRProductRepository productRepository, FRStandingOrderRepository standingOrderRepository,
                             FRTransactionRepository transactionRepository, FRStatementRepository statementRepository,
                             DataCreator dataCreator, FRScheduledPaymentRepository scheduledPayment1Repository,
                             FRPartyRepository partyRepository, FRCustomerInfoRepository customerInfoRepository,
                             @Value("${rs.data.customerInfo.enabled:false}") Boolean isCustomerInfoEnabled,
                             DataUpdater dataUpdater, DataExporter dataExporter, FROfferRepository offerRepository,
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
        this.customerInfoRepository = customerInfoRepository;
        this.dataUpdater = dataUpdater;
        this.offerRepository = offerRepository;
        this.userClientService = userClientService;
        this.isCustomerInfoEnabled = isCustomerInfoEnabled;
        this.dataExporter = dataExporter;
    }

    @Override
    public ResponseEntity<Page<FRAccountData>> exportAccountData(
            @PageableDefault Pageable pageable
    ) {
        List<FRAccountData> accountDataList = new ArrayList<>();
        Page<FRAccount> page = accountsRepository.findAll(pageable);
        // process last page
        for (FRAccount account : page.getContent()) {
            accountDataList.add(dataExporter.exportAccountData(account));
        }
        return ResponseEntity.ok(new PageImpl<>(accountDataList, page.getPageable(), page.getTotalElements()));
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
            userData.addAccountData(dataExporter.exportAccountData(account));
        }

        userData.setCustomerInfo(
                FRCustomerInfoConverter.entityToDto(
                        customerInfoRepository.findByUserID(userId)
                )
        );

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

            // This method is currently keyed off the userName field in the user data.
            // Overwrite any supplied userId with the value retrieved from the platform for the username so that it is consistent
            userData.setUserId(userId);
            log.debug("user found with id: {}", userId);
            // update customer information
            log.debug("Customer information enabled: {}", isCustomerInfoEnabled);
            if (isCustomerInfoEnabled && userData.getCustomerInfo() != null) {
                log.debug("Creating customer information for user {}:{}", userData.getUserName(), userId);
                dataUpdater.updateCustomerInfo(userData.getCustomerInfo(), userId);
            }

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
            log.debug("user found with id: {}", userId);
            FRUserData userDataResponse = new FRUserData();
            userDataResponse.setUserId(userId);
            userDataResponse.setUserName(user.getUserName());
            // Customer info
            log.debug("Customer information enabled: {}", isCustomerInfoEnabled);
            if (isCustomerInfoEnabled && userData.getCustomerInfo() != null) {
                log.debug("Creating customer information for user {}:{}", userData.getUserName(), userId);
                userDataResponse.setCustomerInfo(
                        FRCustomerInfoConverter.entityToDto(
                                dataCreator.createCustomerInfo(userData.getCustomerInfo(), userId)
                        )
                );
            }

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
                dataCreator.createBalances(accountData, existingAccountIds).forEach(b -> accountDataResponse.addBalance(toOBReadBalance1DataBalance(b.getBalance())));
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
                dataCreator.createOffers(accountData, existingAccountIds).forEach(d -> accountDataResponse.addOffer(toOBReadOffer1DataOffer(d.getOffer())));

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
            @RequestParam("userName") String userName
    ) throws DataApiException {
        log.debug("deleting user account data by userName '{}'", userName);
        try {
            User user = userClientService.getUserByName(userName);
            if (user != null) {
                log.debug("deleting user account data for user Id '{}'", user.getId());
                customerInfoRepository.deleteFRCustomerInfoByUserID(user.getId());
                Collection<FRAccount> accounts = accountsRepository.findByUserID(user.getId());
                for (FRAccount account : accounts) {
                    deleteAccount(account, user.getId());
                }
                return ResponseEntity.ok(accounts.size() > 0);
            }
        } catch (ExceptionClient exceptionClient) {
            if (exceptionClient.getErrorClient().getErrorType().getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
                log.debug("No user details with user name {} found, no data has been deleted", userName);
            } else {
                log.error(
                        "Status: {}, reason: {}",
                        exceptionClient.getErrorClient().getErrorType().getHttpStatus(),
                        exceptionClient.getReason()
                );
                throw new DataApiException(exceptionClient);
            }
        }
        return ResponseEntity.ok(false);
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
}
