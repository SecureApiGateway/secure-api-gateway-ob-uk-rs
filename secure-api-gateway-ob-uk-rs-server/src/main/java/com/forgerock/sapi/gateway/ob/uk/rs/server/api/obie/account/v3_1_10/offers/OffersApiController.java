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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.offers;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FROfferConverter.toOBReadOffer1DataOffer;
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
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.offers.OffersApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FROffer;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.offers.FROfferRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;

import uk.org.openbanking.datamodel.account.OBReadOffer1;
import uk.org.openbanking.datamodel.account.OBReadOffer1Data;

@Controller("OffersApiV3.1.10")
public class OffersApiController implements OffersApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final FROfferRepository frOfferRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;
    @Value("${rs.page.default.offers.size:10}")
    private int PAGE_LIMIT_OFFERS;

    private final AccountResourceAccessService accountResourceAccessService;

    public OffersApiController(FROfferRepository frOfferRepository,
            AccountDataInternalIdFilter accountDataInternalIdFilter,
            AccountResourceAccessService accountResourceAccessService) {
        this.frOfferRepository = frOfferRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadOffer1> getAccountOffers(String accountId,
            int page,
            String authorization,
            String xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {

        logger.info("getAccountOffers for accountId: {}, consentId: {}, apiClientId:{}", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkConsentHasRequiredPermission(consent);
        Page<FROffer> offers = frOfferRepository.byAccountIdWithPermissions(accountId, consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, PAGE_LIMIT_OFFERS));
        int totalPages = offers.getTotalPages();

        return ResponseEntity.ok(new OBReadOffer1().data(new OBReadOffer1Data().offer(
                        offers.getContent()
                                .stream()
                                .map(o -> toOBReadOffer1DataOffer(o.getOffer()))
                                .map(accountDataInternalIdFilter::apply)
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(buildGetAccountOffersUri(accountId), page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadOffer1> getOffers(int page,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {

        logger.info("getOffers for consentId: {}, apiClientId: {}", consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkConsentHasRequiredPermission(consent);
        Page<FROffer> offers = frOfferRepository.byAccountIdInWithPermissions(consent.getAuthorisedAccountIds(), consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, PAGE_LIMIT_OFFERS));
        int totalPages = offers.getTotalPages();

        return ResponseEntity.ok(new OBReadOffer1().data(new OBReadOffer1Data().offer(
                        offers.getContent()
                                .stream()
                                .map(o -> toOBReadOffer1DataOffer(o.getOffer()))
                                .map(accountDataInternalIdFilter::apply)
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(buildGetOfferUri(), page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    private String buildGetOfferUri() {
        return linkTo(getClass()).slash("offers").toString();
    }

    private String buildGetAccountOffersUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("offers").toString();
    }

    private static void checkConsentHasRequiredPermission(AccountAccessConsent consent) throws OBErrorException {
        if (!consent.getRequestObj().getData().getPermissions().contains(FRExternalPermissionsCode.READOFFERS)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, FRExternalPermissionsCode.READOFFERS.getValue());
        }
    }

}
