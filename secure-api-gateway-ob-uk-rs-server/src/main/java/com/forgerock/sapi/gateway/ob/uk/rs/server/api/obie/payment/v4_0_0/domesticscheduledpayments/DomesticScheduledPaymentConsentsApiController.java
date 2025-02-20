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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.domesticscheduledpayments;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteDomesticScheduledConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.domesticscheduledpayments.DomesticScheduledPaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0.OBWriteDomesticScheduledConsentResponse5Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticscheduled.DomesticScheduledPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.CreateDomesticScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;

import jakarta.servlet.http.HttpServletRequest;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduledConsentResponse5;

@Controller("DomesticScheduledPaymentConsentsApiV4.0.0")
public class DomesticScheduledPaymentConsentsApiController implements DomesticScheduledPaymentConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticScheduledPaymentConsentStoreClient consentStoreApiClient;

    private final OBValidationService<OBWriteDomesticScheduledConsent4> consentValidator;

    private final OBWriteDomesticScheduledConsentResponse5Factory consentResponseFactory;

    public DomesticScheduledPaymentConsentsApiController(
            @Qualifier("v4.0.0RestDomesticScheduledPaymentConsentStoreClient") DomesticScheduledPaymentConsentStoreClient consentStoreApiClient,
            OBValidationService<OBWriteDomesticScheduledConsent4> consentValidator,
            OBWriteDomesticScheduledConsentResponse5Factory consentResponseFactory) {

        this.consentStoreApiClient = consentStoreApiClient;
        this.consentValidator = consentValidator;
        this.consentResponseFactory = consentResponseFactory;
    }

    @Override
    public ResponseEntity<OBWriteDomesticScheduledConsentResponse5> createDomesticScheduledPaymentConsents(
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            OBWriteDomesticScheduledConsent4 obWriteDomesticScheduledConsent4,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {

        logger.trace("Processing createDomesticScheduledPaymentConsents request - consent: {}, idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                obWriteDomesticScheduledConsent4, xIdempotencyKey, apiClientId, xFapiInteractionId);

        consentValidator.validate(obWriteDomesticScheduledConsent4);

        final CreateDomesticScheduledPaymentConsentRequest createRequest = new CreateDomesticScheduledPaymentConsentRequest();
        createRequest.setConsentRequest(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledConsent(obWriteDomesticScheduledConsent4));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteDomesticScheduledConsent4));

        final DomesticScheduledPaymentConsent consent = consentStoreApiClient.createConsent(createRequest);
        logger.trace("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<FRCharge> calculateCharges(OBWriteDomesticScheduledConsent4 obWriteDomesticScheduledConsent4) {
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<OBWriteDomesticScheduledConsentResponse5> getDomesticScheduledPaymentConsentsConsentId(
            String consentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {

        logger.trace("Processing getDomesticScheduledPaymentConsentsConsentId request - consentId: {}, apiClient: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }
}
