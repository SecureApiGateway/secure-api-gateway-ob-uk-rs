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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.vrp.DomesticVrpConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.OBDomesticVRPConsentResponseFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.conent.store.client.payment.vrp.v3_1_10.DomesticVRPConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.vrp.v3_1_10.CreateDomesticVRPConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import uk.org.openbanking.datamodel.vrp.*;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentResponseData.StatusEnum;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Controller("DomesticVrpConsentApiV3.1.10")
@Slf4j
public class DomesticVrpConsentsApiController implements DomesticVrpConsentsApi {

    private final FundsAvailabilityService fundsAvailabilityService;

    private final DomesticVRPConsentStoreClient consentStoreClient;

    private final OBValidationService<OBDomesticVRPConsentRequest> vrpConsentValidator;

    private final OBDomesticVRPConsentResponseFactory responseFactory;

    public DomesticVrpConsentsApiController(FundsAvailabilityService fundsAvailabilityService,
                                            DomesticVRPConsentStoreClient consentStoreClient,
                                            OBValidationService<OBDomesticVRPConsentRequest> vrpConsentValidator,
                                            OBDomesticVRPConsentResponseFactory responseFactory) {
        this.fundsAvailabilityService = fundsAvailabilityService;
        this.vrpConsentValidator = vrpConsentValidator;
        this.consentStoreClient = consentStoreClient;
        this.responseFactory = responseFactory;
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

        log.info("domesticVrpConsentsGet - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}", consentId, apiClientId, xFapiInteractionId);
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

        log.info("domesticVrpConsentsPost - creating consent: {}, apiClientId: {}, idempotencyKey: {}, x-fapi-interaction-id: {}  ",
                obDomesticVRPConsentRequest, apiClientId, xIdempotencyKey, xFapiInteractionId);

        vrpConsentValidator.validate(obDomesticVRPConsentRequest);

        final CreateDomesticVRPConsentRequest createRequest = new CreateDomesticVRPConsentRequest();
        createRequest.setConsentRequest(FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(obDomesticVRPConsentRequest));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);

        final DomesticVRPConsent consent = consentStoreClient.createConsent(createRequest);
        log.info("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(responseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> domesticVrpConsentsDelete(String consentId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request) throws OBErrorResponseException {
        log.info("domesticVrpConsentsDelete - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}", consentId, apiClientId, xFapiInteractionId);
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

        validateFundsConfirmationRequest(obVRPFundsConfirmationRequest, consentId);

        DomesticVRPConsent consent = consentStoreClient.getConsent(consentId, apiClientId);

        if (StatusEnum.fromValue(consent.getStatus()) != StatusEnum.AUTHORISED) {
            throw new OBErrorResponseException(HttpStatus.BAD_REQUEST, OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(consent.getStatus()));
        }
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
                                                                                OBPAFundsAvailableResult1.FundsAvailableEnum.AVAILABLE :
                                                                                OBPAFundsAvailableResult1.FundsAvailableEnum.NOTAVAILABLE
                                                                )
                                                                .fundsAvailableDateTime(DateTime.now())
                                                )
                                                .instructedAmount(
                                                        obVRPFundsConfirmationRequest.getData().getInstructedAmount()
                                                )
                                )
                );
    }

    private void validateFundsConfirmationRequest(OBVRPFundsConfirmationRequest obVRPFundsConfirmationRequest,
                                                  String consentId) throws OBErrorResponseException {

        log.debug("Validating request {}", obVRPFundsConfirmationRequest.toString());
        OBVRPFundsConfirmationRequestData data = obVRPFundsConfirmationRequest.getData();
        if (Objects.isNull(data) || Objects.isNull(data.getInstructedAmount()) || Objects.isNull(data.getInstructedAmount().getAmount())) {
            String reason = "Mandatory data not provided.";
            log.error(reason);
            throw new OBErrorResponseException(
                    BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.REQUEST_OBJECT_INVALID.toOBError1(reason)
            );
        }
        if (!obVRPFundsConfirmationRequest.getData().getConsentId().equals(consentId)) {
            String reason = "The consentId provided in the body doesn't match with the consent id provided as parameter";
            log.error(reason);
            throw new OBErrorResponseException(
                    BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.REQUEST_OBJECT_INVALID.toOBError1(
                            reason
                    )
            );
        }
    }
}
