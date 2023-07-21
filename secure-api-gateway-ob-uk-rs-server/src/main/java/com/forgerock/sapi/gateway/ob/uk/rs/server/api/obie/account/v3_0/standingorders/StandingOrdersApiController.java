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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_0.standingorders;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStandingOrder;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.standingorders.FRStandingOrderRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_0.standingorders.StandingOrdersApi;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadStandingOrder3;
import uk.org.openbanking.datamodel.account.OBReadStandingOrder3Data;
import uk.org.openbanking.datamodel.account.OBStandingOrder3;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRStandingOrderConverter.toOBStandingOrder3;

@Controller("StandingOrdersApiV3.0")
@Slf4j
public class StandingOrdersApiController implements StandingOrdersApi {

    private final FRStandingOrderRepository frStandingOrderRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;
    @Value("${rs.page.default.standing-order.size:10}")
    private int PAGE_LIMIT_STANDING_ORDERS;

    public StandingOrdersApiController(FRStandingOrderRepository frStandingOrderRepository,
                                       AccountDataInternalIdFilter accountDataInternalIdFilter) {
        this.frStandingOrderRepository = frStandingOrderRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
    }

    @Override
    public ResponseEntity<OBReadStandingOrder3> getAccountStandingOrders(String accountId,
                                                                         int page,
                                                                         String authorization,
                                                                         DateTime xFapiCustomerLastLoggedTime,
                                                                         String xFapiCustomerIpAddress,
                                                                         String xFapiInteractionId,
                                                                         String xCustomerUserAgent,
                                                                         List<OBExternalPermissions1Code> permissions,
                                                                         String httpUrl
    ) {
        log.info("Read standing orders for account {} with minimumPermissions {}",
                accountId, permissions);
        Page<FRStandingOrder> standingOrdersResponse =
                frStandingOrderRepository.byAccountIdWithPermissions(accountId, toFRExternalPermissionsCodeList(permissions),
                        PageRequest.of(page, PAGE_LIMIT_STANDING_ORDERS));
        List<OBStandingOrder3> standingOrders = standingOrdersResponse.stream()
                .map(so -> toOBStandingOrder3(so.getStandingOrder()))
                .map(so -> accountDataInternalIdFilter.apply(so))
                .collect(Collectors.toList());

        int totalPages = standingOrdersResponse.getTotalPages();

        return ResponseEntity.ok(new OBReadStandingOrder3()
                .data(new OBReadStandingOrder3Data().standingOrder(standingOrders))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadStandingOrder3> getStandingOrders(int page,
                                                                  String authorization,
                                                                  DateTime xFapiCustomerLastLoggedTime,
                                                                  String xFapiCustomerIpAddress,
                                                                  String xFapiInteractionId,
                                                                  String xCustomerUserAgent,
                                                                  List<String> accountIds,
                                                                  List<OBExternalPermissions1Code> permissions,
                                                                  String httpUrl
    ) {
        log.info("Reading standing orders from account ids {}", accountIds);
        Page<FRStandingOrder> standingOrdersResponse = frStandingOrderRepository.byAccountIdInWithPermissions(accountIds, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_STANDING_ORDERS));
        List<OBStandingOrder3> standingOrders = standingOrdersResponse.stream()
                .map(so -> toOBStandingOrder3(so.getStandingOrder()))
                .map(so -> accountDataInternalIdFilter.apply(so))
                .collect(Collectors.toList());

        int totalPages = standingOrdersResponse.getTotalPages();

        return ResponseEntity.ok(new OBReadStandingOrder3()
                .data(new OBReadStandingOrder3Data().standingOrder(standingOrders))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }
}
