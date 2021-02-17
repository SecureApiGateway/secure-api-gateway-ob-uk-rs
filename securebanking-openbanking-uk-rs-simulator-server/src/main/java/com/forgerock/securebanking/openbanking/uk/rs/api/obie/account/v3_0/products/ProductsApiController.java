/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_0.products;

import com.forgerock.securebanking.openbanking.uk.rs.common.util.AccountDataInternalIdFilter;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.PaginationUtil;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRProduct;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.products.FRProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadProduct2;
import uk.org.openbanking.datamodel.account.OBReadProduct2Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.openbanking.uk.rs.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;

@Controller("ProductsApiV3.0")
@Slf4j
public class ProductsApiController implements ProductsApi {

    @Value("${rs.page.default.products.size:10}")
    private int PAGE_LIMIT_PRODUCTS;

    private final FRProductRepository frProductRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    public ProductsApiController(FRProductRepository frProductRepository,
                                 AccountDataInternalIdFilter accountDataInternalIdFilter) {
        this.frProductRepository = frProductRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
    }

    @Override
    public ResponseEntity<OBReadProduct2> getAccountProduct(String accountId,
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
        log.info("Read product for account {} with minimumPermissions {}", accountId, permissions);
        Page<FRProduct> products = frProductRepository.byAccountIdWithPermissions(accountId, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_PRODUCTS));

        int totalPage = products.getTotalPages();

        return ResponseEntity.ok(new OBReadProduct2()
                .data(new OBReadProduct2Data().product(products.getContent().stream()
                        .map(p -> accountDataInternalIdFilter.apply(p.getProduct()))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPage))
                .meta(PaginationUtil.generateMetaData(totalPage)));
    }

    @Override
    public ResponseEntity<OBReadProduct2> getProducts(String xFapiFinancialId,
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
        log.info("Reading products from account ids {}", accountIds);
        Page<FRProduct> products = frProductRepository.byAccountIdInWithPermissions(accountIds, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_PRODUCTS));

        int totalPage = products.getTotalPages();

        return ResponseEntity.ok(new OBReadProduct2()
                .data(new OBReadProduct2Data().product(products.getContent().stream()
                        .map(p -> accountDataInternalIdFilter.apply(p.getProduct()))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPage))
                .meta(PaginationUtil.generateMetaData(totalPage)));
    }
}
