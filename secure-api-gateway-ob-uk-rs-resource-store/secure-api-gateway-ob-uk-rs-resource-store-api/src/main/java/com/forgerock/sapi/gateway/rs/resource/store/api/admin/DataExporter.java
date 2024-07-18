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
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRPartyConverter.toOBParty2;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRStatementConverter.toOBStatement2;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRTransactionConverter.toOBTransaction6;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRScheduledPaymentConverter.toOBScheduledPayment3;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRStandingOrderConverter.toOBStandingOrder6;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.forgerock.sapi.gateway.rs.resource.store.datamodel.account.FRAccountData;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRParty;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRProduct;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRScheduledPayment;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStandingOrder;
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

import uk.org.openbanking.datamodel.v3.account.OBBeneficiary5;
import uk.org.openbanking.datamodel.v3.account.OBParty2;
import uk.org.openbanking.datamodel.v3.account.OBReadBalance1DataBalanceInner;
import uk.org.openbanking.datamodel.v3.account.OBReadDirectDebit2DataDirectDebitInner;
import uk.org.openbanking.datamodel.v3.account.OBReadOffer1DataOfferInner;
import uk.org.openbanking.datamodel.v3.account.OBReadProduct2DataProductInner;
import uk.org.openbanking.datamodel.v3.account.OBScheduledPayment3;
import uk.org.openbanking.datamodel.v3.account.OBStandingOrder6;
import uk.org.openbanking.datamodel.v3.account.OBStatement2;
import uk.org.openbanking.datamodel.v3.account.OBTransaction6;

@Service
public class DataExporter {

    /**
     * Default filter used when querying for data to export, accepts all data returned from the repository
     */
    private static final Predicate ACCEPT_ALL_FILTER = obj -> true;
    /**
     * Filter which removes corrupt FRStandingOrder data, this data cannot be updated by the {@link DataUpdater} due
     * to the FRStandingOrder.id not matching the FRStandingOrder.standingOrder.standingOrderId.
     * <p>
     * This is an issue because the FRStandingOrder.standingOrder.standingOrderId is returned in the OB response,
     * and the repo queries can only query for the FRStandingOrder.id value (which by convention should match).
     * <p>
     * Data created by the StandingOrderService, invoked when a TPP submits a StandingOrder, has a bug which means
     * that FRStandingOrders created this way have ids that do not match.
     */
    private static final Predicate<FRStandingOrder> REMOVE_CORRUPT_STANDING_ORDERS_FILTER =
            frStandingOrder -> frStandingOrder.getStandingOrder() != null
                    && frStandingOrder.getId().equals(frStandingOrder.getStandingOrder().getStandingOrderId());
    /**
     * See comment for REMOVE_CORRUPT_STANDING_ORDERS_FILTER, the same issue affects FRScheduledPayments.
     */
    private static final Predicate<FRScheduledPayment> REMOVE_CORRUPT_SCHEDULED_PAYMENTS_FILTER =
            frScheduledPayment -> frScheduledPayment.getScheduledPayment() != null
                    && frScheduledPayment.getId().equals(frScheduledPayment.getScheduledPayment().getScheduledPaymentId());

    private final FRBalanceRepository balanceRepository;
    private final FRBeneficiaryRepository beneficiaryRepository;
    private final FRDirectDebitRepository directDebitRepository;
    private final FRProductRepository productRepository;
    private final FRStandingOrderRepository standingOrderRepository;
    private final FRTransactionRepository transactionRepository;
    private final FRStatementRepository statementRepository;
    private final FRScheduledPaymentRepository scheduledPaymentRepository;
    private final FRPartyRepository partyRepository;
    private final FROfferRepository offerRepository;

    private final int dataPageSize;

    @Autowired
    public DataExporter(FRBalanceRepository balanceRepository,
                        FRBeneficiaryRepository beneficiaryRepository,
                        FRDirectDebitRepository directDebitRepository,
                        FRProductRepository productRepository,
                        FRStandingOrderRepository standingOrderRepository,
                        FRTransactionRepository transactionRepository,
                        FRStatementRepository statementRepository,
                        FRScheduledPaymentRepository scheduledPaymentRepository,
                        FRPartyRepository partyRepository,
                        FROfferRepository offerRepository,
                        @Value("${rs.data.export.page.size:500}") int dataPageSize
    ) {
        this.balanceRepository = balanceRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.directDebitRepository = directDebitRepository;
        this.productRepository = productRepository;
        this.standingOrderRepository = standingOrderRepository;
        this.transactionRepository = transactionRepository;
        this.statementRepository = statementRepository;
        this.scheduledPaymentRepository = scheduledPaymentRepository;
        this.partyRepository = partyRepository;
        this.offerRepository = offerRepository;
        this.dataPageSize = dataPageSize;
    }

