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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_1.beneficiaries;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRAccountBeneficiaryConverter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBeneficiary;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.beneficiaries.FRBeneficiaryRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_1.beneficiaries.BeneficiariesApi;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadBeneficiary3;
import uk.org.openbanking.datamodel.account.OBReadBeneficiary3Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;

@Controller("BeneficiariesApiV3.1.1")
@Slf4j
public class BeneficiariesApiController implements BeneficiariesApi {
    private final FRBeneficiaryRepository frBeneficiaryRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;
    @Value("${rs.page.default.beneficiaries.size:50}")
    private int PAGE_LIMIT_BENEFICIARIES;

    public BeneficiariesApiController(FRBeneficiaryRepository frBeneficiaryRepository,
                                      AccountDataInternalIdFilter accountDataInternalIdFilter) {
        this.frBeneficiaryRepository = frBeneficiaryRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
    }

    @Override
    public ResponseEntity<OBReadBeneficiary3> getAccountBeneficiaries(String accountId,
                                                                      int page,
                                                                      String authorization,
                                                                      DateTime xFapiCustomerLastLoggedTime,
                                                                      String xFapiCustomerIpAddress,
                                                                      String xFapiInteractionId,
                                                                      String xCustomerUserAgent,
                                                                      List<OBExternalPermissions1Code> permissions,
                                                                      String httpUrl
    ) {
        log.info("Read beneficiaries for account {} with minimumPermissions {}", accountId, permissions);
        Page<FRBeneficiary> beneficiaries = frBeneficiaryRepository.byAccountIdWithPermissions(accountId, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_BENEFICIARIES));
        int totalPages = beneficiaries.getTotalPages();

        return ResponseEntity.ok(new OBReadBeneficiary3().data(new OBReadBeneficiary3Data().beneficiary(
                        beneficiaries.getContent()
                                .stream()
                                .map(FRBeneficiary::getBeneficiary)
                                .map(FRAccountBeneficiaryConverter::toOBBeneficiary3)
                                .map(b -> accountDataInternalIdFilter.apply(b))
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadBeneficiary3> getBeneficiaries(int page,
                                                               String authorization,
                                                               DateTime xFapiCustomerLastLoggedTime,
                                                               String xFapiCustomerIpAddress,
                                                               String xFapiInteractionId,
                                                               String xCustomerUserAgent,
                                                               List<String> accountIds,
                                                               List<OBExternalPermissions1Code> permissions,
                                                               String httpUrl
    ) {
        log.info("Beneficiaries from account ids {}", accountIds);
        Page<FRBeneficiary> beneficiaries = frBeneficiaryRepository.byAccountIdInWithPermissions(accountIds, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_BENEFICIARIES));
        int totalPages = beneficiaries.getTotalPages();

        return ResponseEntity.ok(new OBReadBeneficiary3().data(new OBReadBeneficiary3Data().beneficiary(
                        beneficiaries.getContent()
                                .stream()
                                .map(FRBeneficiary::getBeneficiary)
                                .map(FRAccountBeneficiaryConverter::toOBBeneficiary3)
                                .map(b -> accountDataInternalIdFilter.apply(b))
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }
}
