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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.v3_1_4.filepayments;

import com.forgerock.securebanking.openbanking.uk.error.OBErrorException;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation.services.FilePaymentFileValidationService;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validations.v3_1_4.filepayments.FilePaymentFileValidationsApi;
import com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.services.ConsentService;
import com.forgerock.securebanking.openbanking.uk.rs.common.filepayment.PaymentFileType;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteFile2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@RestController("FilePaymentFileValidations_v3.1.4")
@Slf4j
public class FilePaymentFileValidationsController implements FilePaymentFileValidationsApi {

    private final FilePaymentFileValidationService filePaymentFileValidationService;
    private final ConsentService consentService;

    public FilePaymentFileValidationsController(FilePaymentFileValidationService filePaymentFileValidationService, ConsentService consentService) {
        this.filePaymentFileValidationService = filePaymentFileValidationService;
        this.consentService = consentService;
    }

    @Override
    public ResponseEntity<Void> filePaymentConsentIdFileValidations(
            String fileContent,
            String consentId,
            String authorization,
            String xFapiFinancialId,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            HttpServletRequest request
    ) throws OBErrorResponseException {
        // preconditions
        Preconditions.checkArgument(!StringUtils.isEmpty(fileContent), "The file content cannot be empty");
        // Get the intent from platform (we need all intent metadata)
        JsonObject intent = consentService.getIDMIntent(authorization, consentId);
        // deserialize the intent to response ob object
        OBWriteFileConsentResponse4 consent = consentService.deserialize(
                OBWriteFileConsentResponse4.class,
                intent.getAsJsonObject("OBIntentObject"),
                consentId
        );
        // The intent already has a file check
        JsonElement intentFileContent = intent.get("FileContent");
        if (!Objects.isNull(intentFileContent)) {
            log.debug("{} The consent {} already has a file uploaded so rejecting.", xFapiFinancialId, consentId);
            throw new OBErrorResponseException(
                    HttpStatus.FORBIDDEN,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.PAYMENT_ALREADY_SUBMITTED
                            .toOBError1(consent.getData().getStatus().getValue())
            );
        }
        // Content type check
        OBWriteFile2DataInitiation initiation = consent.getData().getInitiation();
        PaymentFileType paymentFileType = PaymentFileType.fromFileType(initiation.getFileType());
        verifyContentTypeHeader(
                request.getHeader(HttpHeaders.CONTENT_TYPE),
                paymentFileType
        );

        // validate the file content against the consent previously submitted
        filePaymentFileValidationService.clearErrors().validate(fileContent, initiation, paymentFileType);

        if (filePaymentFileValidationService.getErrors().isEmpty()) {
            return ResponseEntity.ok().build();
        } else {
            throw badRequestResponseException(xFapiFinancialId, filePaymentFileValidationService.getErrors());
        }
    }

    private void verifyContentTypeHeader(String contentTypeHeader, PaymentFileType fileType) throws OBErrorResponseException {
        // Check the file content-type header is compatible with the consent type
        log.debug(
                "Consent indicates file content-type of: '{}'. " +
                        "Actual content-type header of submitted file: '{}'", fileType.getContentType(), contentTypeHeader
        );
        if (!fileType.getContentType().isCompatibleWith(MediaType.parseMediaType(contentTypeHeader))) {
            log.error(
                    "Content type header '{}' for payment file consent does not match the specified file type: '{}'. " +
                            "Expected content-type: {}",
                    contentTypeHeader,
                    fileType,
                    fileType.getContentType()
            );
            throw badRequestResponseException(
                    new OBErrorException(OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_ACCEPTABLE, fileType.getContentType())
            );
        }
    }

    private static OBErrorResponseException badRequestResponseException(String xFapiFinancialId, List<OBError1> errors) {
        log.error("{}, Errors {}", xFapiFinancialId, errors);
        return new OBErrorResponseException(
                HttpStatus.BAD_REQUEST,
                OBRIErrorResponseCategory.REQUEST_INVALID,
                errors
        );
    }

    private OBErrorResponseException badRequestResponseException(String xFapiFinancialId, String message) {
        log.error("{}, Errors {}", xFapiFinancialId, message);
        return new OBErrorResponseException(
                HttpStatus.BAD_REQUEST,
                OBRIErrorResponseCategory.REQUEST_INVALID,
                OBRIErrorType.DATA_INVALID_REQUEST
                        .toOBError1(message)
        );
    }

    private OBErrorResponseException badRequestResponseException(OBErrorException obErrorException) {
        log.error("Errors {}", obErrorException);
        return new OBErrorResponseException(
                HttpStatus.BAD_REQUEST,
                OBRIErrorResponseCategory.REQUEST_INVALID,
                obErrorException.getOBError()
        );
    }
}
