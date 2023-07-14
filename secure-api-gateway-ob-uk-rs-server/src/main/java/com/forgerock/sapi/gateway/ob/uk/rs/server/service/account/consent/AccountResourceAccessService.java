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

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;

/**
 * Service to retrieve Consents that are being used when attempting to access an accounts resource.
 *
 * Common permission checking will be done in this service to handle cases where Consents have not been Authorised etc
 */
public interface AccountResourceAccessService {

    AccountAccessConsent getConsentForResourceAccess(String consentId, String apiClientId) throws OBErrorException;

    AccountAccessConsent getConsentForResourceAccess(String consentId, String apiClientId, String accountIdToAccess) throws OBErrorException;
}
