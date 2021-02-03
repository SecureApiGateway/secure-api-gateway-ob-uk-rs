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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.domesticpayments;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomestic;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRDomesticPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticPaymentSubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.LinksHelper.createDomesticPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRSubmissionStatusConverter.toOBTransactionIndividualStatus1Code;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteDomesticConsentConverter.toOBDomestic1;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteDomesticConverter.toFRWriteDomestic;
import static com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRSubmissionStatus.PENDING;

@Controller("DomesticPaymentsApiV3.0")
@Slf4j
public class DomesticPaymentsApiController implements DomesticPaymentsApi {

    private final DomesticPaymentSubmissionRepository domesticPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;

    public DomesticPaymentsApiController(DomesticPaymentSubmissionRepository domesticPaymentSubmissionRepository,
                                         PaymentSubmissionValidator paymentSubmissionValidator) {
        this.domesticPaymentSubmissionRepository = domesticPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
    }

    @Override
    public ResponseEntity<OBWriteDomesticResponse1> createDomesticPayments(@Valid OBWriteDomestic1 obWriteDomestic1,
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
        log.debug("Received payment submission: '{}'", obWriteDomestic1);

        // TODO - before we get this far, the IG will need to:
        //      - verify the consent status
        //      - verify the payment details match those in the payment consent
        //      - verify security concerns (e.g. detached JWS, access token, roles, MTLS etc.)

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteDomestic1.getRisk());

        FRWriteDomestic frDomesticPayment = toFRWriteDomestic(obWriteDomestic1);
        log.trace("Converted to: '{}'", frDomesticPayment);
        FRDomesticPaymentSubmission frPaymentSubmission = FRDomesticPaymentSubmission.builder()
                // TODO - openbanking-aspsp uses the consent id - is this for convenience? Could it be a problem?
                .id(frDomesticPayment.getData().getConsentId())
                .domesticPayment(frDomesticPayment)
                .status(PENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(domesticPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getDomesticPaymentsDomesticPaymentId(String domesticPaymentId,
                                                               String xFapiFinancialId,
                                                               String authorization,
                                                               DateTime xFapiCustomerLastLoggedTime,
                                                               String xFapiCustomerIpAddress,
                                                               String xFapiInteractionId,
                                                               String xCustomerUserAgent,
                                                               HttpServletRequest request,
                                                               Principal principal
    ) {
        Optional<FRDomesticPaymentSubmission> isPaymentSubmission = domesticPaymentSubmissionRepository.findById(domesticPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment submission '" + domesticPaymentId + "' can't be found");
        }
        FRDomesticPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        return ResponseEntity.ok(responseEntity(frPaymentSubmission));
    }

    private OBWriteDomesticResponse1 responseEntity(FRDomesticPaymentSubmission frPaymentSubmission) {
        return new OBWriteDomesticResponse1()
                .data(new OBWriteDataDomesticResponse1()
                        .domesticPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBDomestic1(frPaymentSubmission.getDomesticPayment().getData().getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBTransactionIndividualStatus1Code(frPaymentSubmission.getStatus()))
                        .consentId(frPaymentSubmission.getDomesticPayment().getData().getConsentId()))
                .links(createDomesticPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

}
