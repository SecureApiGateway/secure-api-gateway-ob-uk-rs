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
/**
 * NOTE: This class is auto generated by the swagger code generator program (2.3.1).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_3.domesticscheduledpayments;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRScheduledPaymentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDataDomesticScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.FRScheduledPaymentDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRDomesticScheduledPaymentSubmission;
import com.forgerock.sapi.gateway.ob.uk.rs.server.idempotency.IdempotentRepositoryAdapter;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticScheduledPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.scheduledpayment.ScheduledPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_3.domesticscheduledpayments.DomesticScheduledPaymentsApi;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.INITIATIONPENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBWriteDomesticScheduledResponse3DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConsentConverter.toOBWriteDomesticScheduled2DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConverter.toFRWriteDomesticScheduled;
import static com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.FRScheduledPaymentDataFactory.createFRScheduledPaymentData;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller("DomesticScheduledPaymentsApiV3.1.3")
@Slf4j
public class DomesticScheduledPaymentsApiController implements DomesticScheduledPaymentsApi {

    private final DomesticScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final ScheduledPaymentService scheduledPaymentService;

    public DomesticScheduledPaymentsApiController(
            DomesticScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            ScheduledPaymentService scheduledPaymentService) {
        this.scheduledPaymentSubmissionRepository = scheduledPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.scheduledPaymentService = scheduledPaymentService;
    }

    @Override
    public ResponseEntity<OBWriteDomesticScheduledResponse3> createDomesticScheduledPayments(
            @Valid OBWriteDomesticScheduled2 obWriteDomesticScheduled2,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            String xAccountId,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteDomesticScheduled2);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteDomesticScheduled2.getRisk());

        FRWriteDomesticScheduled frScheduledPayment = toFRWriteDomesticScheduled(obWriteDomesticScheduled2);
        log.trace("Converted to: '{}'", frScheduledPayment);

        FRDomesticScheduledPaymentSubmission frPaymentSubmission = FRDomesticScheduledPaymentSubmission.builder()
                .id(obWriteDomesticScheduled2.getData().getConsentId())
                .scheduledPayment(frScheduledPayment)
                .status(INITIATIONPENDING)
                .created(new Date())
                .updated(new Date())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the scheduled payment
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(scheduledPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);

        // Save the scheduled payment data for the Accounts API
        FRScheduledPaymentData scheduledPaymentData = FRScheduledPaymentDataFactory.createFRScheduledPaymentData(frScheduledPayment, xAccountId);
        scheduledPaymentService.createScheduledPayment(scheduledPaymentData);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getDomesticScheduledPaymentsDomesticScheduledPaymentId(
            String domesticScheduledPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRDomesticScheduledPaymentSubmission> isPaymentSubmission = scheduledPaymentSubmissionRepository.findById(domesticScheduledPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            // OB specifies a 400 when the id does not match an existing consent
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment submission '" + domesticScheduledPaymentId + "' can't be found");
        }

        FRDomesticScheduledPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getDomesticScheduledPaymentsDomesticScheduledPaymentIdPaymentDetails(
            String domesticScheduledPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRDomesticScheduledPaymentSubmission> isPaymentSubmission = scheduledPaymentSubmissionRepository.findById(domesticScheduledPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + domesticScheduledPaymentId + "' can't be found");
        }

        FRDomesticScheduledPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        log.debug("Found The Domestic Scheduled Payment '{}' to get details.", domesticScheduledPaymentId);

        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }

        return ResponseEntity.ok(responseEntityDetails(frPaymentSubmission));
    }

    private OBWriteDomesticScheduledResponse3 responseEntity(FRDomesticScheduledPaymentSubmission frPaymentSubmission) {
        FRWriteDataDomesticScheduled data = frPaymentSubmission.getScheduledPayment().getData();
        return new OBWriteDomesticScheduledResponse3()
                .data(new OBWriteDomesticScheduledResponse3Data()
                        .domesticScheduledPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBWriteDomesticScheduled2DataInitiation(data.getInitiation()))
                        .creationDateTime(new DateTime(frPaymentSubmission.getCreated().getTime()))
                        .statusUpdateDateTime(new DateTime(frPaymentSubmission.getUpdated().getTime()))
                        .status(toOBWriteDomesticScheduledResponse3DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId()))
                .links(LinksHelper.createDomesticScheduledPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

    private OBWritePaymentDetailsResponse1 responseEntityDetails(FRDomesticScheduledPaymentSubmission frPaymentSubmission) {
        OBWritePaymentDetailsResponse1DataPaymentStatus.StatusEnum status = OBWritePaymentDetailsResponse1DataPaymentStatus.StatusEnum.fromValue(
                PaymentsUtils.statusLinkingMap.get(frPaymentSubmission.getStatus().getValue())
        );
        String localInstrument = frPaymentSubmission.getScheduledPayment().getData().getInitiation().getLocalInstrument();

        // Build the response object with data to meet the expected data defined by the spec
        OBWritePaymentDetailsResponse1DataStatusDetail.StatusReasonEnum statusReasonEnum = OBWritePaymentDetailsResponse1DataStatusDetail.StatusReasonEnum.PENDINGSETTLEMENT;
        return new OBWritePaymentDetailsResponse1()
                .data(
                        new OBWritePaymentDetailsResponse1Data()
                                .addPaymentStatusItem(
                                        new OBWritePaymentDetailsResponse1DataPaymentStatus()
                                                .status(status)
                                                .paymentTransactionId(UUID.randomUUID().toString())
                                                .statusUpdateDateTime(new DateTime(frPaymentSubmission.getUpdated()))
                                                .statusDetail(
                                                        new OBWritePaymentDetailsResponse1DataStatusDetail()
                                                                .localInstrument(localInstrument)
                                                                .status(status.getValue())
                                                                .statusReason(statusReasonEnum)
                                                                .statusReasonDescription(statusReasonEnum.getValue())
                                                )
                                )

                )
                .links(LinksHelper.createDomesticScheduledPaymentDetailsLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
