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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.vrp;

import java.util.Objects;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.vrp.DomesticVrpConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0.OBDomesticVRPConsentResponseFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.migration.ConsentComparisonService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.migration.ConsentMigrationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBVRPFundsConfirmationRequestValidator.VRPFundsConfirmationValidationContext;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.vrp.DomesticVRPConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.CreateDomesticVRPConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.v4.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.v4.vrp.OBDomesticVRPConsentResponse;
import uk.org.openbanking.datamodel.v4.vrp.OBPAFundsAvailableResult1;
import uk.org.openbanking.datamodel.v4.vrp.OBPAFundsAvailableResult1FundsAvailable;
import uk.org.openbanking.datamodel.v4.vrp.OBVRPFundsConfirmationRequest;
import uk.org.openbanking.datamodel.v4.vrp.OBVRPFundsConfirmationResponse;
import uk.org.openbanking.datamodel.v4.vrp.OBVRPFundsConfirmationResponseData;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Controller("DomesticVrpConsentApiV4.0.0")
@Slf4j
public class DomesticVrpConsentsApiController implements DomesticVrpConsentsApi {

    private final FundsAvailabilityService fundsAvailabilityService;

    private final ConsentMigrationService consentMigrationService;

    private final ConsentComparisonService consentComparisonService;

    private final DomesticVRPConsentStoreClient consentStoreClient;

    private final DomesticVRPConsentStoreClient v3consentStoreClient;

    private final OBValidationService<OBDomesticVRPConsentRequest> vrpConsentValidator;

    private final OBValidationService<VRPFundsConfirmationValidationContext> vrpFundsConfirmationValidator;

    private final OBDomesticVRPConsentResponseFactory responseFactory;

    public DomesticVrpConsentsApiController(FundsAvailabilityService fundsAvailabilityService,
                                            ConsentMigrationService consentMigrationService,
                                            ConsentComparisonService consentComparisonService,
                                            @Qualifier("v4.0.0RestDomesticVRPConsentStoreClient") DomesticVRPConsentStoreClient consentStoreClient,
                                            @Qualifier("v3.1.10RestDomesticVRPConsentStoreClient") DomesticVRPConsentStoreClient v3consentStoreClient,
                                            OBValidationService<OBDomesticVRPConsentRequest> vrpConsentValidator,
                                            OBValidationService<VRPFundsConfirmationValidationContext> vrpFundsConfirmationValidator,
                                            OBDomesticVRPConsentResponseFactory responseFactory) {
        this.fundsAvailabilityService = Objects.requireNonNull(fundsAvailabilityService, "FundsAvailabilityService cannot be null");
        this.consentMigrationService = Objects.requireNonNull(consentMigrationService, "ConsentMigrationService cannot be null");
        this.consentComparisonService = Objects.requireNonNull(consentComparisonService, "ConsentComparisonService cannot be null");
        this.consentStoreClient = Objects.requireNonNull(consentStoreClient, "ConsentStoreClient cannot be null");
        this.v3consentStoreClient = Objects.requireNonNull(v3consentStoreClient, "ConsentStoreClient cannot be null");
        this.vrpConsentValidator = Objects.requireNonNull(vrpConsentValidator, "VRPConsentValidator cannot be null");
        this.vrpFundsConfirmationValidator = Objects.requireNonNull(vrpFundsConfirmationValidator, "VRPFundsConfirmationValidator cannot be null");
        this.responseFactory = Objects.requireNonNull(responseFactory, "ResponseFactory cannot be null");
    }

    @Override
    public ResponseEntity<OBDomesticVRPConsentResponse> domesticVrpConsentsGet(String consentId,
                                                                               String authorization,
                                                                               String xFapiAuthDate,
                                                                               String xFapiCustomerIpAddress,
                                                                               String xFapiInteractionId,
                                                                               String xCustomerUserAgent,
                                                                               String apiClientId,
                                                                               HttpServletRequest request) throws OBErrorResponseException {

        log.trace("domesticVrpConsentsGet - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}", consentId, apiClientId, xFapiInteractionId);
        final DomesticVRPConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        return ResponseEntity.ok(responseFactory.buildConsentResponse(consent, getClass()));
    }

    @Override
    public ResponseEntity<OBDomesticVRPConsentResponse> domesticVrpConsentsPost(String authorization,
                                                                                String xIdempotencyKey,
                                                                                String xJwsSignature,
                                                                                OBDomesticVRPConsentRequest obDomesticVRPConsentRequest,
                                                                                String xFapiAuthDate, String xFapiCustomerIpAddress,
                                                                                String xFapiInteractionId,
                                                                                String xCustomerUserAgent,
                                                                                String apiClientId,
                                                                                HttpServletRequest request) throws OBErrorResponseException {

        log.trace("domesticVrpConsentsPost - creating consent: {}, apiClientId: {}, idempotencyKey: {}, x-fapi-interaction-id: {}  ",
                obDomesticVRPConsentRequest, apiClientId, xIdempotencyKey, xFapiInteractionId);

        vrpConsentValidator.validate(obDomesticVRPConsentRequest);

        final CreateDomesticVRPConsentRequest createRequest = new CreateDomesticVRPConsentRequest();
        createRequest.setConsentRequest(FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(obDomesticVRPConsentRequest));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);

