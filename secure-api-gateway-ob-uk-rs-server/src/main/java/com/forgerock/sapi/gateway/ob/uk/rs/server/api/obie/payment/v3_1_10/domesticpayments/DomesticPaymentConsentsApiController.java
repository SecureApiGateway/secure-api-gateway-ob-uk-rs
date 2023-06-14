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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.domesticpayments;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.domesticpayments.DomesticPaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.OBWriteDomesticConsentResponse5Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.OBWriteFundsConfirmationResponse1Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.conent.store.client.v3_1_10.DomesticPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.v3_1_10.DomesticPaymentConsent;

import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1;

@Controller("DomesticPaymentConsentsApiV3.1.10")
public class DomesticPaymentConsentsApiController implements DomesticPaymentConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticPaymentConsentStoreClient consentStoreApiClient;

    private final OBValidationService<OBWriteDomesticConsent4> domesticConsentValidator;

    private final OBWriteDomesticConsentResponse5Factory consentResponseFactory;

    private final FundsAvailabilityService fundsAvailabilityService;

    private final OBWriteFundsConfirmationResponse1Factory fundsConfirmationResponseFactory;

    public DomesticPaymentConsentsApiController(DomesticPaymentConsentStoreClient consentStoreApiClient,
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
    public ResponseEntity<OBWriteDomesticConsentResponse5> createDomesticPaymentConsents(OBWriteDomesticConsent4 obWriteDomesticConsent4,
                String authorization, String xIdempotencyKey, String xJwsSignature, DateTime xFapiAuthDate, String xFapiCustomerIpAddress,
                String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        domesticConsentValidator.validate(obWriteDomesticConsent4);

        final CreateDomesticPaymentConsentRequest createRequest = new CreateDomesticPaymentConsentRequest();
        createRequest.setConsentRequest(obWriteDomesticConsent4);
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteDomesticConsent4));

        final DomesticPaymentConsent consent = consentStoreApiClient.createConsent(createRequest);

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<OBWriteDomesticConsentResponse5DataCharges> calculateCharges(OBWriteDomesticConsent4 obWriteDomesticConsent4) {
        // TODO add some logic to apply charges to payments
        return List.of();
    }

    @Override
    public ResponseEntity<OBWriteDomesticConsentResponse5> getDomesticPaymentConsentsConsentId(String consentId, String authorization,
            DateTime xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent,
            String apiClientId, HttpServletRequest request, Principal principal) {

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }

    @Override
    public ResponseEntity<OBWriteFundsConfirmationResponse1> getDomesticPaymentConsentsConsentIdFundsConfirmation(String consentId,
            String authorization, DateTime xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId,
            String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        final DomesticPaymentConsent consent = consentStoreApiClient.getConsent(consentId, apiClientId);
        if (StatusEnum.fromValue(consent.getStatus()) != StatusEnum.AUTHORISED) {
            throw new OBErrorResponseException(HttpStatus.BAD_REQUEST, OBRIErrorResponseCategory.REQUEST_INVALID, OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(consent.getStatus()));
        }

        final boolean fundsAvailable = fundsAvailabilityService.isFundsAvailable(consent.getAuthorisedDebtorAccountId(), consent.getRequestObj().getData().getInitiation().getInstructedAmount().getAmount());

        return ResponseEntity.ok(fundsConfirmationResponseFactory.create(fundsAvailable, consentId, getClass()));
    }
}
