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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.domesticpayments;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteDomesticConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.domesticpayments.DomesticPaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0.OBWriteDomesticConsentResponse5Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0.OBWriteFundsConfirmationResponse1Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domestic.DomesticPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;

import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsentResponse5;
import uk.org.openbanking.datamodel.v4.payment.OBWriteFundsConfirmationResponse1;

@Controller("DomesticPaymentConsentsApiV4.0.0")
public class DomesticPaymentConsentsApiController implements DomesticPaymentConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticPaymentConsentStoreClient consentStoreApiClient;

    private final OBValidationService<OBWriteDomesticConsent4> domesticConsentValidator;

    private final OBWriteDomesticConsentResponse5Factory consentResponseFactory;

    private final FundsAvailabilityService fundsAvailabilityService;

    private final OBWriteFundsConfirmationResponse1Factory fundsConfirmationResponseFactory;

    public DomesticPaymentConsentsApiController(@Qualifier("v4.0.0RestDomesticPaymentConsentStoreClient") DomesticPaymentConsentStoreClient consentStoreApiClient,
                                                OBValidationService<OBWriteDomesticConsent4> domesticConsentValidator,
                                                OBWriteDomesticConsentResponse5Factory consentResponseFactory,
                                                FundsAvailabilityService fundsAvailabilityService,
                                                OBWriteFundsConfirmationResponse1Factory fundsConfirmationResponseFactory) {

        this.consentStoreApiClient = consentStoreApiClient;
        this.domesticConsentValidator = domesticConsentValidator;
        this.consentResponseFactory = consentResponseFactory;
        this.fundsAvailabilityService = fundsAvailabilityService;
        this.fundsConfirmationResponseFactory = fundsConfirmationResponseFactory;
    }

    @Override
    public ResponseEntity<OBWriteDomesticConsentResponse5> createDomesticPaymentConsents(String authorization, String xIdempotencyKey, String xJwsSignature, OBWriteDomesticConsent4 obWriteDomesticConsent4, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) throws OBErrorResponseException, com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException {

        logger.info("Processing createDomesticPaymentConsents request - consent: {}, idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                    obWriteDomesticConsent4, xIdempotencyKey, apiClientId, xFapiInteractionId);

        domesticConsentValidator.validate(obWriteDomesticConsent4);

        final CreateDomesticPaymentConsentRequest createRequest = new CreateDomesticPaymentConsentRequest();
        createRequest.setConsentRequest(FRWriteDomesticConsentConverter.toFRWriteDomesticConsent(obWriteDomesticConsent4));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteDomesticConsent4));

        final DomesticPaymentConsent consent = consentStoreApiClient.createConsent(createRequest);
        logger.info("Created consent - id: {}", consent.getId());
        logger.info("Created consent: {}", consent);

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<FRCharge> calculateCharges(OBWriteDomesticConsent4 obWriteDomesticConsent4) {
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<OBWriteDomesticConsentResponse5> getDomesticPaymentConsentsConsentId(String consentId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) {

        logger.info("Processing getDomesticPaymentConsentsConsentId request - consentId: {}, apiClient: {}, x-fapi-interaction-id: {}",
                    consentId, apiClientId, xFapiInteractionId);

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }

    @Override
    public ResponseEntity<OBWriteFundsConfirmationResponse1> getDomesticPaymentConsentsConsentIdFundsConfirmation(String consentId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) throws OBErrorResponseException {

        logger.info("Processing getDomesticPaymentConsentsConsentIdFundsConfirmation request - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        final DomesticPaymentConsent consent = consentStoreApiClient.getConsent(consentId, apiClientId);
        if (OBPaymentConsentStatus.fromValue(consent.getStatus()) != OBPaymentConsentStatus.AUTHORISED) {
            throw new OBErrorResponseException(HttpStatus.BAD_REQUEST, OBRIErrorResponseCategory.REQUEST_INVALID,
                                               OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(consent.getStatus()));
        }

        final boolean fundsAvailable = fundsAvailabilityService.isFundsAvailable(consent.getAuthorisedDebtorAccountId(),
                                                                                 consent.getRequestObj().getData().getInitiation().getInstructedAmount().getAmount());

        return ResponseEntity.ok(fundsConfirmationResponseFactory.create(fundsAvailable, consentId,
                id -> LinksHelper.createDomesticPaymentConsentsFundsConfirmationLink(getClass(), id)));
    }

}
