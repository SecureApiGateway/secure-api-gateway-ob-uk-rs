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
/**
 * NOTE: This class is auto generated by the swagger code generator program (2.3.1).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_5.internationalscheduledpayments;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.INITIATIONPENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAccountIdentifierConverter.toOBCashAccountDebtor4;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBWriteInternationalScheduledResponse6DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter.toOBWriteInternationalScheduled3DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter.toOBWriteInternationalScheduledConsent5;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConverter.toFRWriteInternationalScheduled;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRScheduledPaymentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRExchangeRateConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledData;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_5.internationalscheduledpayments.InternationalScheduledPaymentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.FRScheduledPaymentDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.RefundAccountService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.scheduledpayment.ScheduledPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternationalScheduled3Validator.OBWriteInternationalScheduled3ValidationContext;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.internationalscheduled.v3_1_10.InternationalScheduledPaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRInternationalScheduledPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.InternationalScheduledPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduled3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledResponse6;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledResponse6Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1DataPaymentStatus;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1DataStatusDetail;

@Controller("InternationalScheduledPaymentsApiV3.1.5")
@Slf4j
public class InternationalScheduledPaymentsApiController implements InternationalScheduledPaymentsApi {

    private final InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final ScheduledPaymentService scheduledPaymentService;

    private final InternationalScheduledPaymentConsentStoreClient consentStoreClient;

    private final OBValidationService<OBWriteInternationalScheduled3ValidationContext> paymentValidator;

    private final RefundAccountService refundAccountService;

    public InternationalScheduledPaymentsApiController(
            InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            ScheduledPaymentService scheduledPaymentService,
            InternationalScheduledPaymentConsentStoreClient consentStoreClient,
            OBValidationService<OBWriteInternationalScheduled3ValidationContext> paymentValidator,
            RefundAccountService refundAccountService) {
        this.scheduledPaymentSubmissionRepository = scheduledPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.scheduledPaymentService = scheduledPaymentService;
        this.consentStoreClient = consentStoreClient;
        this.paymentValidator = paymentValidator;
        this.refundAccountService = refundAccountService;
    }

    @Override
    public ResponseEntity<OBWriteInternationalScheduledResponse6> createInternationalScheduledPayments(
            @Valid OBWriteInternationalScheduled3 obWriteInternationalScheduled3,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteInternationalScheduled3);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternationalScheduled3.getRisk());

        String consentId = obWriteInternationalScheduled3.getData().getConsentId();
        //get the consent
        log.debug("Attempting to get consent: {}, clientId: {}", consentId, apiClientId);
        final InternationalScheduledPaymentConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        log.debug("Got consent from store: {}", consent);

        FRWriteInternationalScheduled frScheduledPayment = toFRWriteInternationalScheduled(obWriteInternationalScheduled3);
        log.trace("Converted to: '{}'", frScheduledPayment);

        // validate the consent against the request
        log.debug("Validating International Payment submission");
        paymentValidator.validate(new OBWriteInternationalScheduled3ValidationContext(obWriteInternationalScheduled3,
                toOBWriteInternationalScheduledConsent5(consent.getRequestObj()), consent.getStatus()));
        log.debug("International Payment validation successful");

        FRInternationalScheduledPaymentSubmission frPaymentSubmission = FRInternationalScheduledPaymentSubmission.builder()
                .id(obWriteInternationalScheduled3.getData().getConsentId())
                .scheduledPayment(frScheduledPayment)
                .status(INITIATIONPENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international scheduled payment
       frPaymentSubmission = scheduledPaymentSubmissionRepository.save(frPaymentSubmission);

        // Save the scheduled payment data for the Accounts API
        FRScheduledPaymentData scheduledPaymentData = FRScheduledPaymentDataFactory.createFRScheduledPaymentData(frScheduledPayment,
                consent.getAuthorisedDebtorAccountId());
        scheduledPaymentService.createScheduledPayment(scheduledPaymentData);

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
            DateTime xFapiAuthDate,
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

        log.debug("Attempting to get consent: {}, clientId: {}", consentId, apiClientId);
        final InternationalScheduledPaymentConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        log.debug("Got consent from store: {}", consent);

        OBWriteInternationalScheduledResponse6 entity = responseEntity(consent, frPaymentSubmission);

        return ResponseEntity.ok(entity);
    }

    public ResponseEntity getInternationalScheduledPaymentsInternationalScheduledPaymentIdPaymentDetails(
            String internationalScheduledPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
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
        log.debug("Found The International Scheduled Payment '{}' to get details.", internationalScheduledPaymentId);
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
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBWriteInternationalScheduledResponse6DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(frPaymentSubmission.getScheduledPayment().getData().getConsentId())
                        .debtor(toOBCashAccountDebtor4(data.getInitiation().getDebtorAccount()))
                        .expectedExecutionDateTime(data.getInitiation().getRequestedExecutionDateTime())
                        .refund(refundAccountData.map(FRResponseDataRefundConverter::toOBWriteInternationalResponse5DataRefund).orElse(null))
                        .exchangeRateInformation(FRExchangeRateConverter.toOBWriteInternationalConsentResponse6DataExchangeRateInformation(consent.getExchangeRateInformation()))
                )
                .links(LinksHelper.createInternationalScheduledPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

    private OBWritePaymentDetailsResponse1 responseEntityDetails(FRInternationalScheduledPaymentSubmission frInternationalScheduledPaymentSubmission) {
        OBWritePaymentDetailsResponse1DataPaymentStatus.StatusEnum status = OBWritePaymentDetailsResponse1DataPaymentStatus.StatusEnum.fromValue(
                PaymentsUtils.statusLinkingMap.get(frInternationalScheduledPaymentSubmission.getStatus().getValue())
        );

        // Build the response object with data to meet the expected data defined by the spec
        OBWritePaymentDetailsResponse1DataStatusDetail.StatusReasonEnum statusReasonEnum = OBWritePaymentDetailsResponse1DataStatusDetail.StatusReasonEnum.PENDINGSETTLEMENT;
        return new OBWritePaymentDetailsResponse1()
                .data(
                        new OBWritePaymentDetailsResponse1Data()
                                .addPaymentStatusItem(
                                        new OBWritePaymentDetailsResponse1DataPaymentStatus()
                                                .status(status)
                                                .paymentTransactionId(UUID.randomUUID().toString())
                                                .statusUpdateDateTime(new DateTime(frInternationalScheduledPaymentSubmission.getUpdated()))
                                                .statusDetail(
                                                        new OBWritePaymentDetailsResponse1DataStatusDetail()
                                                                .status(status.getValue())
                                                                .statusReason(statusReasonEnum)
                                                                .statusReasonDescription(statusReasonEnum.getValue())
                                                                .localInstrument(frInternationalScheduledPaymentSubmission.getScheduledPayment().getData().getInitiation().getLocalInstrument())
                                                )
                                )
                )
                .links(LinksHelper.createInternationalScheduledPaymentDetailsLink(this.getClass(), frInternationalScheduledPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
