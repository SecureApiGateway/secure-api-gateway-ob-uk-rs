/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v4_0_0.file;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRCharge;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteFileConsentConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v4_0_0.file.FilePaymentConsentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.factories.v4_0_0.OBWriteFileConsentResponse4Factory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.file.PaymentFileProcessorService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.v4.FilePaymentFileContentValidator.FilePaymentFileContentValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFile;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.payment.file.PaymentFileType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.HashUtils;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.file.FilePaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.CreateFilePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FilePaymentConsent;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.file.v3_1_10.FileUploadRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.v4.payment.OBWriteFileConsent3;
import uk.org.openbanking.datamodel.v4.payment.OBWriteFileConsentResponse4;

import java.util.Collections;
import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.payment.FRWriteFileConsentConverter.toFRWriteFileConsent;

@Controller("FilePaymentConsentsApiV4.0.0")
public class FilePaymentConsentsApiController implements FilePaymentConsentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final FilePaymentConsentStoreClient consentStoreApiClient;

    private final OBValidationService<OBWriteFileConsent3> consentValidator;

    private final PaymentFileProcessorService paymentFileProcessorService;

    private final OBValidationService<FilePaymentFileContentValidationContext> fileContentValidator;

    private final OBWriteFileConsentResponse4Factory consentResponseFactory;

    public FilePaymentConsentsApiController(@Qualifier("v4.0.0RestFilePaymentConsentStoreClient") FilePaymentConsentStoreClient consentStoreApiClient,
                                            OBValidationService<OBWriteFileConsent3> consentValidator,
                                            PaymentFileProcessorService paymentFileProcessorService,
                                            OBValidationService<FilePaymentFileContentValidationContext> fileContentValidator,
                                            OBWriteFileConsentResponse4Factory consentResponseFactory) {
        this.consentStoreApiClient = consentStoreApiClient;
        this.consentValidator = consentValidator;
        this.paymentFileProcessorService = paymentFileProcessorService;
        this.fileContentValidator = fileContentValidator;
        this.consentResponseFactory = consentResponseFactory;
    }

    private List<FRCharge> calculateCharges(OBWriteFileConsent3 obWriteFileConsent3) {
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<OBWriteFileConsentResponse4> createFilePaymentConsents(String authorization, String xIdempotencyKey, String xJwsSignature, OBWriteFileConsent3 obWriteFileConsent3, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) throws OBErrorResponseException {
        logger.info("Processing createFilePaymentConsents request - consent: {}, idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                obWriteFileConsent3, xIdempotencyKey, apiClientId, xFapiInteractionId);

        consentValidator.validate(obWriteFileConsent3);

        final CreateFilePaymentConsentRequest createRequest = new CreateFilePaymentConsentRequest();
        createRequest.setConsentRequest(toFRWriteFileConsent(obWriteFileConsent3));
        createRequest.setApiClientId(apiClientId);
        createRequest.setIdempotencyKey(xIdempotencyKey);
        createRequest.setCharges(calculateCharges(obWriteFileConsent3));

        final FilePaymentConsent consent = consentStoreApiClient.createConsent(createRequest);
        logger.info("Created consent - id: {}", consent.getId());

        return new ResponseEntity<>(consentResponseFactory.buildConsentResponse(consent, getClass()), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> createFilePaymentConsentsConsentIdFile(String consentId, String authorization, String xIdempotencyKey, String xJwsSignature, String body, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request) throws OBErrorException, OBErrorResponseException {
        logger.info("Processing createFilePaymentConsentsConsentIdFile request - idempotencyKey: {}, apiClient: {}, x-fapi-interaction-id: {}",
                xIdempotencyKey, apiClientId, xFapiInteractionId);

        if (body == null || body.isBlank()) {
            throw new OBErrorException(OBRIErrorType.REQUEST_FILE_EMPTY);
        }

        final FilePaymentConsent consent = consentStoreApiClient.getConsent(consentId, apiClientId);

        final String fileType = consent.getRequestObj().getData().getInitiation().getFileType();
        final PaymentFileType paymentFileType = paymentFileProcessorService.findPaymentFileType(fileType);
        final String contentType = request.getContentType();
        if (!paymentFileType.getContentType().toString().equals(contentType)) {
            throw new OBErrorException(OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_SUPPORTED, contentType, paymentFileType.getContentType());
        }

        final PaymentFile paymentFile = paymentFileProcessorService.processFile(fileType, body);

        fileContentValidator.validate(new FilePaymentFileContentValidationContext(HashUtils.computeSHA256FullHash(body),
                paymentFile, FRWriteFileConsentConverter.toOBWriteFileConsent3(consent.getRequestObj())));

        final FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setApiClientId(apiClientId);
        fileUploadRequest.setConsentId(consentId);
        fileUploadRequest.setFileUploadIdempotencyKey(xIdempotencyKey);
        fileUploadRequest.setFileContents(body);

        consentStoreApiClient.uploadFile(fileUploadRequest);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<OBWriteFileConsentResponse4> getFilePaymentConsentsConsentId(String consentId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) {
        logger.info("Processing getFilePaymentConsentsConsentId request - consentId: {}, apiClient: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        return ResponseEntity.ok(consentResponseFactory.buildConsentResponse(consentStoreApiClient.getConsent(consentId, apiClientId), getClass()));
    }

    @Override
    public ResponseEntity<Object> getFilePaymentConsentsConsentIdFile(String consentId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) throws OBErrorException {
        logger.info("Processing getFilePaymentConsentsConsentIdFile request - consentId: {}, apiClient: {}, x-fapi-interaction-id: {}",
                consentId, apiClientId, xFapiInteractionId);

        final FilePaymentConsent consent = consentStoreApiClient.getConsent(consentId, apiClientId);
        if (consent.getFileContent() == null) {
            throw new OBErrorException(OBRIErrorType.NO_FILE_FOR_CONSENT);
        }
        final String fileType = consent.getRequestObj().getData().getInitiation().getFileType();
        final PaymentFileType paymentFileType = paymentFileProcessorService.findPaymentFileType(fileType);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(paymentFileType.getContentType())
                .body(consent.getFileContent());
    }
}
