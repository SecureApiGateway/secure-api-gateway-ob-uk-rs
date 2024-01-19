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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticstandingorders;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticStandingOrderConsentConverter.toFRWriteDomesticStandingOrderConsent;

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
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.domesticstandingorders.DomesticStandingOrderConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.OBWriteDomesticStandingOrderConsentResponse6Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.CreateDomesticStandingOrderConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderConsentResponse6;

@Controller("DomesticStandingOrdersConsentsApiV3.1.10")
public class DomesticStandingOrderConsentsApiController implements DomesticStandingOrderConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticStandingOrderConsentStoreClient consentStoreApiClient;

    private final OBValidationService<OBWriteDomesticStandingOrderConsent5> consentValidator;

    private final OBWriteDomesticStandingOrderConsentResponse6Factory consentResponseFactory;

    public DomesticStandingOrderConsentsApiController(DomesticStandingOrderConsentStoreClient consentStoreApiClient,
            OBValidationService<OBWriteDomesticStandingOrderConsent5> consentValidator,
            OBWriteDomesticStandingOrderConsentResponse6Factory consentResponseFactory) {

        this.consentStoreApiClient = consentStoreApiClient;
        this.consentValidator = consentValidator;
        this.consentResponseFactory = consentResponseFactory;
    }

    @Override
    public ResponseEntity<OBWriteDomesticStandingOrderConsentResponse6> createDomesticStandingOrderConsents(OBWriteDomesticStandingOrderConsent5 obWriteDomesticStandingOrderConsent5,
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

        logger.info("Processing createDomesticStandingOrderConsents request - consent: {}, idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                obWriteDomesticStandingOrderConsent5, xIdempotencyKey, apiClientId, xFapiInteractionId);

        consentValidator.validate(obWriteDomesticStandingOrderConsent5);

        final CreateDomesticStandingOrderConsentRequest createRequest = new CreateDomesticStandingOrderConsentRequest();
        createRequest.setConsentRequest(toFRWriteDomesticStandingOrderConsent(obWriteDomesticStandingOrderConsent5));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteDomesticStandingOrderConsent5));

        final DomesticStandingOrderConsent consent = consentStoreApiClient.createConsent(createRequest);
        logger.info("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<FRCharge> calculateCharges(OBWriteDomesticStandingOrderConsent5 obWriteDomesticStandingOrderConsent5) {
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<OBWriteDomesticStandingOrderConsentResponse6> getDomesticStandingOrderConsentsConsentId(String consentId,
                                                                                                                  String authorization,
                                                                                                                  DateTime xFapiAuthDate,
                                                                                                                  String xFapiCustomerIpAddress,
                                                                                                                  String xFapiInteractionId,
                                                                                                                  String xCustomerUserAgent,
                                                                                                                  String apiClientId,
                                                                                                                  HttpServletRequest request,
                                                                                                                  Principal principal) throws OBErrorResponseException {

        logger.info("Processing getDomesticStandingOrderConsentsConsentId request - consentId: {}, apiClient: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }
}
