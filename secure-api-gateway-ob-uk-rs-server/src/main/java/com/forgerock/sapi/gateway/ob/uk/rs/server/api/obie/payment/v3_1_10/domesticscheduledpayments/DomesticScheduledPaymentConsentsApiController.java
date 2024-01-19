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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticscheduledpayments;

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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.domesticscheduledpayments.DomesticScheduledPaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.OBWriteDomesticScheduledConsentResponse5Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.CreateDomesticScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticscheduled.v3_1_10.DomesticScheduledPaymentConsent;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsentResponse5;

@Controller("DomesticScheduledPaymentConsentsApiV3.1.10")
public class DomesticScheduledPaymentConsentsApiController implements DomesticScheduledPaymentConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticScheduledPaymentConsentStoreClient consentStoreApiClient;

    private final OBValidationService<OBWriteDomesticScheduledConsent4> consentValidator;

    private final OBWriteDomesticScheduledConsentResponse5Factory consentResponseFactory;


    public DomesticScheduledPaymentConsentsApiController(DomesticScheduledPaymentConsentStoreClient consentStoreApiClient,
                                                         OBValidationService<OBWriteDomesticScheduledConsent4> consentValidator,
                                                         OBWriteDomesticScheduledConsentResponse5Factory consentResponseFactory) {

        this.consentStoreApiClient = consentStoreApiClient;
        this.consentValidator = consentValidator;
        this.consentResponseFactory = consentResponseFactory;
    }

    @Override
    public ResponseEntity<OBWriteDomesticScheduledConsentResponse5> createDomesticScheduledPaymentConsents(OBWriteDomesticScheduledConsent4 obWriteDomesticScheduledConsent4,
                                                                                                           String authorization,
                                                                                                           String xIdempotencyKey,
                                                                                                           String xJwsSignature,
                                                                                                           DateTime xFapiAuthDate,
                                                                                                           String xFapiCustomerIpAddress,
                                                                                                           String xFapiInteractionId,
                                                                                                           String xCustomerUserAgent,
                                                                                                           String apiClientId,
                                                                                                           HttpServletRequest request,
                                                                                                           Principal principal) throws OBErrorResponseException {
        logger.info("Processing createDomesticScheduledPaymentConsents request - consent: {}, idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                obWriteDomesticScheduledConsent4, xIdempotencyKey, apiClientId, xFapiInteractionId);

        consentValidator.validate(obWriteDomesticScheduledConsent4);

        final CreateDomesticScheduledPaymentConsentRequest createRequest = new CreateDomesticScheduledPaymentConsentRequest();
        createRequest.setConsentRequest(FRWriteDomesticScheduledConsentConverter.toFRWriteDomesticScheduledConsent(obWriteDomesticScheduledConsent4));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteDomesticScheduledConsent4));

        final DomesticScheduledPaymentConsent consent = consentStoreApiClient.createConsent(createRequest);
        logger.info("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<FRCharge> calculateCharges(OBWriteDomesticScheduledConsent4 obWriteDomesticScheduledConsent4) {
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<OBWriteDomesticScheduledConsentResponse5> getDomesticScheduledPaymentConsentsConsentId(String consentId,
                                                                                                                 String authorization,
                                                                                                                 DateTime xFapiAuthDate,
                                                                                                                 String xFapiCustomerIpAddress,
                                                                                                                 String xFapiInteractionId,
                                                                                                                 String xCustomerUserAgent,
                                                                                                                 String apiClientId,
                                                                                                                 HttpServletRequest request,
                                                                                                                 Principal principal) throws OBErrorResponseException {

        logger.info("Processing getDomesticScheduledPaymentConsentsConsentId request - consentId: {}, apiClient: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }
}
