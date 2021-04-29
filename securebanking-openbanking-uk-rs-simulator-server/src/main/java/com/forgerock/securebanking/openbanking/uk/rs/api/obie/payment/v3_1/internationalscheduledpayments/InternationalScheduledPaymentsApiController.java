/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1.internationalscheduledpayments;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRScheduledPaymentData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduled;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduledData;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBVersion;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRInternationalScheduledPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalScheduledPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.service.scheduledpayment.ScheduledPaymentService;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalScheduledResponse2;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduled2;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledResponse2;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRExchangeRateConverter.toOBExchangeRate2;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRSubmissionStatusConverter.toOBExternalStatus1Code;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteInternationalScheduledConsentConverter.toOBInternationalScheduled2;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteInternationalScheduledConverter.toFRWriteInternationalScheduled;
import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.FRScheduledPaymentDataFactory.createFRScheduledPaymentData;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.link.LinksHelper.createInternationalScheduledPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.PaymentApiResponseUtil.resourceConflictResponse;
import static com.forgerock.securebanking.openbanking.uk.rs.validator.ResourceVersionValidator.isAccessToResourceAllowed;

@Controller("InternationalScheduledPaymentsApiV3.1")
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
    public ResponseEntity<OBWriteInternationalScheduledResponse2> createInternationalScheduledPayments(
            @Valid OBWriteInternationalScheduled2 obWriteInternationalScheduled2,
            String xFapiFinancialId,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            String xAccountId,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteInternationalScheduled2);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternationalScheduled2.getRisk());

        FRWriteInternationalScheduled frScheduledPayment = toFRWriteInternationalScheduled(obWriteInternationalScheduled2);
        log.trace("Converted to: '{}'", frScheduledPayment);

        FRInternationalScheduledPaymentSubmission frPaymentSubmission = FRInternationalScheduledPaymentSubmission.builder()
                .id(obWriteInternationalScheduled2.getData().getConsentId())
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

        return ResponseEntity.status(HttpStatus.CREATED).body(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getInternationalScheduledPaymentsInternationalScheduledPaymentId(
            String internationalScheduledPaymentId,
            String xFapiFinancialId,
            String authorization,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalScheduledPaymentSubmission> isPaymentSubmission = scheduledPaymentSubmissionRepository.findById(internationalScheduledPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment submission '" + internationalScheduledPaymentId + "' can't be found");
        }

        FRInternationalScheduledPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntity(frPaymentSubmission));
    }

    private OBWriteInternationalScheduledResponse2 responseEntity(FRInternationalScheduledPaymentSubmission frPaymentSubmission) {
        FRWriteInternationalScheduledData data = frPaymentSubmission.getScheduledPayment().getData();
        return new OBWriteInternationalScheduledResponse2()
                .data(new OBWriteDataInternationalScheduledResponse2()
                        .internationalScheduledPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBInternationalScheduled2(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .consentId(frPaymentSubmission.getScheduledPayment().getData().getConsentId())
                        .status(toOBExternalStatus1Code(frPaymentSubmission.getStatus()))
                        .exchangeRateInformation(toOBExchangeRate2(frPaymentSubmission.getCalculatedExchangeRate()))
                        .expectedExecutionDateTime(data.getInitiation().getRequestedExecutionDateTime())
                )
                .links(createInternationalScheduledPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

}
