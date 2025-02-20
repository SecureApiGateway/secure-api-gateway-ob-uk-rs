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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.domesticstandingorders;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.INITIATIONPENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRAccountIdentifierConverter.toOBCashAccountDebtor4;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRSubmissionStatusConverter.toOBWriteDomesticStandingOrderResponse6DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteDomesticStandingOrderConsentConverter.toOBWriteDomesticStandingOrder3DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteDomesticStandingOrderConverter.toFRWriteDomesticStandingOrder;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRChargeConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRPaymentDetailsStatusConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteDomesticStandingOrderConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDataDomesticStandingOrder;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticStandingOrder;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.domesticstandingorders.DomesticStandingOrdersApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.RefundAccountService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.IdempotentPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.SinglePaymentForConsentIdempotentPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomesticStandingOrder3Validator.OBWriteDomesticStandingOrder3ValidationContext;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.domesticstandingorder.DomesticStandingOrderConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.domesticstandingorder.v3_1_10.DomesticStandingOrderConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRDomesticStandingOrderPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticStandingOrderPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import jakarta.servlet.http.HttpServletRequest;
import uk.org.openbanking.datamodel.v4.common.Meta;
import uk.org.openbanking.datamodel.v4.common.OBStatusReason;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrder3;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderResponse6;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderResponse6Data;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1StatusDetail;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1StatusDetailStatus;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetailsResponse1Data;

