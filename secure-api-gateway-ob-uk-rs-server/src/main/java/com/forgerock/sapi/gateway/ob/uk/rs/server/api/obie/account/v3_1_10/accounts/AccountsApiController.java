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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.accounts;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRFinancialAccountConverter.toOBAccount6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.accounts.AccountsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import uk.org.openbanking.datamodel.v3.account.OBAccount6;
import uk.org.openbanking.datamodel.v3.account.OBReadAccount6;
import uk.org.openbanking.datamodel.v3.account.OBReadAccount6Data;

@Controller("AccountsApiV3.1.10")
public class AccountsApiController implements AccountsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final AccountResourceAccessService accountResourceAccessService;

    private final FRAccountRepository frAccountRepository;

    public AccountsApiController(FRAccountRepository frAccountRepository, AccountResourceAccessService accountResourceAccessService) {
        this.frAccountRepository = frAccountRepository;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadAccount6> getAccount(String accountId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {

        logger.info("Read account {} for consentId: {}", accountId, consentId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkPermissions(consent);
        FRAccount account = frAccountRepository.byAccountId(accountId, consent.getRequestObj().getData().getPermissions());

        List<OBAccount6> obAccounts = Collections.singletonList(toOBAccount6(account.getAccount()));
        return ResponseEntity.ok(new OBReadAccount6()
                .data(new OBReadAccount6Data().account(obAccounts))
                .links(LinksHelper.createGetAccountsSelfLink(getClass(), accountId))
                .meta(PaginationUtil.generateMetaData(1)));
    }

    @Override
    public ResponseEntity<OBReadAccount6> getAccounts(String page,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {
        logger.info("Get Accounts for consentId: {}", consentId);

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkPermissions(consent);
        List<FRAccount> frAccounts = frAccountRepository.byAccountIds(consent.getAuthorisedAccountIds(), consent.getRequestObj().getData().getPermissions());
        List<OBAccount6> obAccounts = frAccounts
                .stream()
                .map(frAccount -> toOBAccount6(frAccount.getAccount()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new OBReadAccount6()
                .data(new OBReadAccount6Data().account(obAccounts))
                .links(LinksHelper.createGetAccountsSelfLink(getClass()))
                .meta(PaginationUtil.generateMetaData(1)));
    }

    private static void checkPermissions(AccountAccessConsent consent) throws OBErrorException {
        final List<FRExternalPermissionsCode> permissions = consent.getRequestObj().getData().getPermissions();
        if (!permissions.contains(FRExternalPermissionsCode.READACCOUNTSBASIC) && !permissions.contains(FRExternalPermissionsCode.READACCOUNTSDETAIL)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, FRExternalPermissionsCode.READACCOUNTSBASIC + " or " + FRExternalPermissionsCode.READACCOUNTSDETAIL);
        }
    }

}
