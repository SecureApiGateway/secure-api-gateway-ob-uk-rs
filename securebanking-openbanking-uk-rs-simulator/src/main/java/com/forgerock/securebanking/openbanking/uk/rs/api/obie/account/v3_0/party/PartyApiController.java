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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_0.party;

import com.forgerock.securebanking.openbanking.uk.rs.common.util.PaginationUtil;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRParty;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.party.FRPartyRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadParty1;
import uk.org.openbanking.datamodel.account.OBReadParty1Data;

import java.util.List;

import static com.forgerock.securebanking.openbanking.uk.rs.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.account.FRPartyConverter.toOBParty1;

@Controller("PartyApiV3.0")
@Slf4j
public class PartyApiController implements PartyApi {

    private final FRPartyRepository frPartyRepository;

    public PartyApiController(FRPartyRepository frPartyRepository) {
        this.frPartyRepository = frPartyRepository;
    }

    @Override
    public ResponseEntity<OBReadParty1> getAccountParty(String accountId,
                                                        String xFapiFinancialId,
                                                        String authorization,
                                                        DateTime xFapiCustomerLastLoggedTime,
                                                        String xFapiCustomerIpAddress,
                                                        String xFapiInteractionId,
                                                        String xCustomerUserAgent,
                                                        List<OBExternalPermissions1Code> permissions,
                                                        String httpUrl
    ) {
        log.info("Read party for account {} with minimumPermissions {}", accountId, permissions);
        FRParty party = frPartyRepository.byAccountIdWithPermissions(accountId, toFRExternalPermissionsCodeList(permissions));
        int totalPages = 1;

        return ResponseEntity.ok(new OBReadParty1().data(new OBReadParty1Data().party(
                toOBParty1(party.getParty())))
                .links(PaginationUtil.generateLinks(httpUrl, 0, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadParty1> getParty(String xFapiFinancialId,
                                                 String authorization,
                                                 DateTime xFapiCustomerLastLoggedTime,
                                                 String xFapiCustomerIpAddress,
                                                 String xFapiInteractionId,
                                                 String xCustomerUserAgent,
                                                 String userId,
                                                 List<OBExternalPermissions1Code> permissions,
                                                 String httpUrl
    ) {
        log.info("Reading party from user id {}", userId);
        FRParty party = frPartyRepository.byUserIdWithPermissions(userId, toFRExternalPermissionsCodeList(permissions));
        int totalPages = 1;

        return ResponseEntity.ok(new OBReadParty1().data(new OBReadParty1Data().party(
                toOBParty1(party.getParty())))
                .links(PaginationUtil.generateLinks(httpUrl, 0, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }
}
