/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_1_1.standingorders;

import com.forgerock.securebanking.openbanking.uk.rs.common.util.AccountDataInternalIdFilter;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.PaginationUtil;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRStandingOrderConverter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRStandingOrder;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.standingorders.FRStandingOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadStandingOrder5;
import uk.org.openbanking.datamodel.account.OBReadStandingOrder5Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;

@Controller("StandingOrdersApiV3.1.1")
@Slf4j
public class StandingOrdersApiController implements StandingOrdersApi {
    @Value("${rs.page.default.standing-order.size:10}")
    private int PAGE_LIMIT_STANDING_ORDERS;

    private final FRStandingOrderRepository frStandingOrderRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    public StandingOrdersApiController(FRStandingOrderRepository frStandingOrderRepository, AccountDataInternalIdFilter accountDataInternalIdFilter) {
        this.frStandingOrderRepository = frStandingOrderRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
    }

    @Override
    public ResponseEntity<OBReadStandingOrder5> getAccountStandingOrders(String accountId,
                                                                         int page,
                                                                         String xFapiFinancialId,
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
        Page<FRStandingOrder> standingOrders = frStandingOrderRepository.byAccountIdWithPermissions(
                accountId,
                toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_STANDING_ORDERS));

        int totalPages = standingOrders.getTotalPages();

        return ResponseEntity.ok(new OBReadStandingOrder5()
                .data(new OBReadStandingOrder5Data().standingOrder(standingOrders.getContent()
                        .stream()
                        .map(FRStandingOrder::getStandingOrder)
                        .map(FRStandingOrderConverter::toOBStandingOrder5)
                        .map(so -> accountDataInternalIdFilter.apply(so))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadStandingOrder5> getStandingOrders(String xFapiFinancialId,
                                                                  int page,
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
        Page<FRStandingOrder> standingOrders = frStandingOrderRepository.byAccountIdInWithPermissions(
                accountIds,
                toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_STANDING_ORDERS));
        int totalPages = standingOrders.getTotalPages();

        return ResponseEntity.ok(new OBReadStandingOrder5()
                .data(new OBReadStandingOrder5Data().standingOrder(standingOrders.getContent().stream()
                        .map(FRStandingOrder::getStandingOrder)
                        .map(FRStandingOrderConverter::toOBStandingOrder5)
                        .map(so -> accountDataInternalIdFilter.apply(so))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }
}
