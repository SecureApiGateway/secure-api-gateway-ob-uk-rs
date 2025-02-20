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
/**
 * NOTE: This class is auto generated by the swagger code generator program (2.3.1).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.internationalscheduledpayments;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.INITIATIONPENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRAccountIdentifierConverter.toOBCashAccountDebtor4;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRSubmissionStatusConverter.toOBWriteInternationalScheduledResponse6DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalScheduledConsentConverter.toOBWriteInternationalScheduled3DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalScheduledConsentConverter.toOBWriteInternationalScheduledConsent5;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteInternationalScheduledConverter.toFRWriteInternationalScheduled;
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
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRPaymentDetailsStatusConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRExchangeRateConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledData;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.internationalscheduledpayments.InternationalScheduledPaymentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.RefundAccountService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.IdempotentPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.SinglePaymentForConsentIdempotentPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteInternationalScheduled3Validator.OBWriteInternationalScheduled3ValidationContext;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.internationalscheduled.InternationalScheduledPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRInternationalScheduledPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.InternationalScheduledPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import jakarta.servlet.http.HttpServletRequest;
import uk.org.openbanking.datamodel.v4.common.Meta;
import uk.org.openbanking.datamodel.v4.common.OBStatusReason;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalScheduled3;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalScheduledResponse6;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalScheduledResponse6Data;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1PaymentStatusStatus;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1Status;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetails1StatusDetail;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.v4.payment.OBWritePaymentDetailsResponse1Data;

@Controller("InternationalScheduledPaymentsApiV4.0.0")
public class InternationalScheduledPaymentsApiController implements InternationalScheduledPaymentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final InternationalScheduledPaymentConsentStoreClient consentStoreClient;
    private final OBValidationService<OBWriteInternationalScheduled3ValidationContext> paymentValidator;
    private final RefundAccountService refundAccountService;
    private final IdempotentPaymentService<FRInternationalScheduledPaymentSubmission, FRWriteInternationalScheduled> idempotentPaymentService;

    public InternationalScheduledPaymentsApiController(
            InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            @Qualifier("v4.0.0RestInternationalScheduledPaymentConsentStoreClient") InternationalScheduledPaymentConsentStoreClient consentStoreClient,
            OBValidationService<OBWriteInternationalScheduled3ValidationContext> paymentValidator,
            RefundAccountService refundAccountService) {
        this.scheduledPaymentSubmissionRepository = scheduledPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.consentStoreClient = consentStoreClient;
        this.paymentValidator = paymentValidator;
        this.refundAccountService = refundAccountService;
        this.idempotentPaymentService = new SinglePaymentForConsentIdempotentPaymentService<>(scheduledPaymentSubmissionRepository);
    }

    @Override
    public ResponseEntity<OBWriteInternationalScheduledResponse6> createInternationalScheduledPayments(
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            OBWriteInternationalScheduled3 obWriteInternationalScheduled3,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException, OBErrorException {
        logger.debug("Received payment submission: '{}'", obWriteInternationalScheduled3);

        paymentSubmissionValidator.validateIdempotencyKey(xIdempotencyKey);

        String consentId = obWriteInternationalScheduled3.getData().getConsentId();
        //get the consent
        logger.debug("Attempting to get consent: {}, clientId: {}", consentId, apiClientId);
        final InternationalScheduledPaymentConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        logger.debug("Got consent from store: {}", consent);

        FRWriteInternationalScheduled frScheduledPayment = toFRWriteInternationalScheduled(obWriteInternationalScheduled3);
        logger.trace("Converted to: '{}'", frScheduledPayment);

        final Optional<FRInternationalScheduledPaymentSubmission> existingPayment =
                idempotentPaymentService.findExistingPayment(frScheduledPayment, consentId, apiClientId, xIdempotencyKey);
        if (existingPayment.isPresent()) {
            logger.info("Payment submission is a replay of a previous payment, returning previously created payment for x-idempotencyKey: {}, consentId: {}",
                    xIdempotencyKey, consentId);
            return ResponseEntity.status(CREATED).body(responseEntity(consent, existingPayment.get()));
        }

        // validate the consent against the request
        logger.debug("Validating International Payment submission");
        paymentValidator.validate(new OBWriteInternationalScheduled3ValidationContext(obWriteInternationalScheduled3,
                toOBWriteInternationalScheduledConsent5(consent.getRequestObj()), consent.getStatus()));
        logger.debug("International Payment validation successful");

        FRInternationalScheduledPaymentSubmission frPaymentSubmission = FRInternationalScheduledPaymentSubmission.builder()
                .id(obWriteInternationalScheduled3.getData().getConsentId())
                .scheduledPayment(frScheduledPayment)
                .status(INITIATIONPENDING)
                .created(new Date())
                .updated(new Date())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international scheduled payment
        frPaymentSubmission = idempotentPaymentService.savePayment(frPaymentSubmission);

        final ConsumePaymentConsentRequest consumePaymentRequest = new ConsumePaymentConsentRequest();
        consumePaymentRequest.setConsentId(consentId);
        consumePaymentRequest.setApiClientId(apiClientId);
        consentStoreClient.consumeConsent(consumePaymentRequest);

        OBWriteInternationalScheduledResponse6 entity = responseEntity(consent, frPaymentSubmission);

        return ResponseEntity.status(CREATED).body(entity);
    }

    @Override
    public ResponseEntity getInternationalScheduledPaymentsInternationalScheduledPaymentId(
            String internationalScheduledPaymentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalScheduledPaymentSubmission> isPaymentSubmission = scheduledPaymentSubmissionRepository.findById(internationalScheduledPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + internationalScheduledPaymentId + "' can't be found");
        }

        FRInternationalScheduledPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        final String consentId = frPaymentSubmission.getScheduledPayment().getData().getConsentId();

        logger.debug("Attempting to get consent: {}, clientId: {}", consentId, apiClientId);
        final InternationalScheduledPaymentConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        logger.debug("Got consent from store: {}", consent);

        OBWriteInternationalScheduledResponse6 entity = responseEntity(consent, frPaymentSubmission);

        return ResponseEntity.ok(entity);
    }

    public ResponseEntity getInternationalScheduledPaymentsInternationalScheduledPaymentIdPaymentDetails(
            String internationalScheduledPaymentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClient,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalScheduledPaymentSubmission> isInternationalScheduledPaymentSubmission = scheduledPaymentSubmissionRepository.findById(internationalScheduledPaymentId);
        if (!isInternationalScheduledPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("International scheduled payment submission '" + internationalScheduledPaymentId + "' can't be found");
        }

        FRInternationalScheduledPaymentSubmission frInternationalScheduledPaymentSubmission = isInternationalScheduledPaymentSubmission.get();
        logger.debug("Found The International Scheduled Payment '{}' to get details.", internationalScheduledPaymentId);
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frInternationalScheduledPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frInternationalScheduledPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntityDetails(frInternationalScheduledPaymentSubmission));
    }

    private OBWriteInternationalScheduledResponse6 responseEntity(
            InternationalScheduledPaymentConsent consent,
            FRInternationalScheduledPaymentSubmission frPaymentSubmission
    ) {

        FRWriteInternationalScheduledData data = frPaymentSubmission.getScheduledPayment().getData();
        OBWriteInternationalScheduledResponse6Data responseData = new OBWriteInternationalScheduledResponse6Data();

        final Optional<FRInternationalResponseDataRefund> refundAccountData = refundAccountService.getInternationalPaymentRefundData(
                consent.getRequestObj().getData().getReadRefundAccount(),
                consent.getRequestObj().getData().getInitiation().getCreditor(),
                consent.getRequestObj().getData().getInitiation().getCreditorAgent(),
                consent);

        return new OBWriteInternationalScheduledResponse6()
                .data(new OBWriteInternationalScheduledResponse6Data()
                        .charges(toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()))
                        .internationalScheduledPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBWriteInternationalScheduled3DataInitiation(data.getInitiation()))
                        .creationDateTime(new DateTime(frPaymentSubmission.getCreated().getTime()))
                        .statusUpdateDateTime(new DateTime(frPaymentSubmission.getUpdated().getTime()))
                        .status(toOBWriteInternationalScheduledResponse6DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(frPaymentSubmission.getScheduledPayment().getData().getConsentId())
                        .debtor(toOBCashAccountDebtor4(data.getInitiation().getDebtorAccount()))
                        .expectedExecutionDateTime(data.getInitiation().getRequestedExecutionDateTime())
                        .refund(refundAccountData.map(FRResponseDataRefundConverter::toOBWriteInternationalScheduledResponse6DataRefund).orElse(null))
                        .exchangeRateInformation(FRExchangeRateConverter.toOBWriteInternationalConsentResponse6DataExchangeRateInformation(consent.getExchangeRateInformation()))
                        .statusReason(Collections.singletonList(FRModelMapper.map(responseData.getStatusReason(), OBStatusReason.class))))

                .links(LinksHelper.createInternationalScheduledPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

    private OBWritePaymentDetailsResponse1 responseEntityDetails(FRInternationalScheduledPaymentSubmission frInternationalScheduledPaymentSubmission) {
        OBWritePaymentDetails1Status status = FRPaymentDetailsStatusConverter.toOBPaymentDetailsStatus(
                frInternationalScheduledPaymentSubmission.getStatus().getValue());

        // Build the response object with data to meet the expected data defined by the spec
        OBWritePaymentDetails1PaymentStatusStatus statusReasonEnum = OBWritePaymentDetails1PaymentStatusStatus.PDNG;
        return new OBWritePaymentDetailsResponse1()
                .data(
                        new OBWritePaymentDetailsResponse1Data()
                                .addPaymentStatusItem(
                                        new OBWritePaymentDetails1()
                                                .status(status)
                                                .paymentTransactionId(UUID.randomUUID().toString())
                                                .statusUpdateDateTime(new DateTime(frInternationalScheduledPaymentSubmission.getUpdated()))
                                                .statusDetail(
                                                        new OBWritePaymentDetails1StatusDetail()
                                                                .status(FRPaymentDetailsStatusConverter.toOBWritePaymentDetails1StatusDetailStatus(
                                                                        frInternationalScheduledPaymentSubmission.getStatus().getValue()))
                                                                .statusReason(statusReasonEnum.getValue())
                                                                .statusReasonDescription(statusReasonEnum.getValue())
                                                                .localInstrument(frInternationalScheduledPaymentSubmission.getScheduledPayment().getData().getInitiation().getLocalInstrument())
                                                )
                                )
                )
                .links(LinksHelper.createInternationalScheduledPaymentDetailsLink(this.getClass(), frInternationalScheduledPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
