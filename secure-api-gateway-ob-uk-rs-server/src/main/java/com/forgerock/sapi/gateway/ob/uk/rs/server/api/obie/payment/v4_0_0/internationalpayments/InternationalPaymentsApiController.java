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

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.PENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRSubmissionStatusConverter.toOBWriteInternationalResponse5DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalConsentConverter.toOBWriteInternational3DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalConsentConverter.toOBWriteInternationalConsent5;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalConverter.toFRWriteInternational;
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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.mapper.FRModelMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRAccountIdentifierConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRPaymentDetailsStatusConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRExchangeRateConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternational;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalData;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.internationalpayments.InternationalPaymentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.RefundAccountService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.IdempotentPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.SinglePaymentForConsentIdempotentPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteInternational3Validator.OBWriteInternational3ValidationContext;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.international.InternationalPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRInternationalPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.InternationalPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import jakarta.servlet.http.HttpServletRequest;
import uk.org.openbanking.datamodel.v4.common.Meta;
import uk.org.openbanking.datamodel.v4.common.OBStatusReason;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternational3;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalResponse5;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalResponse5Data;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1StatusDetail;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1StatusDetailStatus;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetailsResponse1Data;

@Controller("InternationalPaymentsApiV4.0.0")
public class InternationalPaymentsApiController implements InternationalPaymentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final InternationalPaymentSubmissionRepository paymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final InternationalPaymentConsentStoreClient consentStoreClient;
    private final OBValidationService<OBWriteInternational3ValidationContext> paymentValidator;
    private final RefundAccountService refundAccountService;
    private final IdempotentPaymentService<FRInternationalPaymentSubmission, FRWriteInternational> idempotentPaymentService;

    public InternationalPaymentsApiController(
            InternationalPaymentSubmissionRepository paymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            @Qualifier("v4.0.0RestInternationalPaymentConsentStoreClient") InternationalPaymentConsentStoreClient consentStoreClient,
            OBValidationService<OBWriteInternational3ValidationContext> paymentValidator,
            RefundAccountService refundAccountService) {
        this.paymentSubmissionRepository = paymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.consentStoreClient = consentStoreClient;
        this.paymentValidator = paymentValidator;
        this.refundAccountService = refundAccountService;
        this.idempotentPaymentService = new SinglePaymentForConsentIdempotentPaymentService<>(paymentSubmissionRepository);
    }

    @Override
    public ResponseEntity<OBWriteInternationalResponse5> createInternationalPayments(
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            OBWriteInternational3 obWriteInternational3,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException, OBErrorException {

        logger.debug("Received payment submission: '{}'", obWriteInternational3);

        paymentSubmissionValidator.validateIdempotencyKey(xIdempotencyKey);

        String consentId = obWriteInternational3.getData().getConsentId();

        logger.debug("Attempting to get consent: {}, clientId: {}", consentId, apiClientId);
        final InternationalPaymentConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        logger.debug("Got consent from store: {}", consent);

        FRWriteInternational frInternationalPayment = toFRWriteInternational(obWriteInternational3);
        logger.trace("Converted to: '{}'", frInternationalPayment);

        final Optional<FRInternationalPaymentSubmission> existingPayment =
                idempotentPaymentService.findExistingPayment(frInternationalPayment, consentId, apiClientId, xIdempotencyKey);
        if (existingPayment.isPresent()) {
            logger.info("Payment submission is a replay of a previous payment, returning previously created payment for x-idempotencyKey: {}, consentId: {}",
                    xIdempotencyKey, consentId);
            return ResponseEntity.status(CREATED).body(responseEntity(consent, existingPayment.get()));
        }

        // validate the consent against the request
        logger.debug("Validating International Payment submission");
        paymentValidator.validate(new OBWriteInternational3ValidationContext(obWriteInternational3,
                toOBWriteInternationalConsent5(consent.getRequestObj()), consent.getStatus()));
        logger.debug("International Payment validation successful");

        FRInternationalPaymentSubmission frPaymentSubmission = FRInternationalPaymentSubmission.builder()
                .id(obWriteInternational3.getData().getConsentId())
                .payment(frInternationalPayment)
                .status(PENDING)
                .created(new Date())
                .updated(new Date())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international payment
        frPaymentSubmission = idempotentPaymentService.savePayment(frPaymentSubmission);

        final ConsumePaymentConsentRequest consumePaymentRequest = new ConsumePaymentConsentRequest();
        consumePaymentRequest.setConsentId(consentId);
        consumePaymentRequest.setApiClientId(apiClientId);
        consentStoreClient.consumeConsent(consumePaymentRequest);

        return ResponseEntity.status(CREATED).body(responseEntity(consent, frPaymentSubmission));
    }

    @Override
    public ResponseEntity getInternationalPaymentsInternationalPaymentId(
            String internationalPaymentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) {

        Optional<FRInternationalPaymentSubmission> isPaymentSubmission = paymentSubmissionRepository.findById(internationalPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + internationalPaymentId + "' can't be found");
        }

        FRInternationalPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        //get the consent
        final InternationalPaymentConsent consent = consentStoreClient.getConsent(frPaymentSubmission.getConsentId(), apiClientId);
        logger.debug("Got consent from store: {}", consent);

        return ResponseEntity.ok(responseEntity(consent, frPaymentSubmission));
    }

    @Override
    public ResponseEntity getInternationalPaymentsInternationalPaymentIdPaymentDetails(
            String internationalPaymentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal) {

        Optional<FRInternationalPaymentSubmission> isInternationalPaymentSubmission = paymentSubmissionRepository.findById(internationalPaymentId);
        if (!isInternationalPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("International payment submission '" + internationalPaymentId + "' can't be found");
        }

        FRInternationalPaymentSubmission frInternationalPaymentSubmission = isInternationalPaymentSubmission.get();
        logger.debug("Found The International Payment '{}' to get details.", internationalPaymentId);
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frInternationalPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frInternationalPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntityDetails(frInternationalPaymentSubmission));
    }

    private OBWriteInternationalResponse5 responseEntity(
            InternationalPaymentConsent consent,
            FRInternationalPaymentSubmission frPaymentSubmission
    ) {
        FRWriteInternationalData data = frPaymentSubmission.getPayment().getData();
        OBWriteInternationalResponse5Data responseData = new OBWriteInternationalResponse5Data();

        final Optional<FRInternationalResponseDataRefund> refundAccountData = refundAccountService.getInternationalPaymentRefundData(
                consent.getRequestObj().getData().getReadRefundAccount(),
                consent.getRequestObj().getData().getInitiation().getCreditor(),
                consent.getRequestObj().getData().getInitiation().getCreditorAgent(),
                consent);

        return new OBWriteInternationalResponse5()
                .data(new OBWriteInternationalResponse5Data()
                        .charges(toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()))
                        .internationalPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBWriteInternational3DataInitiation(data.getInitiation()))
                        .creationDateTime(new DateTime(frPaymentSubmission.getCreated().getTime()))
                        .statusUpdateDateTime(new DateTime(frPaymentSubmission.getUpdated().getTime()))
                        .status(toOBWriteInternationalResponse5DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId())
                        .debtor(FRAccountIdentifierConverter.toOBCashAccountDebtor4(data.getInitiation().getDebtorAccount()))
                        .refund(refundAccountData.map(FRResponseDataRefundConverter::toOBWriteInternationalResponse5DataRefund).orElse(null))
                        .exchangeRateInformation(FRExchangeRateConverter.toOBWriteInternationalConsentResponse6DataExchangeRateInformation(consent.getExchangeRateInformation()))
                        .statusReason(Collections.singletonList(FRModelMapper.map(responseData.getStatusReason(), OBStatusReason.class))))
                .links(LinksHelper.createInternationalPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

    private OBWritePaymentDetailsResponse1 responseEntityDetails(FRInternationalPaymentSubmission frInternationalPaymentSubmission) {

        // Build the response object with data to meet the expected data defined by the spec
        OBWritePaymentDetails1StatusDetailStatus statusDetailStatus = OBWritePaymentDetails1StatusDetailStatus.PDNG;
        return new OBWritePaymentDetailsResponse1()
                .data(
                        new OBWritePaymentDetailsResponse1Data()
                                .addPaymentStatusItem(
                                        new OBWritePaymentDetails1()
                                                .status(FRPaymentDetailsStatusConverter.toOBPaymentDetailsStatus(frInternationalPaymentSubmission.getStatus().getValue()))
                                                .paymentTransactionId(UUID.randomUUID().toString())
                                                .statusUpdateDateTime(new DateTime(frInternationalPaymentSubmission.getUpdated()))
                                                .statusDetail(
                                                        new OBWritePaymentDetails1StatusDetail()
                                                                .localInstrument(frInternationalPaymentSubmission.getPayment().getData().getInitiation().getLocalInstrument())
                                                                .status(FRPaymentDetailsStatusConverter.toOBWritePaymentDetails1StatusDetailStatus(
                                                                        frInternationalPaymentSubmission.getStatus().getValue()))
                                                                .statusReason(String.valueOf(statusDetailStatus))
                                                                .statusReasonDescription(statusDetailStatus.getValue())
                                                )
                                )
                )
                .links(LinksHelper.createInternationalPaymentDetailsLink(this.getClass(), frInternationalPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
