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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_0.domesticpayments;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteDomestic;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.payment.FRDomesticPaymentSubmission;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_0.domesticpayments.DomesticPaymentsApi;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.PENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBTransactionIndividualStatus1Code;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConsentConverter.toOBDomestic1;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteDomesticConverter.toFRWriteDomestic;

@Controller("DomesticPaymentsApiV3.0")
@Slf4j
public class DomesticPaymentsApiController implements DomesticPaymentsApi {

    private final DomesticPaymentSubmissionRepository paymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;

    public DomesticPaymentsApiController(DomesticPaymentSubmissionRepository paymentSubmissionRepository,
                                         PaymentSubmissionValidator paymentSubmissionValidator) {
        this.paymentSubmissionRepository = paymentSubmissionRepository;
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
        //      - verify the consent status and consent version (not newer than this submission URL path)
        //      - verify the payment details match those in the payment consent
        //      - verify security concerns (e.g. detached JWS, access token, roles, MTLS etc.)

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteDomestic1.getRisk());

        FRWriteDomestic frDomesticPayment = toFRWriteDomestic(obWriteDomestic1);
        log.trace("Converted to: '{}'", frDomesticPayment);

        FRDomesticPaymentSubmission frPaymentSubmission = FRDomesticPaymentSubmission.builder()
                .id(obWriteDomestic1.getData().getConsentId())
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
        Optional<FRDomesticPaymentSubmission> isPaymentSubmission = paymentSubmissionRepository.findById(domesticPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment submission '" + domesticPaymentId + "' can't be found");
        }

        FRDomesticPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }

        return ResponseEntity.ok(responseEntity(frPaymentSubmission));
    }

    private OBWriteDomesticResponse1 responseEntity(FRDomesticPaymentSubmission frPaymentSubmission) {
        return new OBWriteDomesticResponse1()
                .data(new OBWriteDataDomesticResponse1()
                        .domesticPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBDomestic1(frPaymentSubmission.getPayment().getData().getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBTransactionIndividualStatus1Code(frPaymentSubmission.getStatus()))
                        .consentId(frPaymentSubmission.getPayment().getData().getConsentId()))
                .links(LinksHelper.createDomesticPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

}