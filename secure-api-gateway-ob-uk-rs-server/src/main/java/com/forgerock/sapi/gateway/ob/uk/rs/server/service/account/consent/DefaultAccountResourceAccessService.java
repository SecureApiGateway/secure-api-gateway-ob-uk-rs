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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent;

import org.springframework.stereotype.Service;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.account.v3_1_10.AccountAccessConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;

import uk.org.openbanking.datamodel.common.OBExternalRequestStatus1Code;

@Service
public class DefaultAccountResourceAccessService implements AccountResourceAccessService {

    private final AccountAccessConsentStoreClient consentStoreClient;

    public DefaultAccountResourceAccessService(AccountAccessConsentStoreClient consentStoreClient) {
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
