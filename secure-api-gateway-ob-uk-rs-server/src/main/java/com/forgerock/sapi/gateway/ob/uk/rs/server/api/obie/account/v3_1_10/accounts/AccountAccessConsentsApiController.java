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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.accounts;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.account.FRReadConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.accounts.AccountAccessConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.factory.OBReadConsentResponseFactory;
import com.forgerock.sapi.gateway.rcs.conent.store.client.account.v3_1_10.AccountAccessConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.account.v3_1_10.CreateAccountAccessConsentRequest;

import uk.org.openbanking.datamodel.account.OBReadConsent1;
import uk.org.openbanking.datamodel.account.OBReadConsentResponse1;

@Controller("AccountAccessConsentsApiV3.1.10")
public class AccountAccessConsentsApiController implements AccountAccessConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AccountAccessConsentStoreClient accountAccessConsentStoreClient;

    private final OBReadConsentResponseFactory obReadConsentResponseFactory;

    public AccountAccessConsentsApiController(AccountAccessConsentStoreClient accountAccessConsentStoreClient, OBReadConsentResponseFactory obReadConsentResponseFactory) {
        this.accountAccessConsentStoreClient = accountAccessConsentStoreClient;
        this.obReadConsentResponseFactory = obReadConsentResponseFactory;
    }

    @Override
    public ResponseEntity<OBReadConsentResponse1> createAccountAccessConsents(OBReadConsent1 body, String authorization,
            DateTime xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent,
            String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        final CreateAccountAccessConsentRequest createConsentRequest = new CreateAccountAccessConsentRequest();
        createConsentRequest.setApiClientId(apiClientId);
        createConsentRequest.setConsentRequest(FRReadConsentConverter.toFRReadConsent(body));
        logger.info("CreateAccountAccessConsentRequest: {}", createConsentRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(obReadConsentResponseFactory.buildConsentResponse(accountAccessConsentStoreClient.createConsent(createConsentRequest), getClass()));
    }

    @Override
    public ResponseEntity<Void> deleteAccountAccessConsent(String consentId, String authorization, DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId,
            HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        logger.info("Attempting to deleteAccountAccessConsent - consentId: {}, apiClientId: {}", consentId, apiClientId);
        accountAccessConsentStoreClient.deleteConsent(consentId, apiClientId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<OBReadConsentResponse1> getAccountAccessConsent(String consentId, String authorization,
            DateTime xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent,
            String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        logger.info("Attempting to getAccountAccessConsent - consentId: {}, apiClientId: {}", consentId, apiClientId);

        final AccountAccessConsent consent = accountAccessConsentStoreClient.getConsent(consentId, apiClientId);
        return ResponseEntity.ok(obReadConsentResponseFactory.buildConsentResponse(consent, getClass()));
    }

}
