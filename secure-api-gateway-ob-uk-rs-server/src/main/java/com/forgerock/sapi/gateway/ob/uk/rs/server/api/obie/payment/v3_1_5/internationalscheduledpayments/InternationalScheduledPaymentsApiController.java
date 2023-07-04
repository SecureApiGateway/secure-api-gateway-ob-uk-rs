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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRScheduledPaymentData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduled;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledData;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalScheduledDataInitiation;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_5.internationalscheduledpayments.InternationalScheduledPaymentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.FRScheduledPaymentDataFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.RiskValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.refund.FRResponseDataRefundFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentsUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.payment.FRInternationalScheduledPaymentSubmission;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.InternationalScheduledPaymentSubmissionRepository;
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
import javax.validation.Valid;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.INITIATIONPENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAccountIdentifierConverter.toOBCashAccountDebtor4;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBWriteInternationalScheduledResponse6DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter.toOBWriteInternationalScheduled3DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalScheduledConverter.toFRWriteInternationalScheduled;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@Controller("InternationalScheduledPaymentsApiV3.1.5")
@Slf4j
public class InternationalScheduledPaymentsApiController implements InternationalScheduledPaymentsApi {

    private final InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final ScheduledPaymentService scheduledPaymentService;
    private final RiskValidationService riskValidationService;
    private final ConsentService consentService;
    private final FRAccountRepository frAccountRepository;

    public InternationalScheduledPaymentsApiController(
            InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository,
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
    public ResponseEntity<OBWriteInternationalScheduledResponse6> createInternationalScheduledPayments(
            @Valid OBWriteInternationalScheduled3 obWriteInternationalScheduled3,
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
        log.debug("Received payment submission: '{}'", obWriteInternationalScheduled3);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternationalScheduled3.getRisk());

        String consentId = obWriteInternationalScheduled3.getData().getConsentId();
        //get the consent
        JsonObject intent = consentService.getIDMIntent(authorization, consentId);
        log.debug("Retrieved consent from IDM");

        //deserialize the intent to ob response object
        OBWriteInternationalScheduledConsentResponse5 obConsentResponse5 = consentService.deserialize(
                OBWriteInternationalScheduledConsentResponse5.class,
                intent.getAsJsonObject("OBIntentObject"),
                consentId
        );
        log.debug("Deserialized consent from IDM");

        FRWriteInternationalScheduled frScheduledPayment = toFRWriteInternationalScheduled(obWriteInternationalScheduled3);
        log.trace("Converted to: '{}'", frScheduledPayment);

        // validate the consent against the request
        log.debug("Validating International Scheduled Payment submission");
        try {
            // validates the initiation
            if (!obWriteInternationalScheduled3.getData().getInitiation().equals(obConsentResponse5.getData().getInitiation())) {
                throw new OBErrorException(OBRIErrorType.PAYMENT_INVALID_INITIATION,
                        "The initiation field from payment submitted does not match with the initiation field submitted for the consent"
                );
            }
            riskValidationService.validate(obConsentResponse5.getRisk(), obWriteInternationalScheduled3.getRisk());
        } catch (OBErrorException e) {
            throw new OBErrorResponseException(
                    e.getObriErrorType().getHttpStatus(),
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    e.getOBError());
        }
        log.debug("International Scheduled Payment validation successful");

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
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(scheduledPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);

        // Save the scheduled payment data for the Accounts API
        FRScheduledPaymentData scheduledPaymentData = FRScheduledPaymentDataFactory.createFRScheduledPaymentData(frScheduledPayment, xAccountId);
        scheduledPaymentService.createScheduledPayment(scheduledPaymentData);
        // Get the consent to update the response
        OBWriteInternationalScheduledConsentResponse6 obConsentResponse6 = consentService.deserialize(
                OBWriteInternationalScheduledConsentResponse6.class,
                intent.getAsJsonObject("OBIntentObject"),
                consentId
        );

        OBWriteInternationalScheduledResponse6 entity = responseEntity(frPaymentSubmission, obConsentResponse6);

        // update the entity with refund
        setRefund(
                obConsentResponse5.getData().getReadRefundAccount(),
                frPaymentSubmission.getScheduledPayment().getData().getInitiation(),
                intent,
                entity
        );
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

        //get the consent
        JsonObject intent = consentService.getIDMIntent(authorization, frPaymentSubmission.getConsentId());
        log.debug("Retrieved consent from IDM");

        // Get the consent to update the response
        OBWriteInternationalScheduledConsentResponse6 obConsentResponse6 = consentService.deserialize(
                OBWriteInternationalScheduledConsentResponse6.class,
                intent.getAsJsonObject("OBIntentObject"),
                frPaymentSubmission.getConsentId()
        );
        OBWriteInternationalScheduledResponse6 entity = responseEntity(frPaymentSubmission, obConsentResponse6);

        // update the entity with refund
        setRefund(
                obConsentResponse6.getData().getReadRefundAccount(),
                frPaymentSubmission.getScheduledPayment().getData().getInitiation(),
                intent,
                entity
        );
        return ResponseEntity.ok(entity);
    }

    public ResponseEntity getInternationalScheduledPaymentsInternationalScheduledPaymentIdPaymentDetails(
            String internationalScheduledPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
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
            FRInternationalScheduledPaymentSubmission frPaymentSubmission,
            OBWriteInternationalScheduledConsentResponse6 obConsent
    ) {
        FRWriteInternationalScheduledData data = frPaymentSubmission.getScheduledPayment().getData();
        return new OBWriteInternationalScheduledResponse6()
                .data(new OBWriteInternationalScheduledResponse6Data()
                        .charges(obConsent.getData().getCharges())
                        .internationalScheduledPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBWriteInternationalScheduled3DataInitiation(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBWriteInternationalScheduledResponse6DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(frPaymentSubmission.getScheduledPayment().getData().getConsentId())
                        .debtor(toOBCashAccountDebtor4(data.getInitiation().getDebtorAccount()))
                        .exchangeRateInformation(obConsent.getData().getExchangeRateInformation())
                        .expectedExecutionDateTime(data.getInitiation().getRequestedExecutionDateTime())
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

    private void setRefund(
            OBReadRefundAccountEnum obReadRefundAccountEnum,
            FRWriteInternationalScheduledDataInitiation initiation,
            JsonObject intent,
            OBWriteInternationalScheduledResponse6 entity
    ) {
        if (Objects.nonNull(obReadRefundAccountEnum) && obReadRefundAccountEnum.equals(OBReadRefundAccountEnum.YES)) {
            String accountId = Objects.nonNull(intent.get("accountId")) ? intent.get("accountId").getAsString() : null;
            log.debug("Account Id from consent '{}'", accountId);
            if (Objects.nonNull(accountId)) {
                FRAccount frAccount = Objects.nonNull(accountId) ? frAccountRepository.byAccountId(accountId) : null;
                FRAccountIdentifier frAccountIdentifier = Objects.nonNull(frAccount) ?
                        frAccount.getAccount().getFirstAccount() :
                        null;
                Optional<FRInternationalResponseDataRefund> refund = FRResponseDataRefundFactory.frInternationalResponseDataRefund(
                        frAccountIdentifier,
                        initiation
                );

                if(Objects.nonNull(refund)) {
                    entity.getData().setRefund(
                            refund.map(FRResponseDataRefundConverter::toOBWriteInternationalResponse5DataRefund).orElse(null)
                    );
                }
            }
        }
    }
}
