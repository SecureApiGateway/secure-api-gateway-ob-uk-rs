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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.internationalpayments;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.internationalpayments.InternationalPaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0.OBWriteFundsConfirmationResponse1Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0.OBWriteInternationalConsentResponse6Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.service.currency.ExchangeRateService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.international.InternationalPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.CreateInternationalPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;

import jakarta.servlet.http.HttpServletRequest;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.v4.payment.OBWriteFundsConfirmationResponse1;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternational3DataInitiation;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalConsentResponse6;

@Controller("InternationalPaymentConsentsApiV4.0.0")
public class InternationalPaymentConsentsApiController implements InternationalPaymentConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InternationalPaymentConsentStoreClient consentStoreApiClient;

    private final OBValidationService<OBWriteInternationalConsent5> consentValidator;

    private final ExchangeRateService exchangeRateService;

    private final OBWriteInternationalConsentResponse6Factory consentResponseFactory;

    private final FundsAvailabilityService fundsAvailabilityService;

    private final OBWriteFundsConfirmationResponse1Factory fundsConfirmationResponseFactory;

    public InternationalPaymentConsentsApiController(
            @Qualifier("v4.0.0RestInternationalPaymentConsentStoreClient") InternationalPaymentConsentStoreClient consentStoreApiClient,
            OBValidationService<OBWriteInternationalConsent5> consentValidator,
            ExchangeRateService exchangeRateService,
            OBWriteInternationalConsentResponse6Factory consentResponseFactory,
            FundsAvailabilityService fundsAvailabilityService,
            OBWriteFundsConfirmationResponse1Factory fundsConfirmationResponseFactory) {

        this.consentStoreApiClient = consentStoreApiClient;
        this.consentValidator = consentValidator;
        this.exchangeRateService = exchangeRateService;
        this.consentResponseFactory = consentResponseFactory;
        this.fundsAvailabilityService = fundsAvailabilityService;
        this.fundsConfirmationResponseFactory = fundsConfirmationResponseFactory;
    }

    @Override
    public ResponseEntity<OBWriteInternationalConsentResponse6> createInternationalPaymentConsents(
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            OBWriteInternationalConsent5 obWriteInternationalConsent5,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {

        logger.trace("Processing createInternationalPaymentConsents request - consent: {}, idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                obWriteInternationalConsent5, xIdempotencyKey, apiClientId, xFapiInteractionId);

        consentValidator.validate(obWriteInternationalConsent5);

        final CreateInternationalPaymentConsentRequest createRequest = new CreateInternationalPaymentConsentRequest();
        createRequest.setConsentRequest(FRWriteInternationalConsentConverter.toFRWriteInternationalConsent(obWriteInternationalConsent5));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteInternationalConsent5));
        final OBWriteInternational3DataInitiation initiation = obWriteInternationalConsent5.getData().getInitiation();
        final OBWriteDomestic2DataInitiationInstructedAmount instructedAmount = initiation.getInstructedAmount();
        createRequest.setExchangeRateInformation(exchangeRateService.calculateExchangeRateInfo(initiation.getCurrencyOfTransfer(),
                new FRAmount(instructedAmount.getAmount(), instructedAmount.getCurrency()), initiation.getExchangeRateInformation()));

        final InternationalPaymentConsent consent = consentStoreApiClient.createConsent(createRequest);
        logger.trace("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<FRCharge> calculateCharges(OBWriteInternationalConsent5 obWriteInternationalConsent5) {
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<OBWriteInternationalConsentResponse6> getInternationalPaymentConsentsConsentId(
            String consentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) {

        logger.trace("Processing getInternationalPaymentConsentsConsentId request - consentId: {}, apiClient: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }

    @Override
    public ResponseEntity<OBWriteFundsConfirmationResponse1> getInternationalPaymentConsentsConsentIdFundsConfirmation(
            String consentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {

        logger.trace("Processing getInternationalPaymentConsentsConsentIdFundsConfirmation request - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        final InternationalPaymentConsent consent = consentStoreApiClient.getConsent(consentId, apiClientId);
        if (OBPaymentConsentStatus.fromValue(consent.getStatus()) != OBPaymentConsentStatus.AUTHORISED) {
            throw new OBErrorResponseException(HttpStatus.BAD_REQUEST, OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(consent.getStatus()));
        }

        final boolean fundsAvailable = fundsAvailabilityService.isFundsAvailable(consent.getAuthorisedDebtorAccountId(),
                consent.getRequestObj().getData().getInitiation().getInstructedAmount().getAmount());

        return ResponseEntity.ok(fundsConfirmationResponseFactory.create(fundsAvailable, consentId,
                id -> LinksHelper.createInternationalPaymentConsentsFundsConfirmationLink(getClass(), id)));
    }
}