        final DomesticVRPConsent consent = consentStoreClient.createConsent(createRequest);
        log.debug("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(responseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> domesticVrpConsentsDelete(String consentId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request) throws OBErrorResponseException {
        log.trace("domesticVrpConsentsDelete - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}", consentId, apiClientId, xFapiInteractionId);
        consentStoreClient.deleteConsent(consentId, apiClientId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<OBVRPFundsConfirmationResponse> domesticVrpConsentsFundsConfirmation(String consentId,
                                                                                               String authorization,
                                                                                               String xJwsSignature,
                                                                                               OBVRPFundsConfirmationRequest obVRPFundsConfirmationRequest,
                                                                                               String xFapiAuthDate,
                                                                                               String xFapiCustomerIpAddress,
                                                                                               String xFapiInteractionId,
                                                                                               String xCustomerUserAgent,
                                                                                               String apiClientId,
                                                                                               HttpServletRequest request
    ) throws OBErrorResponseException {

        log.trace("domesticVrpConsentsFundsConfirmation - attempting to get funds conf for consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}", consentId, apiClientId, xFapiInteractionId);

        DomesticVRPConsent consent = consentStoreClient.getConsent(consentId, apiClientId);

        vrpFundsConfirmationValidator.validate(new VRPFundsConfirmationValidationContext(consentId, consent.getStatus(), obVRPFundsConfirmationRequest));

        String accountId = consent.getAuthorisedDebtorAccountId();
        String amount = obVRPFundsConfirmationRequest.getData().getInstructedAmount().getAmount();

        // Check if funds are available on the account
        boolean areFundsAvailable = fundsAvailabilityService.isFundsAvailable(accountId, amount);
        log.debug("Are funds available {}", areFundsAvailable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new OBVRPFundsConfirmationResponse()
                                .data(
                                        new OBVRPFundsConfirmationResponseData()
                                                .consentId(obVRPFundsConfirmationRequest.getData().getConsentId())
                                                .reference(obVRPFundsConfirmationRequest.getData().getReference())
                                                .fundsConfirmationId(UUID.randomUUID().toString())
                                                .fundsAvailableResult(
                                                        new OBPAFundsAvailableResult1()
                                                                .fundsAvailable(
                                                                        areFundsAvailable ?
                                                                                OBPAFundsAvailableResult1FundsAvailable.AVAILABLE :
                                                                                OBPAFundsAvailableResult1FundsAvailable.NOTAVAILABLE
                                                                )
                                                                .fundsAvailableDateTime(DateTime.now())
                                                )
                                                .instructedAmount(
                                                        obVRPFundsConfirmationRequest.getData().getInstructedAmount()
                                                )
                                )
                );
    }

    @Override
    public ResponseEntity<OBDomesticVRPConsentResponse> domesticVrpConsentsPut(String consentId,
                                                                               String authorization,
                                                                               String xIdempotencyKey,
                                                                               String xJwsSignature,
                                                                               OBDomesticVRPConsentRequest obDomesticVRPConsentRequest,
                                                                               String xFapiAuthDate,
                                                                               String xFapiCustomerIpAddress,
                                                                               String xFapiInteractionId,
                                                                               String xCustomerUserAgent,
                                                                               String apiClientId,
                                                                               HttpServletRequest request) throws OBErrorResponseException {

        final DomesticVRPConsent consent = v3consentStoreClient.getConsent(consentId, apiClientId);
        log.error("domesticVrpConsentsGet - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}", consentId, apiClientId, xFapiInteractionId);
        log.error("V3consent");

        // Validate consent
        if (consent == null) {
            throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.PAYMENT_CONSENT_NOT_FOUND.toOBError1(null)
            );
        }

        // Request vs. consent
        boolean doRequestAndConsentMatch = consentComparisonService.doesRequestMatchConsent(obDomesticVRPConsentRequest, consent);
        if (doRequestAndConsentMatch) {
            log.error("Request and consent match");
            log.error("domesticVrpConsentsDelete - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}",
                    consentId, apiClientId, xFapiInteractionId);
            v3consentStoreClient.deleteConsent(consentId, apiClientId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        // Create new consent
        CreateDomesticVRPConsentRequest createRequest = new CreateDomesticVRPConsentRequest();
        createRequest.setConsentRequest(FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(obDomesticVRPConsentRequest));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);

        DomesticVRPConsent newConsent = consentStoreClient.createConsent(createRequest);
        log.error("Created consent - id: {}", newConsent.getId());

        return new ResponseEntity<>(responseFactory.buildConsentResponse(newConsent, getClass()), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<OBDomesticVRPConsentResponse> domesticVrpConsentsPatch(final String consentId,
                                                                                 final String authorization,
                                                                                 final String xIdempotencyKey,
                                                                                 final String xJwsSignature,
                                                                                 final String body,
                                                                                 final String xFapiAuthDate,
                                                                                 final String xFapiCustomerIpAddress,
                                                                                 final String xFapiInteractionId,
                                                                                 final String xCustomerUserAgent,
                                                                                 final String apiClientId,
                                                                                 final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
