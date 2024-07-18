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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.internationalstandingorders;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.internationalstandingorders.InternationalStandingOrderConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.OBWriteInternationalStandingOrderConsentResponse7Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.internationalstandingorder.v3_1_10.InternationalStandingOrderConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.CreateInternationalStandingOrderConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalstandingorder.v3_1_10.InternationalStandingOrderConsent;

import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsentResponse7;

@Controller("InternationalStandingOrderConsentsApiV3.1.10")
public class InternationalStandingOrderConsentsApiController implements InternationalStandingOrderConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InternationalStandingOrderConsentStoreClient consentStoreClient;

    private final OBValidationService<OBWriteInternationalStandingOrderConsent6> consentValidator;

    private final OBWriteInternationalStandingOrderConsentResponse7Factory consentResponseFactory;

    public InternationalStandingOrderConsentsApiController(InternationalStandingOrderConsentStoreClient consentStoreClient,
            OBValidationService<OBWriteInternationalStandingOrderConsent6> consentValidator,
            OBWriteInternationalStandingOrderConsentResponse7Factory consentResponseFactory) {
        this.consentStoreClient = consentStoreClient;
        this.consentValidator = consentValidator;
        this.consentResponseFactory = consentResponseFactory;
    }

    @Override
    public ResponseEntity<OBWriteInternationalStandingOrderConsentResponse7> createInternationalStandingOrderConsents(OBWriteInternationalStandingOrderConsent6 obWriteInternationalStandingOrderConsent6,
            String authorization, String xIdempotencyKey, String xJwsSignature, String xFapiAuthDate, String xFapiCustomerIpAddress,
            String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        logger.info("Processing createInternationalStandingOrderConsents request - consent: {}, idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                obWriteInternationalStandingOrderConsent6, xIdempotencyKey, apiClientId, xFapiInteractionId);

        consentValidator.validate(obWriteInternationalStandingOrderConsent6);

        final CreateInternationalStandingOrderConsentRequest createRequest = new CreateInternationalStandingOrderConsentRequest();
        createRequest.setConsentRequest(FRWriteInternationalStandingOrderConsentConverter.toFRWriteInternationalStandingOrderConsent(obWriteInternationalStandingOrderConsent6));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteInternationalStandingOrderConsent6));

        final InternationalStandingOrderConsent consent = consentStoreClient.createConsent(createRequest);
        logger.info("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<FRCharge> calculateCharges(OBWriteInternationalStandingOrderConsent6 obWriteInternationalStandingOrderConsent6) {
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<OBWriteInternationalStandingOrderConsentResponse7> getInternationalStandingOrderConsentsConsentId(String consentId,
            String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId,
            String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        logger.info("Processing getInternationalStandingOrderConsentsConsentId request - apiClient: {}, x-fapi-interaction-id: {}",
                apiClientId, xFapiInteractionId);

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreClient.getConsent(consentId, apiClientId), getClass()));
    }
}
