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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v3_1_10;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.funds.v3_1_10.FundsConfirmationConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.factory.OBFundsConfirmationConsentResponseFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.v3_1_10.FundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.v3.fund.OBFundsConfirmationConsentResponse1;

@Controller("FundsConfirmationConsentsApiV3.1.10")
@Slf4j
public class FundsConfirmationConsentsApiController implements FundsConfirmationConsentsApi {

    private final FundsConfirmationConsentStoreClient fundsConfirmationConsentStoreClient;

    private final OBFundsConfirmationConsentResponseFactory obFundsConfirmationConsentResponseFactory;

    public FundsConfirmationConsentsApiController(FundsConfirmationConsentStoreClient fundsConfirmationConsentStoreClient, OBFundsConfirmationConsentResponseFactory obFundsConfirmationConsentResponseFactory) {
        this.fundsConfirmationConsentStoreClient = fundsConfirmationConsentStoreClient;
        this.obFundsConfirmationConsentResponseFactory = obFundsConfirmationConsentResponseFactory;
    }

    @Override
    public ResponseEntity<OBFundsConfirmationConsentResponse1> createFundsConfirmationConsent(
            OBFundsConfirmationConsent1 body,
            String authorization, String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        final CreateFundsConfirmationConsentRequest createConsentRequest = new CreateFundsConfirmationConsentRequest();
        createConsentRequest.setApiClientId(apiClientId);
        createConsentRequest.setConsentRequest(FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(body));
        log.info("CreateFundsConfirmationConsentRequest: {}", createConsentRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obFundsConfirmationConsentResponseFactory.buildConsentResponse(
                        fundsConfirmationConsentStoreClient.createConsent(createConsentRequest),
                        getClass()
                ));
    }

    @Override
    public ResponseEntity<OBFundsConfirmationConsentResponse1> getFundsConfirmationConsentsConsentId(
            String consentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.info("Attempting to getFundsConfirmationConsentsConsent - consentId: {}, apiClientId: {}", consentId, apiClientId);
        final FundsConfirmationConsent consent = fundsConfirmationConsentStoreClient.getConsent(consentId, apiClientId);
        return ResponseEntity.ok(obFundsConfirmationConsentResponseFactory.buildConsentResponse(consent, getClass()));
    }

    @Override
    public ResponseEntity deleteFundsConfirmationConsentsConsentId(
            String consentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.info("Attempting to deleteFundsConfirmationConsents - consentId: {}, apiClientId: {}", consentId, apiClientId);
        fundsConfirmationConsentStoreClient.deleteConsent(consentId, apiClientId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
