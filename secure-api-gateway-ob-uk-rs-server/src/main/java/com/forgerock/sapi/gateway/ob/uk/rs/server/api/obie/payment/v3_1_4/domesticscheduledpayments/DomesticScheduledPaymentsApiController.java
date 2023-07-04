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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_4.domesticscheduledpayments;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRScheduledPaymentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDataDomesticScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomesticScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_4.domesticscheduledpayments.DomesticScheduledPaymentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.FRScheduledPaymentDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.RiskValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.refund.FRResponseDataRefundFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.payment.FRDomesticScheduledPaymentSubmission;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticScheduledPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.scheduledpayment.ScheduledPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.INITIATIONPENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBWriteDomesticScheduledResponse4DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConsentConverter.toOBWriteDomesticScheduled2DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticScheduledConverter.toFRWriteDomesticScheduled;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@Controller("DomesticScheduledPaymentsApiV3.1.4")
@Slf4j
public class DomesticScheduledPaymentsApiController implements DomesticScheduledPaymentsApi {

    private final DomesticScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final ScheduledPaymentService scheduledPaymentService;

    private final ConsentService consentService;
    private final RiskValidationService riskValidationService;
    private final FRAccountRepository frAccountRepository;

    public DomesticScheduledPaymentsApiController(
            DomesticScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            ScheduledPaymentService scheduledPaymentService,
            ConsentService consentService,
            RiskValidationService riskValidationService,
            FRAccountRepository frAccountRepository
    ) {
        this.scheduledPaymentSubmissionRepository = scheduledPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.scheduledPaymentService = scheduledPaymentService;
        this.consentService = consentService;
        this.riskValidationService = riskValidationService;
        this.frAccountRepository = frAccountRepository;
    }

