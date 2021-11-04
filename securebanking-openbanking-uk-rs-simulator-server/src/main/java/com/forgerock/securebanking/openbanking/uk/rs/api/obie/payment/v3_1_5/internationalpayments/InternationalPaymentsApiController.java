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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_5.internationalpayments;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRReadRefundAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternational;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalData;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBVersion;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRInternationalPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalResponse5Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAccountIdentifierConverter.toOBCashAccountDebtor4;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.FRAccountIdentifierConverter.toOBDebtorIdentification1;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRExchangeRateConverter.toOBWriteInternationalConsentResponse6DataExchangeRateInformation;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRResponseDataRefundConverter.toOBWriteInternationalResponse5DataRefund;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRSubmissionStatusConverter.toOBWriteInternationalResponse5DataStatus;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteInternationalConsentConverter.toOBWriteInternational3DataInitiation;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteInternationalConverter.toFRWriteInternational;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRSubmissionStatus.PENDING;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.link.LinksHelper.createInternationalPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.common.refund.FRReadRefundAccountFactory.frReadRefundAccount;
import static com.forgerock.securebanking.openbanking.uk.rs.common.refund.FRResponseDataRefundFactory.frInternationalResponseDataRefund;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.PaymentApiResponseUtil.resourceConflictResponse;
import static com.forgerock.securebanking.openbanking.uk.rs.validator.ResourceVersionValidator.isAccessToResourceAllowed;
import static org.springframework.http.HttpStatus.*;

@Controller("InternationalPaymentsApiV3.1.5")
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
    public ResponseEntity<OBWriteInternationalResponse5> createInternationalPayments(
            @Valid OBWriteInternational3 obWriteInternational3,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xReadRefundAccount,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteInternational3);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternational3.getRisk());

        FRWriteInternational frInternationalPayment = toFRWriteInternational(obWriteInternational3);
        log.trace("Converted to: '{}'", frInternationalPayment);

        FRInternationalPaymentSubmission frPaymentSubmission = FRInternationalPaymentSubmission.builder()
                .id(obWriteInternational3.getData().getConsentId())
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
        return ResponseEntity.status(CREATED).body(responseEntity(frPaymentSubmission, frReadRefundAccount(xReadRefundAccount)));
    }

    @Override
    public ResponseEntity getInternationalPaymentsInternationalPaymentId(
            String internationalPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xReadRefundAccount,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalPaymentSubmission> isPaymentSubmission = paymentSubmissionRepository.findById(internationalPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + internationalPaymentId + "' can't be found");
        }

        FRInternationalPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntity(frPaymentSubmission, frReadRefundAccount(xReadRefundAccount)));
    }

    public ResponseEntity<OBWritePaymentDetailsResponse1> getInternationalPaymentsInternationalPaymentIdPaymentDetails(
            String internationalPaymentId,
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

    private OBWriteInternationalResponse5 responseEntity(FRInternationalPaymentSubmission frPaymentSubmission,
                                                         FRReadRefundAccount readRefundAccount) {
        FRWriteInternationalData data = frPaymentSubmission.getPayment().getData();
        Optional<FRInternationalResponseDataRefund> refund = frInternationalResponseDataRefund(readRefundAccount, data.getInitiation());
        return new OBWriteInternationalResponse5()
                .data(new OBWriteInternationalResponse5Data()
                        .internationalPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBWriteInternational3DataInitiation(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBWriteInternationalResponse5DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId())
                        .debtor(toOBCashAccountDebtor4(data.getInitiation().getDebtorAccount()))
                        .refund(refund.isPresent() ? toOBWriteInternationalResponse5DataRefund(refund.get()) : null)
                        .exchangeRateInformation(toOBWriteInternationalConsentResponse6DataExchangeRateInformation(
                                frPaymentSubmission.getCalculatedExchangeRate())))
                .links(createInternationalPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