@Controller("DomesticStandingOrdersApiV4.0.0")
public class DomesticStandingOrdersApiController implements DomesticStandingOrdersApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DomesticStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final DomesticStandingOrderConsentStoreClient consentStoreClient;
    private final OBValidationService<OBWriteDomesticStandingOrder3ValidationContext> paymentValidator;
    private final RefundAccountService refundAccountService;
    private final IdempotentPaymentService<FRDomesticStandingOrderPaymentSubmission, FRWriteDomesticStandingOrder> idempotentPaymentService;

    public DomesticStandingOrdersApiController(
            DomesticStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            @Qualifier("v4.0.0RestDomesticStandingOrderConsentStoreClient") DomesticStandingOrderConsentStoreClient consentStoreClient,
            OBValidationService<OBWriteDomesticStandingOrder3ValidationContext> paymentValidator,
            RefundAccountService refundAccountService) {

        this.standingOrderPaymentSubmissionRepository = standingOrderPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.consentStoreClient = consentStoreClient;
        this.paymentValidator = paymentValidator;
        this.refundAccountService = refundAccountService;
        this.idempotentPaymentService = new SinglePaymentForConsentIdempotentPaymentService<>(standingOrderPaymentSubmissionRepository);
    }

    @Override
    public ResponseEntity<OBWriteDomesticStandingOrderResponse6> createDomesticStandingOrders(
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            OBWriteDomesticStandingOrder3 obWriteDomesticStandingOrder3,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException, OBErrorException {

        logger.debug("Received payment submission: '{}'", obWriteDomesticStandingOrder3);

        paymentSubmissionValidator.validateIdempotencyKey(xIdempotencyKey);

        String consentId = obWriteDomesticStandingOrder3.getData().getConsentId();
        logger.debug("Attempting to get consent: {}, clientId: {}", consentId, apiClientId);
        final DomesticStandingOrderConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        logger.debug("Got consent from store: {}", consent);

        final FRWriteDomesticStandingOrder frStandingOrder = toFRWriteDomesticStandingOrder(obWriteDomesticStandingOrder3);
        logger.trace("Converted to: '{}'", frStandingOrder);

        final Optional<FRDomesticStandingOrderPaymentSubmission> existingPayment =
                idempotentPaymentService.findExistingPayment(frStandingOrder, consentId, apiClientId, xIdempotencyKey);
        if (existingPayment.isPresent()) {
            logger.info("Payment submission is a replay of a previous payment, returning previously created payment for x-idempotencyKey: {}, consentId: {}",
                    xIdempotencyKey, consentId);
            return ResponseEntity.status(CREATED).body(responseEntity(consent, existingPayment.get()));
        }

        // validate the consent against the request
        logger.debug("Validating Domestic Standing Order submission");
        final OBWriteDomesticStandingOrder3ValidationContext validationCtxt = new OBWriteDomesticStandingOrder3ValidationContext(obWriteDomesticStandingOrder3,
                FRWriteDomesticStandingOrderConsentConverter.toOBWriteDomesticStandingOrderConsent5(consent.getRequestObj()), consent.getStatus());
        paymentValidator.validate(validationCtxt);
        logger.debug("Domestic Standing Order validation successful");

        FRDomesticStandingOrderPaymentSubmission frPaymentSubmission = FRDomesticStandingOrderPaymentSubmission.builder()
                .id(obWriteDomesticStandingOrder3.getData().getConsentId())
                .standingOrder(frStandingOrder)
                .status(INITIATIONPENDING)
                .created(new Date())
                .updated(new Date())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the domestic standing order
        frPaymentSubmission = idempotentPaymentService.savePayment(frPaymentSubmission);

        final ConsumePaymentConsentRequest consumePaymentRequest = new ConsumePaymentConsentRequest();
        consumePaymentRequest.setConsentId(consentId);
        consumePaymentRequest.setApiClientId(apiClientId);
        consentStoreClient.consumeConsent(consumePaymentRequest);

        return ResponseEntity.status(CREATED).body(responseEntity(consent, frPaymentSubmission));
    }

    @Override
    public ResponseEntity getDomesticStandingOrdersDomesticStandingOrderId(
            String domesticStandingOrderId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {

        Optional<FRDomesticStandingOrderPaymentSubmission> isPaymentSubmission = standingOrderPaymentSubmissionRepository.findById(domesticStandingOrderId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + domesticStandingOrderId + "' can't be found");
        }

        FRDomesticStandingOrderPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }

        final DomesticStandingOrderConsent consent = consentStoreClient.getConsent(frPaymentSubmission.getConsentId(), apiClientId);

        return ResponseEntity.ok(responseEntity(consent, frPaymentSubmission));
    }

    @Override
    public ResponseEntity getDomesticStandingOrdersDomesticStandingOrderIdPaymentDetails(
            String domesticStandingOrderId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {

        Optional<FRDomesticStandingOrderPaymentSubmission> isStandingOrderSubmission = standingOrderPaymentSubmissionRepository.findById(domesticStandingOrderId);
        if (!isStandingOrderSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Domestic Standing Order submission '" + domesticStandingOrderId + "' can't be found");
        }

        FRDomesticStandingOrderPaymentSubmission frStandingOrderSubmission = isStandingOrderSubmission.get();
        logger.debug("Found the Domestic Standing Order '{}' to get details.", domesticStandingOrderId);
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frStandingOrderSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frStandingOrderSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntityDetails(frStandingOrderSubmission));
    }

    private OBWriteDomesticStandingOrderResponse6 responseEntity(DomesticStandingOrderConsent consent,
            FRDomesticStandingOrderPaymentSubmission frPaymentSubmission) {

        final Optional<FRResponseDataRefund> refundAccountData = refundAccountService.getDomesticPaymentRefundData(
                consent.getRequestObj().getData().getReadRefundAccount(), consent);

        FRWriteDataDomesticStandingOrder data = frPaymentSubmission.getStandingOrder().getData();
        OBWriteDomesticStandingOrderResponse6Data responseData = new OBWriteDomesticStandingOrderResponse6Data();
        return new OBWriteDomesticStandingOrderResponse6()
                .data(new OBWriteDomesticStandingOrderResponse6Data()
                        .charges(FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()))
                        .domesticStandingOrderId(frPaymentSubmission.getId())
                        .initiation(toOBWriteDomesticStandingOrder3DataInitiation(data.getInitiation()))
                        .creationDateTime(new DateTime(frPaymentSubmission.getCreated().getTime()))
                        .statusUpdateDateTime(new DateTime(frPaymentSubmission.getUpdated().getTime()))
                        .status(toOBWriteDomesticStandingOrderResponse6DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId())
                        .debtor(toOBCashAccountDebtor4(data.getInitiation().getDebtorAccount()))
                        .refund(refundAccountData.map(FRResponseDataRefundConverter::toOBWriteDomesticResponse5DataRefund).orElse(null))
                        .statusReason(Collections.singletonList(FRModelMapper.map(responseData.getStatusReason(), OBStatusReason.class)))
                )
                .links(LinksHelper.createDomesticStandingOrderPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

    private OBWritePaymentDetailsResponse1 responseEntityDetails(FRDomesticStandingOrderPaymentSubmission frStandingOrderSubmission) {

        // Build the response object with data to meet the expected data defined by the spec
        OBWritePaymentDetails1StatusDetailStatus statusDetailStatus = OBWritePaymentDetails1StatusDetailStatus.PDNG;
        return new OBWritePaymentDetailsResponse1()
                .data(
                        new OBWritePaymentDetailsResponse1Data()
                                .addPaymentStatusItem(
                                        new OBWritePaymentDetails1()
                                                .status(FRPaymentDetailsStatusConverter.toOBPaymentDetailsStatus(frStandingOrderSubmission.getStatus().getValue()))
                                                .paymentTransactionId(UUID.randomUUID().toString())
                                                .statusUpdateDateTime(new DateTime(frStandingOrderSubmission.getUpdated()))
                                                .statusDetail(
                                                        new OBWritePaymentDetails1StatusDetail()
                                                                .status(FRPaymentDetailsStatusConverter.toOBWritePaymentDetails1StatusDetailStatus(
                                                                        frStandingOrderSubmission.getStatus().getValue()))
                                                                .statusReason(String.valueOf(statusDetailStatus))
                                                                .statusReasonDescription(statusDetailStatus.getValue())
                                                )
                                )
                )
                .links(LinksHelper.createDomesticStandingOrderPaymentDetailsLink(this.getClass(), frStandingOrderSubmission.getId()))
                .meta(new Meta());
    }
}
