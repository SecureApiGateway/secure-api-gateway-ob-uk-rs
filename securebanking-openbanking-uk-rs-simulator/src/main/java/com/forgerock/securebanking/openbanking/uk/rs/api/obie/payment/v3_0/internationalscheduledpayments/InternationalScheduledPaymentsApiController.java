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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.internationalscheduledpayments;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduled;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalScheduledData;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRInternationalScheduledPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalScheduledPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalScheduledResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduled1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.LinksHelper.createInternationalScheduledPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRExchangeRateConverter.toOBExchangeRate2;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRSubmissionStatusConverter.toOBExternalStatus1Code;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteInternationalScheduledConsentConverter.toOBInternationalScheduled1;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteInternationalScheduledConverter.toFRWriteInternationalScheduled;

@Controller("InternationalScheduledPaymentsApiV3.0")
@Slf4j
public class InternationalScheduledPaymentsApiController implements InternationalScheduledPaymentsApi {

    private final InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;

    public InternationalScheduledPaymentsApiController(
            InternationalScheduledPaymentSubmissionRepository scheduledPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator) {
        this.scheduledPaymentSubmissionRepository = scheduledPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
    }

    @Override
    public ResponseEntity<OBWriteInternationalScheduledResponse1> createInternationalScheduledPayments(
            @Valid OBWriteInternationalScheduled1 obWriteInternationalScheduled1,
            String xFapiFinancialId,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteInternationalScheduled1);

        // TODO - before we get this far, the IG will need to:
        //      - verify the consent status
        //      - verify the payment details match those in the payment consent
        //      - verify security concerns (e.g. detached JWS, access token, roles, MTLS etc.)

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternationalScheduled1.getRisk());

        FRWriteInternationalScheduled frScheduledPayment = toFRWriteInternationalScheduled(obWriteInternationalScheduled1);
        log.trace("Converted to: '{}'", frScheduledPayment);

        FRInternationalScheduledPaymentSubmission frPaymentSubmission = FRInternationalScheduledPaymentSubmission.builder()
                .id(UUID.randomUUID().toString())
                .scheduledPayment(frScheduledPayment)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international scheduled payment
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(scheduledPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);
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

        return ResponseEntity.ok(responseEntity(isPaymentSubmission.get()));
    }

    private OBWriteInternationalScheduledResponse1 responseEntity(FRInternationalScheduledPaymentSubmission frPaymentSubmission) {
        FRWriteInternationalScheduledData data = frPaymentSubmission.getScheduledPayment().getData();
        return new OBWriteInternationalScheduledResponse1()
                .data(new OBWriteDataInternationalScheduledResponse1()
                        .internationalScheduledPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBInternationalScheduled1(data.getInitiation()))
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
