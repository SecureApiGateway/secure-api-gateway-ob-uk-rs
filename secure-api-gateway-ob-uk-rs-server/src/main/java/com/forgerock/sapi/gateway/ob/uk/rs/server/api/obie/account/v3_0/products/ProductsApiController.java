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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_0.products;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_0.products.ProductsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRProduct;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.products.FRProductRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.account.OBReadProduct2;
import uk.org.openbanking.datamodel.account.OBReadProduct2Data;

@Controller("ProductsApiV3.0")
@Slf4j
public class ProductsApiController implements ProductsApi {

    private final FRProductRepository frProductRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    private final AccountResourceAccessService accountResourceAccessService;

    @Value("${rs.page.default.products.size:10}")
    private int PAGE_LIMIT_PRODUCTS;

    public ProductsApiController(FRProductRepository frProductRepository,
                                 AccountDataInternalIdFilter accountDataInternalIdFilter,
                                 AccountResourceAccessService accountResourceAccessService) {
        this.frProductRepository = frProductRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadProduct2> getAccountProduct(String accountId,
                                                            int page,
                                                            String authorization,
                                                            DateTime xFapiCustomerLastLoggedTime,
                                                            String xFapiCustomerIpAddress,
                                                            String xFapiInteractionId,
                                                            String xCustomerUserAgent,
                                                            String consentId,
                                                            String apiClientId
    ) throws OBErrorException {
        log.info("getAccountProduct for accountId: {}, consentId: {}, apiClientId: {}", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        Page<FRProduct> products = frProductRepository.byAccountIdWithPermissions(accountId, consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, PAGE_LIMIT_PRODUCTS));

        int totalPage = products.getTotalPages();

        return ResponseEntity.ok(new OBReadProduct2()
                .data(new OBReadProduct2Data().product(products.getContent().stream()
                        .map(p -> accountDataInternalIdFilter.apply(p.getProduct()))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(buildGetAccountProductsUri(accountId), page, totalPage))
                .meta(PaginationUtil.generateMetaData(totalPage)));
    }

    @Override
    public ResponseEntity<OBReadProduct2> getProducts(int page,
                                                      String authorization,
                                                      DateTime xFapiCustomerLastLoggedTime,
                                                      String xFapiCustomerIpAddress,
                                                      String xFapiInteractionId,
                                                      String xCustomerUserAgent,
                                                      String consentId,
                                                      String apiClientId
    ) throws OBErrorException {
        log.info("getProducts for consentId: {}, apiClientId: {}", consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkPermissions(consent);
        Page<FRProduct> products = frProductRepository.byAccountIdInWithPermissions(consent.getAuthorisedAccountIds(), consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, PAGE_LIMIT_PRODUCTS));

        int totalPage = products.getTotalPages();

        return ResponseEntity.ok(new OBReadProduct2()
                .data(new OBReadProduct2Data().product(products.getContent().stream()
                        .map(p -> accountDataInternalIdFilter.apply(p.getProduct()))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(buildGetProductsUri(), page, totalPage))
                .meta(PaginationUtil.generateMetaData(totalPage)));
    }

    private String buildGetAccountProductsUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("product").toString();
    }

    private String buildGetProductsUri() {
        return linkTo(getClass()).slash("products").toString();
    }

    private static void checkPermissions(AccountAccessConsent consent) throws OBErrorException {
        if (!consent.getRequestObj().getData().getPermissions().contains(FRExternalPermissionsCode.READPRODUCTS)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, FRExternalPermissionsCode.READPRODUCTS.getValue());
        }
    }
}