    @Override
    public ResponseEntity<OBWriteDomesticScheduledResponse4> createDomesticScheduledPayments(
            OBWriteDomesticScheduled2 obWriteDomesticScheduled2,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            String xAccountId,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteDomesticScheduled2);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteDomesticScheduled2.getRisk());

        String consentId = obWriteDomesticScheduled2.getData().getConsentId();
        //get the consent
        JsonObject intent = consentService.getIDMIntent(authorization, consentId);
        log.debug("Retrieved consent from IDM");

        //deserialize the intent to ob response object
        OBWriteDomesticScheduledConsentResponse4 obConsentResponse = consentService.deserialize(
                OBWriteDomesticScheduledConsentResponse4.class,
                intent.getAsJsonObject("OBIntentObject"),
                consentId
        );
        log.debug("Deserialized consent from IDM");

        FRWriteDomesticScheduled frScheduledPayment = toFRWriteDomesticScheduled(obWriteDomesticScheduled2);
        log.trace("Converted to: '{}'", frScheduledPayment);

        // validate the consent against the request
        log.debug("Validating Domestic Scheduled Payment submission");
        try {
            // validates the initiation
            if (!obWriteDomesticScheduled2.getData().getInitiation().equals(obConsentResponse.getData().getInitiation())) {
                throw new OBErrorException(OBRIErrorType.PAYMENT_INVALID_INITIATION,
                        "The initiation field from payment submitted does not match with the initiation field submitted for the consent"
                );
            }
            riskValidationService.validate(obConsentResponse.getRisk(), obWriteDomesticScheduled2.getRisk());
        } catch (OBErrorException e) {
            throw new OBErrorResponseException(
                    e.getObriErrorType().getHttpStatus(),
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    e.getOBError());
        }
        log.debug("Domestic Scheduled Payment validation successful");

        FRDomesticScheduledPaymentSubmission frPaymentSubmission = FRDomesticScheduledPaymentSubmission.builder()
                .id(obWriteDomesticScheduled2.getData().getConsentId())
                .scheduledPayment(frScheduledPayment)
                .status(INITIATIONPENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the scheduled payment
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(scheduledPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);

        // Save the scheduled payment data for the Accounts API
        FRScheduledPaymentData scheduledPaymentData = FRScheduledPaymentDataFactory.createFRScheduledPaymentData(frScheduledPayment, xAccountId);
        scheduledPaymentService.createScheduledPayment(scheduledPaymentData);

        OBWriteDomesticScheduledResponse4 entity = responseEntity(
                frPaymentSubmission,
                obConsentResponse
        );

        // update the entity with refund
        setRefund(obConsentResponse.getData().getReadRefundAccount(), intent, entity);

        return ResponseEntity.status(CREATED).body(entity);
    }

    @Override
    public ResponseEntity getDomesticScheduledPaymentsDomesticScheduledPaymentId(
            String domesticScheduledPaymentId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRDomesticScheduledPaymentSubmission> isPaymentSubmission = scheduledPaymentSubmissionRepository.findById(domesticScheduledPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            // OB specifies a 400 when the id does not match an existing consent
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + domesticScheduledPaymentId + "' can't be found");
        }

        FRDomesticScheduledPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        // Get the consent to update the response
        JsonObject intent = consentService.getIDMIntent(authorization, frPaymentSubmission.getConsentId());
        log.debug("Retrieved consent from IDM");

        // deserialize the intent to ob response object
        OBWriteDomesticScheduledConsentResponse4 obConsentResponse = consentService.deserialize(
                OBWriteDomesticScheduledConsentResponse4.class,
                intent.getAsJsonObject("OBIntentObject"),
                frPaymentSubmission.getConsentId()
        );

        OBWriteDomesticScheduledResponse4 entity = responseEntity(
                frPaymentSubmission,
                obConsentResponse
        );

        // update the entity with refund
        setRefund(obConsentResponse.getData().getReadRefundAccount(), intent, entity);

        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity getDomesticScheduledPaymentsDomesticScheduledPaymentIdPaymentDetails(
            String domesticScheduledPaymentId,
            String authorization,
            String xFapiAuthDate,
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

    private OBWriteDomesticScheduledResponse4 responseEntity(
            FRDomesticScheduledPaymentSubmission frPaymentSubmission,
            OBWriteDomesticScheduledConsentResponse4 obConsent
    ) {
        FRWriteDataDomesticScheduled data = frPaymentSubmission.getScheduledPayment().getData();
        return new OBWriteDomesticScheduledResponse4()
                .data(new OBWriteDomesticScheduledResponse4Data()
                        .charges(obConsent.getData().getCharges())
                        .domesticScheduledPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBWriteDomesticScheduled2DataInitiation(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBWriteDomesticScheduledResponse4DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId())
                )
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

    private void setRefund(
            OBReadRefundAccountEnum obReadRefundAccountEnum,
            JsonObject intent,
            OBWriteDomesticScheduledResponse4 entity
    ) {
        if (Objects.nonNull(obReadRefundAccountEnum) && obReadRefundAccountEnum.equals(OBReadRefundAccountEnum.YES)) {
            String accountId = Objects.nonNull(intent.get("accountId")) ? intent.get("accountId").getAsString() : null;
            log.debug("Account Id from consent '{}'", accountId);
            if (Objects.nonNull(accountId)) {
                FRAccount frAccount = Objects.nonNull(accountId) ? frAccountRepository.byAccountId(accountId) : null;
                FRAccountIdentifier frAccountIdentifier = Objects.nonNull(frAccount) ?
                        frAccount.getAccount().getFirstAccount() :
                        null;
                Optional<FRResponseDataRefund> refund = FRResponseDataRefundFactory.frResponseDataRefund(frAccountIdentifier);
                if(Objects.nonNull(refund)) {
                    entity.getData().setRefund(
                            refund.map(FRResponseDataRefundConverter::toOBWriteDomesticResponse4DataRefund).orElse(null)
                    );
                }
            }
        }
    }
}
