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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.internationalpayments;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternational;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRInternationalPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteInternational1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.LinksHelper.createInternationalPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRExchangeRateConverter.toOBExchangeRate2;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRSubmissionStatusConverter.toOBTransactionIndividualStatus1Code;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteInternationalConsentConverter.toOBInternational1;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteInternationalConverter.toFRWriteInternational;
import static com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRSubmissionStatus.PENDING;

@Controller("InternationalPaymentsApiV3.0")
@Slf4j
public class InternationalPaymentsApiController implements InternationalPaymentsApi {

    private final InternationalPaymentSubmissionRepository paymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;

    public InternationalPaymentsApiController(InternationalPaymentSubmissionRepository paymentSubmissionRepository,
                                              PaymentSubmissionValidator paymentSubmissionValidator) {
        this.paymentSubmissionRepository = paymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
    }

    @Override
    public ResponseEntity<OBWriteInternationalResponse1> createInternationalPayments(
            @Valid OBWriteInternational1 obWriteInternational1,
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
        log.debug("Received payment submission: '{}'", obWriteInternational1);

        // TODO - before we get this far, the IG will need to:
        //      - verify the consent status
        //      - verify the payment details match those in the payment consent
        //      - verify security concerns (e.g. detached JWS, access token, roles, MTLS etc.)

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternational1.getRisk());

        FRWriteInternational frInternationalPayment = toFRWriteInternational(obWriteInternational1);
        log.trace("Converted to: '{}'", frInternationalPayment);

        FRInternationalPaymentSubmission frPaymentSubmission = FRInternationalPaymentSubmission.builder()
                .id(UUID.randomUUID().toString())
                .payment(frInternationalPayment)
                .status(PENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international payment
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(paymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getInternationalPaymentsInternationalPaymentId(
            String internationalPaymentId,
            String xFapiFinancialId,
            String authorization,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalPaymentSubmission> isPaymentSubmission = paymentSubmissionRepository.findById(internationalPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment submission '" + internationalPaymentId + "' can't be found");
        }
        return ResponseEntity.ok(responseEntity(isPaymentSubmission.get()));
    }

    private OBWriteInternationalResponse1 responseEntity(FRInternationalPaymentSubmission frPaymentSubmission) {
        return new OBWriteInternationalResponse1().data(new OBWriteDataInternationalResponse1()
                .internationalPaymentId(frPaymentSubmission.getId())
                .initiation(toOBInternational1(frPaymentSubmission.getPayment().getData().getInitiation()))
                .creationDateTime(frPaymentSubmission.getCreated())
                .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                .status(toOBTransactionIndividualStatus1Code(frPaymentSubmission.getStatus()))
                .consentId(frPaymentSubmission.getPayment().getData().getConsentId())
                .exchangeRateInformation(toOBExchangeRate2(frPaymentSubmission.getCalculatedExchangeRate())))
                .links(createInternationalPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
