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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_1_5.beneficiaries;

import com.forgerock.securebanking.openbanking.uk.rs.common.util.AccountDataInternalIdFilter;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.PaginationUtil;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRBeneficiary;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.beneficiaries.FRBeneficiaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadBeneficiary5;
import uk.org.openbanking.datamodel.account.OBReadBeneficiary5Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRAccountBeneficiaryConverter.toOBBeneficiary5;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;

@Controller("BeneficiariesApiV3.1.5")
@Slf4j
public class BeneficiariesApiController implements BeneficiariesApi {

    private final int pageLimitBeneficiaries;

    private final FRBeneficiaryRepository frBeneficiaryRepository;

    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    public BeneficiariesApiController(@Value("${rs.page.default.beneficiaries.size:50}") int pageLimitBeneficiaries,
                                      FRBeneficiaryRepository frBeneficiaryRepository,
                                      AccountDataInternalIdFilter accountDataInternalIdFilter) {
        this.pageLimitBeneficiaries = pageLimitBeneficiaries;
        this.frBeneficiaryRepository = frBeneficiaryRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
    }

    @Override
    public ResponseEntity<OBReadBeneficiary5> getAccountBeneficiaries(String accountId,
                                                                      int page,
                                                                      String authorization,
                                                                      DateTime xFapiAuthDate,
                                                                      String xFapiCustomerIpAddress,
                                                                      String xFapiInteractionId,
                                                                      String xCustomerUserAgent,
                                                                      List<OBExternalPermissions1Code> permissions,
                                                                      String httpUrl) {
        log.info("Read beneficiaries for account {} with minimumPermissions {}", accountId, permissions);

        Page<FRBeneficiary> beneficiaries = frBeneficiaryRepository.byAccountIdWithPermissions(accountId, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, pageLimitBeneficiaries));
        return packageResponse(page, httpUrl, beneficiaries);
    }

    @Override
    public ResponseEntity<OBReadBeneficiary5> getBeneficiaries(int page,
                                                               String authorization,
                                                               DateTime xFapiAuthDate,
                                                               String xFapiCustomerIpAddress,
                                                               String xFapiInteractionId,
                                                               String xCustomerUserAgent,
                                                               List<String> accountIds,
                                                               List<OBExternalPermissions1Code> permissions,
                                                               String httpUrl) {
        log.info("Beneficiaries from account ids {}", accountIds);

        Page<FRBeneficiary> beneficiaries = frBeneficiaryRepository.byAccountIdInWithPermissions(accountIds, toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, pageLimitBeneficiaries));
        return packageResponse(page, httpUrl, beneficiaries);
    }

    private ResponseEntity<OBReadBeneficiary5> packageResponse(int page, String httpUrl, Page<FRBeneficiary> beneficiaries) {
        int totalPages = beneficiaries.getTotalPages();

        return ResponseEntity.ok(new OBReadBeneficiary5().data(new OBReadBeneficiary5Data().beneficiary(
                beneficiaries.getContent()
                        .stream()
                        .map(b -> toOBBeneficiary5(b.getBeneficiary()))
                        .map(b -> accountDataInternalIdFilter.apply(b))
                        .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }
}