    FRAccountData exportAccountData(FRAccount account) {
        final FRAccountData accountData = new FRAccountData();
        accountData.setAccount(toOBAccount6(account.getAccount()));

        final String accountId = account.getId();
        accountData.setTransactions(getTransactions(accountId));
        accountData.setProduct(getProduct(accountId));
        accountData.setBalances(getBalances(accountId));
        accountData.setBeneficiaries(getBeneficiaries(accountId));
        accountData.setDirectDebits(getDirectDebits(accountId));
        accountData.setStandingOrders(getStandingOrders(accountId));
        accountData.setStatements(getStatements(accountId));
        accountData.setScheduledPayments(getScheduledPayments(accountId));
        accountData.setOffers(getOffers(accountId));
        accountData.setParty(getParty(accountId));

        return accountData;
    }

    private OBParty2 getParty(String accountId) {
        final FRParty party = partyRepository.findByAccountId(accountId);
        if (party != null) {
            return toOBParty2(party.getParty());
        }
        return null;
    }

    private List<OBReadOffer1DataOfferInner> getOffers(String accountId) {
        return executePagingFindQuery(accountId, offerRepository::findByAccountId,
                frOffer -> toOBReadOffer1DataOffer(frOffer.getOffer()));
    }

    private List<OBScheduledPayment3> getScheduledPayments(String accountId) {
        return executePagingFindQuery(accountId, scheduledPaymentRepository::findByAccountId,
                REMOVE_CORRUPT_SCHEDULED_PAYMENTS_FILTER,
                frScheduledPayment -> toOBScheduledPayment3(frScheduledPayment.getScheduledPayment()));
    }

    private List<OBStatement2> getStatements(String accountId) {
        return executePagingFindQuery(accountId, statementRepository::findByAccountId,
                frStatement -> toOBStatement2(frStatement.getStatement()));
    }

    private List<OBStandingOrder6> getStandingOrders(String accountId) {
        return executePagingFindQuery(accountId, standingOrderRepository::findByAccountId,
                REMOVE_CORRUPT_STANDING_ORDERS_FILTER,
                frStandingOrder -> toOBStandingOrder6(frStandingOrder.getStandingOrder()));
    }

    private List<OBReadDirectDebit2DataDirectDebitInner> getDirectDebits(String accountId) {
        return executePagingFindQuery(accountId, directDebitRepository::findByAccountId,
                frDirectDebit -> toOBReadDirectDebit2DataDirectDebit(frDirectDebit.getDirectDebit()));
    }

    private List<OBBeneficiary5> getBeneficiaries(String accountId) {
        return executePagingFindQuery(accountId, beneficiaryRepository::findByAccountId,
                frBeneficiary -> toOBBeneficiary5(frBeneficiary.getBeneficiary()));
    }

    private List<OBReadBalance1DataBalanceInner> getBalances(String accountId) {
        return executePagingFindQuery(accountId, balanceRepository::findByAccountId,
                frBalance -> toOBReadBalance1DataBalance(frBalance.getBalance()));
    }

    private OBReadProduct2DataProductInner getProduct(String accountId) {
        final Page<FRProduct> product = productRepository.findByAccountId(accountId, PageRequest.ofSize(1));
        if (product.hasContent()) {
            return product.getContent().get(0).getProduct();
        }
        return null;
    }

    private List<OBTransaction6> getTransactions(String accountId) {
        return executePagingFindQuery(accountId, transactionRepository::findByAccountId,
                frTransaction -> toOBTransaction6(frTransaction.getTransaction()));
    }

    private <F, O> List<O> executePagingFindQuery(String accountId,
                                                  BiFunction<String, Pageable, Page<F>> findByAccountIdPagerQuery,
                                                  Function<F, O> obDataModelConverter) {

        return executePagingFindQuery(accountId, findByAccountIdPagerQuery, ACCEPT_ALL_FILTER, obDataModelConverter);
    }

    /**
     * Helper function for executing a query that returns data in pages.
     *
     * @param accountId                 the id of the account that we are ret
     * @param findByAccountIdPagerQuery Function which takes an accountId and a Pageable and returns a Page of data
     * @param dataFilter                Predicate which returns true if the data is to be returned in the response,
     *                                  this is designed to allow corrupt data to be removed
     * @param obDataModelConverter      Function which takes an FR data model object and converts it into an OB
     *                                  data model object
     * @param <F>                       FR data model type (database representation)
     * @param <O>                       OB data model type (OB API representation)
     * @return List of OB data model objects retrieved by executing the query for the given accountId, returns an empty list if
     * the query finds no results
     */
    private <F, O> List<O> executePagingFindQuery(String accountId,
                                                  BiFunction<String, Pageable, Page<F>> findByAccountIdPagerQuery,
                                                  Predicate<F> dataFilter,
                                                  Function<F, O> obDataModelConverter) {
        int pageNumber = 0;
        Page<F> pageOfFrItems;
        List<O> obItems = null;
        do {
            pageOfFrItems = findByAccountIdPagerQuery.apply(accountId, PageRequest.of(pageNumber, dataPageSize));
            if (obItems == null) {
                obItems = new ArrayList<>((int) pageOfFrItems.getTotalElements());
            }
            for (F frItem : pageOfFrItems.getContent()) {
                if (dataFilter.test(frItem)) {
                    obItems.add(obDataModelConverter.apply(frItem));
                }
            }
            pageNumber++;
        }
        while (pageOfFrItems.hasNext());

        return obItems;
    }

}
