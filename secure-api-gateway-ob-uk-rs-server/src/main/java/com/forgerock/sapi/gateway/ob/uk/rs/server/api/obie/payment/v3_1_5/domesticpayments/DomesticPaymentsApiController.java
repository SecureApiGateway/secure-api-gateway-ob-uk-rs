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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_5.domesticpayments;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRReadRefundAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDataDomestic;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomestic;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.refund.FRReadRefundAccountFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.refund.FRResponseDataRefundFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.payment.FRDomesticPaymentSubmission;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_5.domesticpayments.DomesticPaymentsApi;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.PENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAccountIdentifierConverter.toOBCashAccountDebtor4;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBWriteDomesticResponse5DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter.toOBWriteDomestic2DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConverter.toFRWriteDomestic;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1DataStatusDetail.StatusReasonEnum.PENDINGSETTLEMENT;

@Controller("DomesticPaymentsApiV3.1.5")
@Slf4j
public class DomesticPaymentsApiController implements DomesticPaymentsApi {
    private final DomesticPaymentSubmissionRepository paymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final ConsentService consentService;

    public DomesticPaymentsApiController(
            DomesticPaymentSubmissionRepository paymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            ConsentService consentService
    ) {
        this.paymentSubmissionRepository = paymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.consentService = consentService;
    }

    @Override
    public ResponseEntity<OBWriteDomesticResponse5> createDomesticPayments(
            @Valid OBWriteDomestic2 obWriteDomestic2,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xReadRefundAccount,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteDomestic2);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteDomestic2.getRisk());

        FRWriteDomestic frDomesticPayment = toFRWriteDomestic(obWriteDomestic2);
        log.trace("Converted to: '{}'", frDomesticPayment);

        FRDomesticPaymentSubmission frPaymentSubmission = FRDomesticPaymentSubmission.builder()
                .id(obWriteDomestic2.getData().getConsentId())
                .payment(frDomesticPayment)
                .status(PENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the payment
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(paymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);
        // Get the consent to update the response
        OBWriteDomesticConsentResponse5 obConsent = consentService.getOBIntentObject(
                OBWriteDomesticConsentResponse5.class,
                authorization,
                obWriteDomestic2.getData().getConsentId()
        );
        return ResponseEntity.status(CREATED).body(
                responseEntity(frPaymentSubmission, FRReadRefundAccountFactory.frReadRefundAccount(xReadRefundAccount), obConsent)
        );
    }

    @Override
    public ResponseEntity getDomesticPaymentsDomesticPaymentId(
            String domesticPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xReadRefundAccount,
            HttpServletRequest request,
            Principal principal
    ) {

        Optional<FRDomesticPaymentSubmission> isPaymentSubmission = paymentSubmissionRepository.findById(domesticPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + domesticPaymentId + "' can't be found");
        }
        FRDomesticPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();

        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        // Get the consent to update the response
        OBWriteDomesticConsentResponse5 obConsent = consentService.getOBIntentObject(
                OBWriteDomesticConsentResponse5.class,
                authorization,
                domesticPaymentId
        );

        return ResponseEntity.ok(
                responseEntity(frPaymentSubmission, FRReadRefundAccountFactory.frReadRefundAccount(xReadRefundAccount), obConsent)
        );
    }

    @Override
    public ResponseEntity getDomesticPaymentsDomesticPaymentIdPaymentDetails(
            String domesticPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRDomesticPaymentSubmission> isPaymentSubmission = paymentSubmissionRepository.findById(domesticPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + domesticPaymentId + "' can't be found");
        }

        FRDomesticPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        log.debug("Found The Domestic Scheduled Payment '{}' to get details.", domesticPaymentId);

        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }

        return ResponseEntity.ok(responseEntityDetails(frPaymentSubmission));
    }

    private OBWriteDomesticResponse5 responseEntity(
            FRDomesticPaymentSubmission frPaymentSubmission,
            FRReadRefundAccount readRefundAccount,
            OBWriteDomesticConsentResponse5 obConsentResponse
    ) {
        FRWriteDataDomestic data = frPaymentSubmission.getPayment().getData();
        Optional<FRResponseDataRefund> refund = FRResponseDataRefundFactory.frDomesticResponseDataRefund(readRefundAccount, data.getInitiation());
        return new OBWriteDomesticResponse5()
                .data(new OBWriteDomesticResponse5Data()
                        .domesticPaymentId(frPaymentSubmission.getId())
                        .charges(obConsentResponse.getData().getCharges())
                        .initiation(toOBWriteDomestic2DataInitiation(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBWriteDomesticResponse5DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId())
                        .debtor(toOBCashAccountDebtor4(data.getInitiation().getDebtorAccount()))
                        .refund(refund.map(FRResponseDataRefundConverter::toOBWriteDomesticResponse5DataRefund).orElse(null)))
                .links(LinksHelper.createDomesticPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

    private OBWritePaymentDetailsResponse1 responseEntityDetails(FRDomesticPaymentSubmission frPaymentSubmission) {
        OBWritePaymentDetailsResponse1DataPaymentStatus.StatusEnum status = OBWritePaymentDetailsResponse1DataPaymentStatus.StatusEnum.fromValue(
                frPaymentSubmission.getStatus().getValue()
        );

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
                                                                .localInstrument(
                                                                        frPaymentSubmission.getPayment().getData()
                                                                                .getInitiation().getLocalInstrument()
                                                                )
                                                                .status(status.getValue())
                                                                // Build the response object with data to meet the expected data defined by the spec
                                                                .statusReason(PENDINGSETTLEMENT)
                                                                .statusReasonDescription(PENDINGSETTLEMENT.getValue())
                                                )
                                )

                )
                .links(LinksHelper.createDomesticPaymentDetailsLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}