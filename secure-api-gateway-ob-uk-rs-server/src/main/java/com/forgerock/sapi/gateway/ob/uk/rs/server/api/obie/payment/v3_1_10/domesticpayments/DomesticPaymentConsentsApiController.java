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
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.conent.store.client.DomesticPaymentConsentApiClient;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.CreateDomesticPaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.conent.store.datamodel.payment.domestic.DomesticPaymentConsent;

import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5Data.StatusEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1Data;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1DataFundsAvailableResult;

@Controller("DomesticPaymentConsentsApiV3.1.10")
public class DomesticPaymentConsentsApiController implements DomesticPaymentConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DomesticPaymentConsentApiClient consentStoreApiClient;

    private final OBValidationService<OBWriteDomesticConsent4> domesticConsentValidator;

    private final FundsAvailabilityService fundsAvailabilityService;

    public DomesticPaymentConsentsApiController(DomesticPaymentConsentApiClient consentStoreApiClient,
                                                OBValidationService<OBWriteDomesticConsent4> domesticConsentValidator,
                                                FundsAvailabilityService fundsAvailabilityService) {
        this.consentStoreApiClient = consentStoreApiClient;
        this.domesticConsentValidator = domesticConsentValidator;
        this.fundsAvailabilityService = fundsAvailabilityService;
    }

    @Override
    public ResponseEntity<OBWriteDomesticConsentResponse5> createDomesticPaymentConsents(OBWriteDomesticConsent4 obWriteDomesticConsent4, String authorization, String xIdempotencyKey, String xJwsSignature, DateTime xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {

        domesticConsentValidator.validate(obWriteDomesticConsent4);

        final CreateDomesticPaymentConsentRequest createRequest = new CreateDomesticPaymentConsentRequest();
        createRequest.setConsentRequest(obWriteDomesticConsent4);
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteDomesticConsent4));

        // TODO handle errors
        final DomesticPaymentConsent consent = consentStoreApiClient.createConsent(createRequest);

        return new ResponseEntity<>(buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    private List<OBWriteDomesticConsentResponse5DataCharges> calculateCharges(OBWriteDomesticConsent4 obWriteDomesticConsent4) {
        // TODO add some logic to apply charges to payments
        return List.of();
    }

    // TODO move to builder lib
    public static OBWriteDomesticConsentResponse5 buildConsentResponse(DomesticPaymentConsent domesticPaymentConsent, Class<?> controllerClass) {
        final OBWriteDomesticConsentResponse5 consentResponse = new OBWriteDomesticConsentResponse5();
        final OBWriteDomesticConsentResponse5Data data = new OBWriteDomesticConsentResponse5Data();
        final OBWriteDomesticConsent4Data consentRequestData = domesticPaymentConsent.getRequestObj().getData();
        data.authorisation(consentRequestData.getAuthorisation());
        data.readRefundAccount(consentRequestData.getReadRefundAccount());
        data.scASupportData(consentRequestData.getScASupportData());
        data.initiation(consentRequestData.getInitiation());

        data.charges(domesticPaymentConsent.getCharges());
        data.consentId(domesticPaymentConsent.getId());
        data.status(StatusEnum.fromValue(domesticPaymentConsent.getStatus()));
        data.creationDateTime(domesticPaymentConsent.getCreationDateTime());
        data.statusUpdateDateTime(domesticPaymentConsent.getStatusUpdateDateTime());

        consentResponse.setData(data);

        consentResponse.setRisk(domesticPaymentConsent.getRequestObj().getRisk());
        consentResponse.links(LinksHelper.createDomesticPaymentConsentsLink(controllerClass, domesticPaymentConsent.getId())).meta(new Meta());

        return consentResponse;
    }

    @Override
    public ResponseEntity<OBWriteDomesticConsentResponse5> getDomesticPaymentConsentsConsentId(String consentId, String authorization, DateTime xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {
        // TODO error handling
        return ResponseEntity.ok(buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }

    @Override
    public ResponseEntity<OBWriteFundsConfirmationResponse1> getDomesticPaymentConsentsConsentIdFundsConfirmation(String consentId, String authorization, DateTime xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request, Principal principal) throws OBErrorResponseException {
        final DomesticPaymentConsent consent = consentStoreApiClient.getConsent(consentId, apiClientId);
        if (StatusEnum.fromValue(consent.getStatus()) != StatusEnum.AUTHORISED) {
            throw new OBErrorResponseException(HttpStatus.BAD_REQUEST, OBRIErrorResponseCategory.REQUEST_INVALID, OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(consent.getStatus()));
        }

        final boolean fundsAvailable = fundsAvailabilityService.isFundsAvailable(consent.getAuthorisedDebtorAccountId(), consent.getRequestObj().getData().getInitiation().getInstructedAmount().getAmount());

        // TODO create reuseable factory for these responses, will be the same across controllers
        return ResponseEntity.ok(new OBWriteFundsConfirmationResponse1()
                                        .data(new OBWriteFundsConfirmationResponse1Data()
                                                     .fundsAvailableResult(new OBWriteFundsConfirmationResponse1DataFundsAvailableResult()
                                                                                     .fundsAvailable(fundsAvailable)
                                                                                     .fundsAvailableDateTime(DateTime.now()))
                                                     .supplementaryData(null))
                                        .links(LinksHelper.createDomesticPaymentsConsentFundsConfirmationLink(getClass(), consentId))
                                        .meta(new Meta()));
    }
}
