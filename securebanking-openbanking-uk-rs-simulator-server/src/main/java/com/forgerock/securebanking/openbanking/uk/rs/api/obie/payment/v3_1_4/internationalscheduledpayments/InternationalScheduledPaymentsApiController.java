/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_4.internationalscheduledpayments;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRScheduledPaymentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.common.FRResponseDataRefundConverter;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRReadRefundAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduled;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduledData;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRInternationalScheduledPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalScheduledPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.service.scheduledpayment.ScheduledPaymentService;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduled3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledResponse5Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRExchangeRateConverter.toOBWriteInternationalConsentResponse5DataExchangeRateInformation;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.common.FRSubmissionStatusConverter.toOBWriteInternationalScheduledResponse5DataStatus;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter.toOBWriteInternationalScheduled3DataInitiation;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteInternationalScheduledConverter.toFRWriteInternationalScheduled;
import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.factories.FRScheduledPaymentDataFactory.createFRScheduledPaymentData;
import static com.forgerock.securebanking.openbanking.uk.rs.common.refund.FRReadRefundAccountFactory.frReadRefundAccount;
import static com.forgerock.securebanking.openbanking.uk.rs.common.refund.FRResponseDataRefundFactory.frInternationalResponseDataRefund;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.PaymentApiResponseUtil.resourceConflictResponse;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.link.LinksHelper.createInternationalScheduledPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.validator.ResourceVersionValidator.isAccessToResourceAllowed;
import static org.springframework.http.HttpStatus.*;

@Controller("InternationalScheduledPaymentsApiV3.1.4")
@Slf4j
public class InternationalScheduledPaymentsApiController implements InternationalScheduledPaymentsApi {

    private final InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final ScheduledPaymentService scheduledPaymentService;

    public InternationalScheduledPaymentsApiController(
            InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            ScheduledPaymentService scheduledPaymentService) {
        this.scheduledPaymentSubmissionRepository = scheduledPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.scheduledPaymentService = scheduledPaymentService;
    }

    @Override
    public ResponseEntity<OBWriteInternationalScheduledResponse5> createInternationalScheduledPayments(
            @Valid OBWriteInternationalScheduled3 obWriteInternationalScheduled3,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            String xAccountId,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xReadRefundAccount,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteInternationalScheduled3);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternationalScheduled3.getRisk());

        FRWriteInternationalScheduled frScheduledPayment = toFRWriteInternationalScheduled(obWriteInternationalScheduled3);
        log.trace("Converted to: '{}'", frScheduledPayment);

        FRInternationalScheduledPaymentSubmission frPaymentSubmission = FRInternationalScheduledPaymentSubmission.builder()
                .id(obWriteInternationalScheduled3.getData().getConsentId())
                .scheduledPayment(frScheduledPayment)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international scheduled payment
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(scheduledPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);

        // Save the scheduled payment data for the Accounts API
        FRScheduledPaymentData scheduledPaymentData = createFRScheduledPaymentData(frScheduledPayment, xAccountId);
        scheduledPaymentService.createScheduledPayment(scheduledPaymentData);

        return ResponseEntity.status(CREATED).body(responseEntity(frPaymentSubmission, frReadRefundAccount(xReadRefundAccount)));
    }

    @Override
    public ResponseEntity getInternationalScheduledPaymentsInternationalScheduledPaymentId(
            String internationalScheduledPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xReadRefundAccount,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalScheduledPaymentSubmission> isPaymentSubmission = scheduledPaymentSubmissionRepository.findById(internationalScheduledPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + internationalScheduledPaymentId + "' can't be found");
        }

        FRInternationalScheduledPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntity(frPaymentSubmission, frReadRefundAccount(xReadRefundAccount)));
    }

    @Override
    public ResponseEntity<OBWritePaymentDetailsResponse1> getInternationalScheduledPaymentsInternationalScheduledPaymentIdPaymentDetails(
            String internationalScheduledPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        // Optional endpoint - not implemented
        return new ResponseEntity<>(NOT_IMPLEMENTED);
    }

    private OBWriteInternationalScheduledResponse5 responseEntity(FRInternationalScheduledPaymentSubmission frPaymentSubmission,
                                                                  FRReadRefundAccount readRefundAccount) {
        FRWriteInternationalScheduledData data = frPaymentSubmission.getScheduledPayment().getData();
        Optional<FRInternationalResponseDataRefund> refund = frInternationalResponseDataRefund(readRefundAccount, data.getInitiation());
        return new OBWriteInternationalScheduledResponse5()
                .data(new OBWriteInternationalScheduledResponse5Data()
                        .internationalScheduledPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBWriteInternationalScheduled3DataInitiation(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .consentId(frPaymentSubmission.getScheduledPayment().getData().getConsentId())
                        .refund(refund.map(FRResponseDataRefundConverter::toOBWriteInternationalResponse4DataRefund).orElse(null))
                        .status(toOBWriteInternationalScheduledResponse5DataStatus(frPaymentSubmission.getStatus()))
                        .exchangeRateInformation(toOBWriteInternationalConsentResponse5DataExchangeRateInformation(
                                frPaymentSubmission.getCalculatedExchangeRate()))
                        .expectedExecutionDateTime(data.getInitiation().getRequestedExecutionDateTime())
                )
                .links(createInternationalScheduledPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
