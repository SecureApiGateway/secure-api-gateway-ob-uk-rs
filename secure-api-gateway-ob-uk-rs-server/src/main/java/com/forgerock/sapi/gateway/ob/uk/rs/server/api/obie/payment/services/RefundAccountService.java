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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRFinancialAgent;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRFinancialCreditor;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRReadRefundAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.refund.FRResponseDataRefundFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.BasePaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service to retrieve Account Data to populate the Refund section of a Payment response
 */
@Service
public class RefundAccountService {

    private final FRAccountRepository accountRepository;

    public RefundAccountService(FRAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<FRResponseDataRefund> getDomesticPaymentRefundData(FRReadRefundAccount readRefundAccount, BasePaymentConsent<?> paymentConsent) {
        if (readRefundAccount == FRReadRefundAccount.YES) {
            final FRAccount debtorAccount = accountRepository.byAccountId(paymentConsent.getAuthorisedDebtorAccountId());
            return FRResponseDataRefundFactory.frResponseDataRefund(debtorAccount.getAccount().getFirstAccount());
        } else {
            return Optional.empty();
        }
    }

    public Optional<FRInternationalResponseDataRefund> getInternationalPaymentRefundData(FRReadRefundAccount readRefundAccount,
            FRFinancialCreditor creditor, FRFinancialAgent agent, BasePaymentConsent<?> paymentConsent) {

        return getDomesticPaymentRefundData(readRefundAccount, paymentConsent)
                    .map(refundData -> FRInternationalResponseDataRefund.builder()
                                                                        .account(refundData.getAccount())
                                                                        .creditor(creditor)
                                                                        .agent(agent)
                                                                        .build());
    }
}
