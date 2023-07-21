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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_3.directdebits;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRDirectDebit;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.directdebits.FRDirectDebitRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_3.directdebits.DirectDebitsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import uk.org.openbanking.datamodel.account.OBReadDirectDebit2;
import uk.org.openbanking.datamodel.account.OBReadDirectDebit2Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRDirectDebitConverter.toOBReadDirectDebit2DataDirectDebit;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller("DirectDebitsApiV3.1.3")
@Slf4j
public class DirectDebitsApiController implements DirectDebitsApi {

    private final int pageLimitDirectDebits;

    private final FRDirectDebitRepository frDirectDebitRepository;

    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    private final AccountResourceAccessService accountResourceAccessService;

    public DirectDebitsApiController(@Value("${rs.page.default.direct-debits.size:10}") int pageLimitDirectDebits,
                                     FRDirectDebitRepository frDirectDebitRepository,
                                     AccountDataInternalIdFilter accountDataInternalIdFilter, AccountResourceAccessService accountResourceAccessService) {
        this.pageLimitDirectDebits = pageLimitDirectDebits;
        this.frDirectDebitRepository = frDirectDebitRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadDirectDebit2> getAccountDirectDebits(String accountId,
                                                                     int page,
                                                                     String authorization,
                                                                     DateTime xFapiAuthDate,
                                                                     String xFapiCustomerIpAddress,
                                                                     String xFapiInteractionId,
                                                                     String xCustomerUserAgent,
                                                                     String consentId,
                                                                     String apiClientId) throws OBErrorException {

        log.info("Read direct debits for account: {}, consentId: {}, apiClientId: {} ", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkConsentHasRequiredPermission(consent);
        Page<FRDirectDebit> directDebits = frDirectDebitRepository.byAccountIdWithPermissions(accountId, consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, pageLimitDirectDebits));
        return packageResponse(page, buildGetAccountDirectDebitsUri(accountId), directDebits);
    }

    @Override
    public ResponseEntity<OBReadDirectDebit2> getDirectDebits(int page,
                                                              String authorization,
                                                              DateTime xFapiAuthDate,
                                                              String xFapiCustomerIpAddress,
                                                              String xFapiInteractionId,
                                                              String xCustomerUserAgent,
                                                              String consentId,
                                                              String apiClientId) throws OBErrorException {

        log.info("getDirectDebits for consentId: {}, apiClientId: {} ", consentId, apiClientId);

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkConsentHasRequiredPermission(consent);

        Page<FRDirectDebit> directDebits = frDirectDebitRepository.byAccountIdInWithPermissions(consent.getAuthorisedAccountIds(),
                consent.getRequestObj().getData().getPermissions(), PageRequest.of(page, pageLimitDirectDebits));
        return packageResponse(page, buildGetDirectDebitsUri(), directDebits);
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
