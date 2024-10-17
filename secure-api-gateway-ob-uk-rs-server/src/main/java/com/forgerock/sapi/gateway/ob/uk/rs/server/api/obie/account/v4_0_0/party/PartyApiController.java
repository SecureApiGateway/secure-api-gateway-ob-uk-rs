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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v4_0_0.party;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v4_0_0.party.PartyApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.v4.account.FRParty;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.v4.accounts.party.FRPartyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.v4.account.*;

import java.util.ArrayList;
import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.account.FRPartyConverter.toOBParty2;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller("PartyApiV4.0.0")
public class PartyApiController implements PartyApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final FRPartyRepository frPartyRepository;

    private final AccountResourceAccessService accountResourceAccessService;

    public PartyApiController(FRPartyRepository frPartyRepository, @Qualifier("v4.0.0DefaultAccountResourceAccessService") AccountResourceAccessService accountResourceAccessService) {
        this.frPartyRepository = frPartyRepository;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadParty2> getParty(String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            String consentId
    ) throws OBErrorException {
        logger.info("getParty - consentId: {}, apiClientId: {}", consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkConsentHasRequiredPermission(consent, FRExternalPermissionsCode.READPARTYPSU);
        final String resourceOwnerId = consent.getResourceOwnerId();
        FRParty party = frPartyRepository.byUserIdWithPermissions(resourceOwnerId, consent.getRequestObj().getData().getPermissions());
        int totalPages = 1;

        return ResponseEntity.ok(new OBReadParty2()
                .data(new OBReadParty2Data()
                        .party(toOBParty2(party.getParty())))
                .links(PaginationUtil.generateLinks(buildGetPartyUri(), 0, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadParty3> getAccountsAccountIdParties(String accountId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, String consentId) throws OBErrorException {
        logger.info("getAccountParties - accountId: {}, consentId: {}, apiClientId: {}", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkConsentHasRequiredPermission(consent, FRExternalPermissionsCode.READPARTY);
        final List<FRExternalPermissionsCode> permissions = consent.getRequestObj().getData().getPermissions();
        FRParty accountParty = frPartyRepository.byAccountIdWithPermissions(accountId, permissions);
        List<OBParty2> parties = new ArrayList<>();
        if (accountParty != null) {
            logger.debug("Found account party '{}' for id: {}", accountId, accountId);
            parties.add(toOBParty2(accountParty.getParty()));
        }

        final String resourceOwnerId = consent.getResourceOwnerId();
        FRParty userParty = frPartyRepository.byUserIdWithPermissions(resourceOwnerId, permissions);
        if (userParty != null) {
            logger.debug("Found user party '{}' for id: {}", userParty, resourceOwnerId);
            parties.add(toOBParty2(userParty.getParty()));
        }

        int totalPages = 1;
        return ResponseEntity.ok(new OBReadParty3()
                .data(new OBReadParty3Data().party(parties))
                .links(PaginationUtil.generateLinks(buildGetAccountPartiesUri(accountId), 0, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadParty2> getAccountsAccountIdParty(String accountId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, String consentId) throws OBErrorException {
        logger.info("getAccountParty - accountId: {}, consentId: {}, apiClientId: {}", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkConsentHasRequiredPermission(consent, FRExternalPermissionsCode.READPARTY);
        FRParty party = frPartyRepository.byAccountIdWithPermissions(accountId, consent.getRequestObj().getData().getPermissions());
        int totalPages = 1;

        return ResponseEntity.ok(new OBReadParty2()
                .data(new OBReadParty2Data()
                        .party(toOBParty2(party.getParty())))
                .links(PaginationUtil.generateLinks(buildGetAccountPartyUri(accountId), 0, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    private String buildGetAccountPartyUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("party").toString();
    }

    private String buildGetAccountPartiesUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("parties").toString();
    }

    private String buildGetPartyUri() {
        return linkTo(getClass()).slash("party").toString();
    }

    private static void checkConsentHasRequiredPermission(AccountAccessConsent consent, FRExternalPermissionsCode permission) throws OBErrorException {
        if (!consent.getRequestObj().getData().getPermissions().contains(permission)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, permission.getValue());
        }
    }

}
