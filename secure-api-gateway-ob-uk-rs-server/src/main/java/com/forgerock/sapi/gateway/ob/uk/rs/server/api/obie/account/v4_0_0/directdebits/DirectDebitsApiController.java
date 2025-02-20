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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v4_0_0.directdebits;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v4_0_0.directdebits.DirectDebitsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRDirectDebit;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.directdebits.FRDirectDebitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.v4.account.OBReadDirectDebit2;
import uk.org.openbanking.datamodel.v4.account.OBReadDirectDebit2Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.account.FRDirectDebitConverter.toOBReadDirectDebit2DataDirectDebit;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller("DirectDebitsApiV4.0.0")
public class DirectDebitsApiController implements DirectDebitsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int pageLimitDirectDebits;

    private final FRDirectDebitRepository frDirectDebitRepository;

    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    private final AccountResourceAccessService accountResourceAccessService;

    public DirectDebitsApiController(@Value("${rs.page.default.direct-debits.size:10}") int pageLimitDirectDebits,
                                     FRDirectDebitRepository frDirectDebitRepository,
                                     AccountDataInternalIdFilter accountDataInternalIdFilter, @Qualifier("v4.0.0DefaultAccountResourceAccessService") AccountResourceAccessService accountResourceAccessService) {
        this.pageLimitDirectDebits = pageLimitDirectDebits;
        this.frDirectDebitRepository = frDirectDebitRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadDirectDebit2> getDirectDebits(String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, String consentId, int page) throws OBErrorException {
        logger.info("getDirectDebits for consentId: {}, apiClientId: {} ", consentId, apiClientId);

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkConsentHasRequiredPermission(consent);

        Page<FRDirectDebit> directDebits = frDirectDebitRepository.byAccountIdInWithPermissions(consent.getAuthorisedAccountIds(),
                consent.getRequestObj().getData().getPermissions(), PageRequest.of(page, pageLimitDirectDebits));
        return packageResponse(page, buildGetDirectDebitsUri(), directDebits);
    }

    @Override
    public ResponseEntity<OBReadDirectDebit2> getAccountsAccountIdDirectDebits(String accountId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, String consentId, int page) throws OBErrorException {
        logger.info("Read direct debits for account: {}, consentId: {}, apiClientId: {} ", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkConsentHasRequiredPermission(consent);
        Page<FRDirectDebit> directDebits = frDirectDebitRepository.byAccountIdWithPermissions(accountId, consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, pageLimitDirectDebits));
        return packageResponse(page, buildGetAccountDirectDebitsUri(accountId), directDebits);
    }

    private static void checkConsentHasRequiredPermission(AccountAccessConsent consent) throws OBErrorException {
        final List<FRExternalPermissionsCode> permissions = consent.getRequestObj().getData().getPermissions();
        if (!permissions.contains(FRExternalPermissionsCode.READDIRECTDEBITS)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, FRExternalPermissionsCode.READDIRECTDEBITS);
        }
    }

    private String buildGetDirectDebitsUri() {
        return linkTo(getClass()).slash("direct-debits").toString();
    }

    private String buildGetAccountDirectDebitsUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("direct-debits").toString();
    }

    private ResponseEntity<OBReadDirectDebit2> packageResponse(int page, String httpUrl, Page<FRDirectDebit> directDebits) {
        int totalPages = directDebits.getTotalPages();

        return ResponseEntity.ok(new OBReadDirectDebit2()
                .data(new OBReadDirectDebit2Data().directDebit(directDebits.getContent()
                        .stream()
                        .map(dd -> toOBReadDirectDebit2DataDirectDebit(dd.getDirectDebit()))
                        .map(accountDataInternalIdFilter::apply)
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

}
