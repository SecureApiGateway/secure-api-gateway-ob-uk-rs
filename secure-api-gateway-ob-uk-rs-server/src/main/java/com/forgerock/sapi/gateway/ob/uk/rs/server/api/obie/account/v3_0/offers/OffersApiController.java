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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_0.offers;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FROffer;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.offers.FROfferRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_0.offers.OffersApi;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadOffer1;
import uk.org.openbanking.datamodel.account.OBReadOffer1Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FROfferConverter.toOBReadOffer1DataOffer;

@Controller("OffersApiV3.0")
@Slf4j
public class OffersApiController implements OffersApi {

    private final FROfferRepository frOfferRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;
    @Value("${rs.page.default.offers.size:10}")
    private int PAGE_LIMIT_OFFERS;

    public OffersApiController(FROfferRepository frOfferRepository,
                               AccountDataInternalIdFilter accountDataInternalIdFilter) {
        this.frOfferRepository = frOfferRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
    }

    @Override
    public ResponseEntity<OBReadOffer1> getAccountOffers(String accountId,
                                                         int page,
                                                         String authorization,
                                                         DateTime xFapiCustomerLastLoggedTime,
                                                         String xFapiCustomerIpAddress,
                                                         String xFapiInteractionId,
                                                         String xCustomerUserAgent,
                                                         List<OBExternalPermissions1Code> permissions,
                                                         String httpUrl
    ) {
        log.info("Read offers for account {} with minimumPermissions {}", accountId, permissions);
        Page<FROffer> offers = frOfferRepository.byAccountIdWithPermissions(accountId, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_OFFERS));
        int totalPages = offers.getTotalPages();

        return ResponseEntity.ok(new OBReadOffer1().data(new OBReadOffer1Data().offer(
                        offers.getContent()
                                .stream()
                                .map(o -> toOBReadOffer1DataOffer(o.getOffer()))
                                .map(accountDataInternalIdFilter::apply)
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadOffer1> getOffers(int page,
                                                  String authorization,
                                                  DateTime xFapiCustomerLastLoggedTime,
                                                  String xFapiCustomerIpAddress,
                                                  String xFapiInteractionId,
                                                  String xCustomerUserAgent,
                                                  List<String> accountIds,
                                                  List<OBExternalPermissions1Code> permissions,
                                                  String httpUrl
    ) {
        log.info("Reading offers from account ids {}", accountIds);
        Page<FROffer> offers = frOfferRepository.byAccountIdInWithPermissions(accountIds, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_OFFERS));
        int totalPages = offers.getTotalPages();

        return ResponseEntity.ok(new OBReadOffer1().data(new OBReadOffer1Data().offer(
                        offers.getContent()
                                .stream()
                                .map(o -> toOBReadOffer1DataOffer(o.getOffer()))
                                .map(accountDataInternalIdFilter::apply)
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }
}
