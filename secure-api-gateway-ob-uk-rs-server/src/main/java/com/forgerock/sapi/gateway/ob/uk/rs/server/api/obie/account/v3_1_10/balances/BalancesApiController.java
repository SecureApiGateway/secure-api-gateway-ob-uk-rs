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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.balances;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRCashBalanceConverter.toOBReadBalance1DataBalance;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.balances.BalancesApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBalance;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.balances.FRBalanceRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;

import uk.org.openbanking.datamodel.v3.account.OBReadBalance1;
import uk.org.openbanking.datamodel.v3.account.OBReadBalance1Data;

@Controller("BalancesApiV3.1.10")
public class BalancesApiController implements BalancesApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountResourceAccessService accountResourceAccessService;

    private final FRBalanceRepository frBalanceRepository;
    @Value("${rs.page.default.balances.size:10}")
    private int PAGE_LIMIT_BALANCES;

    public BalancesApiController(FRBalanceRepository frBalanceRepository, AccountResourceAccessService accountResourceAccessService) {
        this.frBalanceRepository = frBalanceRepository;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadBalance1> getAccountBalances(String accountId,
            int page,
            String authorization,
            String xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {

        logger.info("Read balances for consentId: {}, accountId: {}, apiClientId: {}", consentId, accountId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkConsentHasRequiredPermission(consent);
        Page<FRBalance> balances = frBalanceRepository.findByAccountId(accountId, PageRequest.of(page, PAGE_LIMIT_BALANCES));
        int totalPage = balances.getTotalPages();

        return ResponseEntity.ok(new OBReadBalance1()
                .data(new OBReadBalance1Data().balance(balances.getContent().stream()
                        .map(b -> toOBReadBalance1DataBalance(b.getBalance()))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(buildGetAccountBalancesUri(accountId), page, totalPage))
                .meta(PaginationUtil.generateMetaData(totalPage)));
    }

    @Override
    public ResponseEntity<OBReadBalance1> getBalances(int page,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {
        logger.info("Reading balances for consentId: {}, apiClientId: {}", consentId, apiClientId);

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkConsentHasRequiredPermission(consent);
        Page<FRBalance> balances = frBalanceRepository.findByAccountIdIn(consent.getAuthorisedAccountIds(), PageRequest.of(page, PAGE_LIMIT_BALANCES));

        int totalPage = balances.getTotalPages();

        return ResponseEntity.ok(new OBReadBalance1()
                .data(new OBReadBalance1Data().balance(balances.getContent().stream()
                        .map(b -> toOBReadBalance1DataBalance(b.getBalance()))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(buildGetBalancesUri(), page, totalPage))
                .meta(PaginationUtil.generateMetaData(totalPage)));
    }

    private String buildGetBalancesUri() {
        return linkTo(getClass()).slash("balances").toString();
    }

    private String buildGetAccountBalancesUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("balances").toString();
    }

    private static void checkConsentHasRequiredPermission(AccountAccessConsent consent) throws OBErrorException {
        final FRExternalPermissionsCode readBalancesPermission = FRExternalPermissionsCode.READBALANCES;
        if (!consent.getRequestObj().getData().getPermissions().contains(readBalancesPermission)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, readBalancesPermission.getValue());
        }
    }

}
