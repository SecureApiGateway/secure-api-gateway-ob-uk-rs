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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRReadRefundAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.FRAccountTestDataFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RefundAccountServiceTest {

    @Mock
    private FRAccountRepository accountRepository;

    @InjectMocks
    private RefundAccountService refundAccountService;

    @Test
    void testRefundAccountNotRequested() {
        assertThat(refundAccountService.getRefundAccountData(FRReadRefundAccount.NO, new DomesticPaymentConsent())).isEqualTo(Optional.empty());
        assertThat(refundAccountService.getRefundAccountData(null, new DomesticPaymentConsent())).isEqualTo(Optional.empty());
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void testRefundAccountRequested() {
        final DomesticPaymentConsent paymentConsent = new DomesticPaymentConsent();
        final String accountId = "test-acc-987";
        paymentConsent.setAuthorisedDebtorAccountId(accountId);

        final FRAccount mockAccountResponse = FRAccountTestDataFactory.aValidFRAccount();
        given(accountRepository.byAccountId(eq(accountId))).willReturn(mockAccountResponse);

        final Optional<FRResponseDataRefund> refundAccountData = refundAccountService.getRefundAccountData(FRReadRefundAccount.YES, paymentConsent);
        assertThat(refundAccountData.isPresent()).isTrue();
        final FRResponseDataRefund refundData = refundAccountData.get();
        assertThat(refundData.getAccount()).isEqualTo(mockAccountResponse.getAccount().getFirstAccount());
    }

}