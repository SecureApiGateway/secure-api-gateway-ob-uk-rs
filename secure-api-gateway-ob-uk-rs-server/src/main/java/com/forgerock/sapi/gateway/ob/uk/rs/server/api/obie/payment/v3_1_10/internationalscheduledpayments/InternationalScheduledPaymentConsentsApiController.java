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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.internationalscheduledpayments;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.internationalscheduledpayments.InternationalScheduledPaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.OBWriteFundsConfirmationResponse1Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.OBWriteInternationalScheduledConsentResponse6Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.currency.ExchangeRateService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.CreateInternationalScheduledPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsent;

import jakarta.servlet.http.HttpServletRequest;
import uk.org.openbanking.datamodel.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduled3DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsentResponse6;

@Controller("InternationalScheduledPaymentConsentsApiV3.1.10")
public class InternationalScheduledPaymentConsentsApiController implements InternationalScheduledPaymentConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InternationalScheduledPaymentConsentStoreClient consentStoreApiClient;

    private final OBValidationService<OBWriteInternationalScheduledConsent5> consentValidator;

    private final ExchangeRateService exchangeRateService;

    private final OBWriteInternationalScheduledConsentResponse6Factory consentResponseFactory;

    private final FundsAvailabilityService fundsAvailabilityService;

    private final OBWriteFundsConfirmationResponse1Factory fundsConfirmationResponseFactory;

    public InternationalScheduledPaymentConsentsApiController(InternationalScheduledPaymentConsentStoreClient consentStoreApiClient,
            OBValidationService<OBWriteInternationalScheduledConsent5> consentValidator,
            ExchangeRateService exchangeRateService,
            OBWriteInternationalScheduledConsentResponse6Factory consentResponseFactory,
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
    public ResponseEntity<OBWriteInternationalScheduledConsentResponse6> createInternationalScheduledPaymentConsents(OBWriteInternationalScheduledConsent5 obWriteInternationalScheduledConsent5,
            String authorization, String xIdempotencyKey, String xJwsSignature, String xFapiAuthDate, String xFapiCustomerIpAddress,
            String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        logger.info("Processing createInternationalScheduledPaymentConsents request - consent: {}, idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                obWriteInternationalScheduledConsent5, xIdempotencyKey, apiClientId, xFapiInteractionId);

        consentValidator.validate(obWriteInternationalScheduledConsent5);

        final CreateInternationalScheduledPaymentConsentRequest createRequest = new CreateInternationalScheduledPaymentConsentRequest();
        createRequest.setConsentRequest(FRWriteInternationalScheduledConsentConverter.toFRWriteInternationalScheduledConsent(obWriteInternationalScheduledConsent5));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteInternationalScheduledConsent5));
        final OBWriteInternationalScheduled3DataInitiation initiation = obWriteInternationalScheduledConsent5.getData().getInitiation();
        final OBWriteDomestic2DataInitiationInstructedAmount instructedAmount = initiation.getInstructedAmount();
        createRequest.setExchangeRateInformation(exchangeRateService.calculateExchangeRateInfo(initiation.getCurrencyOfTransfer(),
                new FRAmount(instructedAmount.getAmount(), instructedAmount.getCurrency()), initiation.getExchangeRateInformation()));

        final InternationalScheduledPaymentConsent consent = consentStoreApiClient.createConsent(createRequest);
        logger.info("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<FRCharge> calculateCharges(OBWriteInternationalScheduledConsent5 obWriteInternationalScheduledConsent5) {
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<OBWriteInternationalScheduledConsentResponse6> getInternationalScheduledPaymentConsentsConsentId(String consentId, String authorization,
            String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent,
            String apiClientId, HttpServletRequest request, Principal principal) {

        logger.info("Processing getInternationalScheduledPaymentConsentsConsentId request - consentId: {}, apiClient: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }

    @Override
    public ResponseEntity<OBWriteFundsConfirmationResponse1> getInternationalScheduledPaymentConsentsConsentIdFundsConfirmation(String consentId,
            String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId,
            String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        logger.info("Processing getInternationalScheduledPaymentConsentsConsentIdFundsConfirmation request - consentId: {}, apiClientId: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        final InternationalScheduledPaymentConsent consent = consentStoreApiClient.getConsent(consentId, apiClientId);
        if (OBPaymentConsentStatus.fromValue(consent.getStatus()) != OBPaymentConsentStatus.AUTHORISED) {
            throw new OBErrorResponseException(HttpStatus.BAD_REQUEST, OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(consent.getStatus()));
        }

        final boolean fundsAvailable = fundsAvailabilityService.isFundsAvailable(consent.getAuthorisedDebtorAccountId(),
                consent.getRequestObj().getData().getInitiation().getInstructedAmount().getAmount());

        return ResponseEntity.ok(fundsConfirmationResponseFactory.create(fundsAvailable, consentId,
                id -> LinksHelper.createInternationalScheduledPaymentConsentsFundsConfirmationLink(getClass(), id)));
    }
}
