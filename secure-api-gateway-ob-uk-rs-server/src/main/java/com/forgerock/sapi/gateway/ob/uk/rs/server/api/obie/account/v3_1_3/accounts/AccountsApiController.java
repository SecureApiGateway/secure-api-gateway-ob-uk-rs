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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_3.accounts;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRFinancialAccountConverter.toOBAccount6;
import static java.util.Collections.singletonList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_3.accounts.AccountsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.account.OBAccount6;
import uk.org.openbanking.datamodel.account.OBReadAccount5;
import uk.org.openbanking.datamodel.account.OBReadAccount5Data;

@Controller("AccountsApiV3.1.3")
@Slf4j
public class AccountsApiController implements AccountsApi {

    private final AccountResourceAccessService accountResourceAccessService;

    private final FRAccountRepository frAccountRepository;

    public AccountsApiController(FRAccountRepository frAccountRepository, AccountResourceAccessService accountResourceAccessService) {
        this.frAccountRepository = frAccountRepository;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadAccount5> getAccount(String accountId,
                                                     String authorization,
                                                     DateTime xFapiAuthDate,
                                                     String xFapiCustomerIpAddress,
                                                     String xFapiInteractionId,
                                                     String xCustomerUserAgent,
                                                     String consentId,
                                                     String apiClientId) throws OBErrorException {

        log.info("Read account {} for consentId: {}", accountId, consentId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, singletonList(accountId));
        FRAccount account = frAccountRepository.byAccountId(accountId, consent.getRequestObj().getData().getPermissions());

        List<OBAccount6> obAccounts = Collections.singletonList(toOBAccount6(account.getAccount()));
        return ResponseEntity.ok(new OBReadAccount5()
                .data(new OBReadAccount5Data().account(obAccounts))
                .links(LinksHelper.createGetAccountsSelfLink(getClass(), accountId))
                .meta(PaginationUtil.generateMetaData(1)));
    }

    @Override
    public ResponseEntity<OBReadAccount5> getAccounts(String page,
                                                      String authorization,
                                                      DateTime xFapiAuthDate,
                                                      String xFapiCustomerIpAddress,
                                                      String xFapiInteractionId,
                                                      String xCustomerUserAgent,
                                                      String consentId,
                                                      String apiClientId) throws OBErrorException {
        log.info("Get Accounts for consentId: {}", consentId);

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        List<FRAccount> frAccounts = frAccountRepository.byAccountIds(consent.getAuthorisedAccountIds(), consent.getRequestObj().getData().getPermissions());
        List<OBAccount6> obAccounts = frAccounts
                .stream()
                .map(frAccount -> toOBAccount6(frAccount.getAccount()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new OBReadAccount5()
                .data(new OBReadAccount5Data().account(obAccounts))
                .links(LinksHelper.createGetAccountsSelfLink(getClass()))
                .meta(PaginationUtil.generateMetaData(1)));
    }

}
