/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v4_0_0;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.funds.FRFundsConfirmationConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.funds.v4_0_0.FundsConfirmationConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.factory.v4_0_0.OBFundsConfirmationConsentResponseFactory;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.FundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.CreateFundsConfirmationConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.v4.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.v4.fund.OBFundsConfirmationConsentResponse1;

@Controller("FundsConfirmationConsentsApiV4.0.0")
@Slf4j
public class FundsConfirmationConsentsApiController implements FundsConfirmationConsentsApi {

    private final FundsConfirmationConsentStoreClient fundsConfirmationConsentStoreClient;

    private final OBFundsConfirmationConsentResponseFactory obFundsConfirmationConsentResponseFactory;

    public FundsConfirmationConsentsApiController(@Qualifier("v4.0.0RestFundsConfirmationConsentStoreClient") FundsConfirmationConsentStoreClient fundsConfirmationConsentStoreClient, OBFundsConfirmationConsentResponseFactory obFundsConfirmationConsentResponseFactory) {
        this.fundsConfirmationConsentStoreClient = fundsConfirmationConsentStoreClient;
        this.obFundsConfirmationConsentResponseFactory = obFundsConfirmationConsentResponseFactory;
    }

    @Override
    public ResponseEntity<OBFundsConfirmationConsentResponse1> createFundsConfirmationConsents(String authorization, OBFundsConfirmationConsent1 obFundsConfirmationConsent1, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) {
        final CreateFundsConfirmationConsentRequest createConsentRequest = new CreateFundsConfirmationConsentRequest();
        createConsentRequest.setApiClientId(apiClientId);
        createConsentRequest.setConsentRequest(FRFundsConfirmationConsentConverter.toFRFundsConfirmationConsent(obFundsConfirmationConsent1));
        log.info("CreateFundsConfirmationConsentRequest: {}", createConsentRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(obFundsConfirmationConsentResponseFactory.buildConsentResponse(
                        fundsConfirmationConsentStoreClient.createConsent(createConsentRequest),
                        getClass()
                ));
    }

    @Override
    public ResponseEntity<Void> deleteFundsConfirmationConsentsConsentId(String consentId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) {
        log.info("Attempting to deleteFundsConfirmationConsents - consentId: {}, apiClientId: {}", consentId, apiClientId);
        fundsConfirmationConsentStoreClient.deleteConsent(consentId, apiClientId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<OBFundsConfirmationConsentResponse1> getFundsConfirmationConsentsConsentId(String consentId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) {
        log.info("Attempting to getFundsConfirmationConsentsConsent - consentId: {}, apiClientId: {}", consentId, apiClientId);
        final FundsConfirmationConsent consent = fundsConfirmationConsentStoreClient.getConsent(consentId, apiClientId);
        return ResponseEntity.ok(obFundsConfirmationConsentResponseFactory.buildConsentResponse(consent, getClass()));
    }
}
