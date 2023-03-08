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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_0.file;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteFile;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.payment.FRFilePaymentSubmission;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.FilePaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_0.file.FilePaymentsApi;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataFileResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteFile1;
import uk.org.openbanking.datamodel.payment.OBWriteFileResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBExternalStatus1Code;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteFileConsentConverter.toOBFile1;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteFileConverter.toFRWriteFile;

@Controller("FilePaymentsApiV3.0")
@Slf4j
public class FilePaymentsApiController implements FilePaymentsApi {

    private final FilePaymentSubmissionRepository filePaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;

    public FilePaymentsApiController(
            FilePaymentSubmissionRepository filePaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator) {
        this.filePaymentSubmissionRepository = filePaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
    }

    @Override
    public ResponseEntity createFilePayments(@Valid OBWriteFile1 obWriteFile1,
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
        log.debug("Received file payment submission: '{}'", obWriteFile1);

        paymentSubmissionValidator.validateIdempotencyKey(xIdempotencyKey);

        FRWriteFile frWriteFile = toFRWriteFile(obWriteFile1);
        log.trace("Converted to: '{}'", frWriteFile);

        FRFilePaymentSubmission frPaymentSubmission = FRFilePaymentSubmission.builder()
                .id(obWriteFile1.getData().getConsentId())
                .filePayment(frWriteFile)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the file payment(s)
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(filePaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getFilePaymentsFilePaymentId(String filePaymentId,
                                                       String xFapiFinancialId,
                                                       String authorization,
                                                       DateTime xFapiCustomerLastLoggedTime,
                                                       String xFapiCustomerIpAddress,
                                                       String xFapiInteractionId,
                                                       String xCustomerUserAgent,
                                                       HttpServletRequest request,
                                                       Principal principal
    ) throws OBErrorResponseException {
        Optional<FRFilePaymentSubmission> isPaymentSubmission = filePaymentSubmissionRepository.findById(filePaymentId);
        if (!isPaymentSubmission.isPresent()) {
            throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.PAYMENT_SUBMISSION_NOT_FOUND
                            .toOBError1(filePaymentId));
        }

        FRFilePaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getFilePaymentsFilePaymentIdReportFile(String filePaymentId,
                                                                 String xFapiFinancialId,
                                                                 String authorization,
                                                                 DateTime xFapiCustomerLastLoggedTime,
                                                                 String xFapiCustomerIpAddress,
                                                                 String xFapiInteractionId,
                                                                 String xCustomerUserAgent,
                                                                 HttpServletRequest request,
                                                                 Principal principal
    ) throws OBErrorResponseException {

        return new ResponseEntity(HttpStatus.NOT_IMPLEMENTED);

//        FRFilePaymentSubmission filePayment = filePaymentSubmissionRepository.findById(filePaymentId)
//                .orElseThrow(() ->
//                        new OBErrorResponseException(
//                                HttpStatus.BAD_REQUEST,
//                                OBRIErrorResponseCategory.REQUEST_INVALID,
//                                OBRIErrorType.PAYMENT_ID_NOT_FOUND
//                                        .toOBError1(filePaymentId))
//                );
//        log.debug("Payment File '{}' exists with status: {} so generating a report file for type: '{}'",
//                filePayment.getId(),
//                filePayment.getStatus(),
//                filePayment.getFilePayment().getData().getInitiation().getFileType());
//        String reportFile = paymentReportFileService.createPaymentReport(filePayment);
//        log.debug("Generated report file for Payment File: '{}'", filePayment.getId());
//        return ResponseEntity.ok(reportFile);
    }

    private OBWriteFileResponse1 responseEntity(FRFilePaymentSubmission frPaymentSubmission) {
        return new OBWriteFileResponse1()
                .data(new OBWriteDataFileResponse1()
                        .filePaymentId(frPaymentSubmission.getId())
                        .initiation(toOBFile1(frPaymentSubmission.getFilePayment().getData().getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBExternalStatus1Code(frPaymentSubmission.getStatus()))
                        .consentId(frPaymentSubmission.getFilePayment().getData().getConsentId()))
                .links(LinksHelper.createFilePaymentsLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}