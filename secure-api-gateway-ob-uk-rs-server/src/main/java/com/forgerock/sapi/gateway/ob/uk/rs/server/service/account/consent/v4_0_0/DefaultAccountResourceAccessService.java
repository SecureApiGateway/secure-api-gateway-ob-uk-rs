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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.v4_0_0;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.account.AccountAccessConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.v3.common.OBExternalRequestStatus1Code;

@Service("v4.0.0DefaultAccountResourceAccessService")
public class DefaultAccountResourceAccessService implements AccountResourceAccessService {

    private final AccountAccessConsentStoreClient consentStoreClient;

    public DefaultAccountResourceAccessService(@Qualifier("v4.0.0RestAccountAccessConsentStoreClient") AccountAccessConsentStoreClient consentStoreClient) {
        this.consentStoreClient = consentStoreClient;
    }

    @Override
    public AccountAccessConsent getConsentForResourceAccess(String consentId, String apiClientId) throws OBErrorException {
        final AccountAccessConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        if (!consent.getStatus().equals(OBExternalRequestStatus1Code.AUTHORISED.toString())) {
            throw new OBErrorException(OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED, consent.getStatus());
        }
        return consent;
    }

    @Override
    public AccountAccessConsent getConsentForResourceAccess(String consentId, String apiClientId, String accountIdToAccess) throws OBErrorException {
        final AccountAccessConsent consent = getConsentForResourceAccess(consentId, apiClientId);
        if (!consent.getAuthorisedAccountIds().contains(accountIdToAccess)) {
            throw new OBErrorException(OBRIErrorType.UNAUTHORISED_ACCOUNT, accountIdToAccess);
        }
        return consent;
    }

}
