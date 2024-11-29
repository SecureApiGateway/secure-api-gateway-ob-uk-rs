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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.beneficiaries;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.account.FRAccountBeneficiaryConverter.toOBBeneficiary5;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.List;
import java.util.stream.Collectors;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRBeneficiary;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.beneficiaries.FRBeneficiaryRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.beneficiaries.BeneficiariesApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import uk.org.openbanking.datamodel.v3.account.OBReadBeneficiary5;
import uk.org.openbanking.datamodel.v3.account.OBReadBeneficiary5Data;

@Controller("BeneficiariesApiV3.1.10")
public class BeneficiariesApiController  implements BeneficiariesApi {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final int pageLimitBeneficiaries;

    private final FRBeneficiaryRepository frBeneficiaryRepository;

    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    private final AccountResourceAccessService accountResourceAccessService;

    public BeneficiariesApiController(@Value("${rs.page.default.beneficiaries.size:50}") int pageLimitBeneficiaries,
            FRBeneficiaryRepository frBeneficiaryRepository,
            AccountDataInternalIdFilter accountDataInternalIdFilter,
            @Qualifier("v3.1.10DefaultAccountResourceAccessService") AccountResourceAccessService accountResourceAccessService) {

        this.pageLimitBeneficiaries = pageLimitBeneficiaries;
        this.frBeneficiaryRepository = frBeneficiaryRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadBeneficiary5> getAccountBeneficiaries(String accountId,
            int page,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {

        logger.info("Read beneficiaries for account {}, consentId: {}, apiClientId: {}", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkConsentHasRequiredPermission(consent);

        Page<FRBeneficiary> beneficiaries = frBeneficiaryRepository.byAccountIdWithPermissions(accountId, consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, pageLimitBeneficiaries));
        return packageResponse(page, buildGetAccountBeneficiariesUri(accountId), beneficiaries);
    }

    private String buildGetBeneficiariesUri() {
        return linkTo(getClass()).slash("beneficiaries").toString();
    }

    private String buildGetAccountBeneficiariesUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("beneficiaries").toString();
    }

    @Override
    public ResponseEntity<OBReadBeneficiary5> getBeneficiaries(int page,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {

        logger.info("getBeneficiaries for consentId: {}, apiClientId: {}", consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkConsentHasRequiredPermission(consent);

        Page<FRBeneficiary> beneficiaries = frBeneficiaryRepository.byAccountIdInWithPermissions(consent.getAuthorisedAccountIds(),
                consent.getRequestObj().getData().getPermissions(), PageRequest.of(page, pageLimitBeneficiaries));
        return packageResponse(page, buildGetBeneficiariesUri(), beneficiaries);
    }

    private static void checkConsentHasRequiredPermission(AccountAccessConsent consent) throws OBErrorException {
        final List<FRExternalPermissionsCode> permissions = consent.getRequestObj().getData().getPermissions();
        if (!permissions.contains(FRExternalPermissionsCode.READBENEFICIARIESBASIC)
                && !permissions.contains(FRExternalPermissionsCode.READBENEFICIARIESDETAIL)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID,
                    List.of(FRExternalPermissionsCode.READBENEFICIARIESBASIC, FRExternalPermissionsCode.READBENEFICIARIESDETAIL));
        }
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
