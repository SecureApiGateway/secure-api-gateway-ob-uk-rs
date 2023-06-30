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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_3.accounts.AccountsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rcs.conent.store.client.account.v3_1_10.AccountAccessConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.account.OBAccount6;
import uk.org.openbanking.datamodel.account.OBReadAccount5;
import uk.org.openbanking.datamodel.account.OBReadAccount5Data;
import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;

@Controller("AccountsApiV3.1.3")
@Slf4j
public class AccountsApiController implements AccountsApi {

    private final AccountAccessConsentStoreClient accountAccessConsentStoreClient;

    private final FRAccountRepository frAccountRepository;

    public AccountsApiController(FRAccountRepository frAccountRepository, AccountAccessConsentStoreClient accountAccessConsentStoreClient) {
        this.frAccountRepository = frAccountRepository;
        this.accountAccessConsentStoreClient = accountAccessConsentStoreClient;
    }

    @Override
    public ResponseEntity<OBReadAccount5> getAccount(String accountId,
                                                     String authorization,
                                                     DateTime xFapiAuthDate,
                                                     String xFapiCustomerIpAddress,
                                                     String xFapiInteractionId,
                                                     String xCustomerUserAgent,
                                                     String consentId,
                                                     String apiClientId) throws OBErrorResponseException {

        log.info("Read account {} for consentId: {}", accountId, consentId);
        final AccountAccessConsent consent = getConsent(consentId, apiClientId);

        if (!consent.getAuthorisedAccountIds().contains(accountId)) {
            throw new OBErrorResponseException(HttpStatus.BAD_REQUEST, OBRIErrorResponseCategory.REQUEST_INVALID, OBRIErrorType.UNAUTHORISED_ACCOUNT.toOBError1(accountId));
        }

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
                                                      String apiClientId) {
        log.info("Get Accounts for consentId: {}", consentId);

        final AccountAccessConsent consent = accountAccessConsentStoreClient.getConsent(consentId, apiClientId);
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

    private AccountAccessConsent getConsent(String consentId, String apiClientId) throws OBErrorResponseException {
        final AccountAccessConsent consent = accountAccessConsentStoreClient.getConsent(consentId, apiClientId);
        if (!consent.getStatus().equals(OBExternalRequestStatus1Code.AUTHORISED.toString())) {
            throw new OBErrorResponseException(HttpStatus.FORBIDDEN, OBRIErrorResponseCategory.REQUEST_INVALID, OBRIErrorType.ACCOUNT_REQUEST_WAITING_PSU_CONSENT.toOBError1(consent.getStatus()));
        }
        return consent;
    }

}
