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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_5.standingorders;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRStandingOrderConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_5.standingorders.StandingOrdersApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStandingOrder;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.standingorders.FRStandingOrderRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.account.OBReadStandingOrder6;
import uk.org.openbanking.datamodel.account.OBReadStandingOrder6Data;

@Controller("StandingOrdersApiV3.1.5")
@Slf4j
public class StandingOrdersApiController implements StandingOrdersApi {

    private final int pageLimitStandingOrders;

    private final FRStandingOrderRepository frStandingOrderRepository;

    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    private final AccountResourceAccessService accountResourceAccessService;

    public StandingOrdersApiController(@Value("${rs.page.default.standing-order.size:10}") int pageLimitStandingOrders,
                                       FRStandingOrderRepository frStandingOrderRepository,
                                       AccountDataInternalIdFilter accountDataInternalIdFilter,
                                       AccountResourceAccessService accountResourceAccessService) {
        this.pageLimitStandingOrders = pageLimitStandingOrders;
        this.frStandingOrderRepository = frStandingOrderRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadStandingOrder6> getAccountStandingOrders(String accountId,
                                                                         int page,
                                                                         String authorization,
                                                                         DateTime xFapiAuthDate,
                                                                         String xFapiCustomerIpAddress,
                                                                         String xFapiInteractionId,
                                                                         String xCustomerUserAgent,
                                                                         String consentId,
                                                                         String apiClientId) throws OBErrorException {
        log.info("getAccountStandingOrders for accountId: {}, consentId: {}, apiClientId: {}",
                accountId, consentId, apiClientId);

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkPermissions(consent);
        Page<FRStandingOrder> standingOrders =
                frStandingOrderRepository.byAccountIdWithPermissions(accountId, consent.getRequestObj().getData().getPermissions(),
                        PageRequest.of(page, pageLimitStandingOrders));

        int totalPages = standingOrders.getTotalPages();

        return ResponseEntity.ok(new OBReadStandingOrder6()
                .data(new OBReadStandingOrder6Data().standingOrder(standingOrders.getContent()
                        .stream()
                        .map(FRStandingOrder::getStandingOrder)
                        .map(FRStandingOrderConverter::toOBStandingOrder6)
                        .map(so -> accountDataInternalIdFilter.apply(so))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(buildGetAccountStandingOrdersUri(accountId), page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadStandingOrder6> getStandingOrders(int page,
                                                                  String authorization,
                                                                  DateTime xFapiAuthDate,
                                                                  String xFapiCustomerIpAddress,
                                                                  String xFapiInteractionId,
                                                                  String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {
        log.info("getStandingOrders for consentId: {}, apiClientId: {}", consentId, apiClientId);

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkPermissions(consent);

        Page<FRStandingOrder> standingOrders = frStandingOrderRepository.byAccountIdInWithPermissions(consent.getAuthorisedAccountIds(), consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, pageLimitStandingOrders));
        int totalPages = standingOrders.getTotalPages();

        return ResponseEntity.ok(new OBReadStandingOrder6()
                .data(new OBReadStandingOrder6Data().standingOrder(standingOrders.getContent().stream()
                        .map(FRStandingOrder::getStandingOrder)
                        .map(FRStandingOrderConverter::toOBStandingOrder6)
                        .map(so -> accountDataInternalIdFilter.apply(so))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(buildGetStandingOrderUri(), page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    private String buildGetAccountStandingOrdersUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("standing-orders").toString();
    }

    private String buildGetStandingOrderUri() {
        return linkTo(getClass()).slash("standing-orders").toString();
    }

    private static void checkPermissions(AccountAccessConsent consent) throws OBErrorException {
        final List<FRExternalPermissionsCode> permissions = consent.getRequestObj().getData().getPermissions();
        if (!permissions.contains(FRExternalPermissionsCode.READSTANDINGORDERSBASIC) && !permissions.contains(FRExternalPermissionsCode.READSTANDINGORDERSDETAIL)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, FRExternalPermissionsCode.READSTANDINGORDERSBASIC + " or " + FRExternalPermissionsCode.READSTANDINGORDERSDETAIL);
        }
    }
}
